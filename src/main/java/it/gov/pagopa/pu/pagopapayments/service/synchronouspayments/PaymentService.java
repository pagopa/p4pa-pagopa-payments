package it.gov.pagopa.pu.pagopapayments.service.synchronouspayments;

import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.organization.dto.generated.Broker;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.pagopapayments.connector.DebtPositionClient;
import it.gov.pagopa.pu.pagopapayments.connector.OrganizationClient;
import it.gov.pagopa.pu.pagopapayments.connector.auth.AuthnService;
import it.gov.pagopa.pu.pagopapayments.dto.RetrievePaymentDTO;
import it.gov.pagopa.pu.pagopapayments.enums.PagoPaNodeFaults;
import it.gov.pagopa.pu.pagopapayments.exception.SynchronousPaymentException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class PaymentService {

  public enum PaymentStatus {
    UNPAID,
    EXPIRED,
    PAID,
    CANCELLED,
    DRAFT,
    TO_SYNCH
  }

  private final DebtPositionClient debtPositionClient;
  private final OrganizationClient organizationClient;
  private final AuthnService authnService;

  public PaymentService(DebtPositionClient debtPositionClient, OrganizationClient organizationClient, AuthnService authnService) {
    this.debtPositionClient = debtPositionClient;
    this.organizationClient = organizationClient;
    this.authnService = authnService;
  }

  public Pair<InstallmentDTO, Organization> retrievePayment(RetrievePaymentDTO request) {
    String accessToken = authnService.getAccessToken();
    Organization organization = organizationClient.getOrganizationByFiscalCode(request.getFiscalCode(), accessToken);
    if (organization == null) {
      throw new SynchronousPaymentException(PagoPaNodeFaults.PAA_ID_DOMINIO_ERRATO.code(), request.getIdBrokerPA());
    }
    if (!Objects.equals(organization.getStatus(), Organization.StatusEnum.ACTIVE)) {
      log.warn("retrievePayment [{}/{}]: organization is not active", request.getFiscalCode(), request.getNoticeNumber());
      throw new SynchronousPaymentException(PagoPaNodeFaults.PAA_ID_DOMINIO_ERRATO.code(), organization.getOrgFiscalCode());
    }
    //broker cannot be null if organization is found
    Broker broker = organizationClient.getBrokerById(organization.getBrokerId(), accessToken);
    if (!Objects.equals(request.getIdBrokerPA(), broker.getBrokerFiscalCode())) {
      log.warn("retrievePayment [{}/{}]: invalid broken for organization expected/actual[{}/{}]",
        request.getFiscalCode(), request.getNoticeNumber(),
        request.getIdBrokerPA(), broker.getBrokerFiscalCode());
      throw new SynchronousPaymentException(PagoPaNodeFaults.PAA_ID_INTERMEDIARIO_ERRATO.code(), broker.getBrokerFiscalCode());
    }
    if (!Objects.equals(request.getIdStation(), broker.getStationId())) {
      log.warn("retrievePayment [{}/{}]: invalid stationId for organization broker expected/actual[{}/{}]",
        request.getFiscalCode(), request.getNoticeNumber(),
        request.getIdStation(), broker.getStationId());
      throw new SynchronousPaymentException(PagoPaNodeFaults.PAA_STAZIONE_INT_ERRATA.code(), broker.getBrokerFiscalCode());
    }

    InstallmentDTO installment = getPayableDebtPositionByOrganizationAndNav(organization, request.getNoticeNumber(), null, accessToken);
    return Pair.of(installment, organization);
  }

  private InstallmentDTO getPayableDebtPositionByOrganizationAndNav(Organization organization, String noticeNumber, Boolean postalTransfer, String accessToken) {
    List<InstallmentDTO> installmentDTOList = debtPositionClient.getDebtPositionsByOrganizationIdAndNav(organization.getOrganizationId(), noticeNumber, accessToken);

    if (installmentDTOList.isEmpty()) {
      log.debug("getPayableDebtPositionByOrganizationAndNav [{}/{}]: no debt positions found", organization.getOrgFiscalCode(), noticeNumber);
      throw new SynchronousPaymentException(PagoPaNodeFaults.PAA_PAGAMENTO_SCONOSCIUTO.code(), organization.getOrgFiscalCode());
    }

    /*
     * Rules (first one true wins, in this order):
     * If no data found -> KO PAA_PAGAMENTO_SCONOSCIUTO
     * If there is 1 UNPAID -> OK
     * If there are >1 UNPAID -> KO PAA_PAGAMENTO_DUPLICATO
     * If there is >= 1 EXPIRED -> KO PAA_PAGAMENTO_SCADUTO
     * If there is >= 1 PAID -> KO PAA_PAGAMENTO_SCADUTO
     * If there is >= 1 CANCELLED -> KO PAA_PAGAMENTO_ANNULLATO
     * Any other case -> KO PAA_PAGAMENTO_SCONOSCIUTO
     */

    List<InstallmentDTO> payableInstallmentDTOList = installmentDTOList.stream().filter(i -> {
      //if status is not UNPAID, the installment is not payable
      if (!Objects.equals(i.getStatus(), PaymentStatus.UNPAID.name()))
        return false;
      //only for getPayment (for verifyPayment, postalTransfer is not set):
      //if at least 1 transfer of the same organization that created the debt position
      // does not have a suitable IBAN for payment type (postal/pagopa), the installment is not payable
      return postalTransfer==null || i.getTransfers().stream()
        .filter(t -> Objects.equals(t.getOrgFiscalCode(), organization.getOrgFiscalCode()))
        .map(t -> postalTransfer ? t.getPostalIban() : t.getIban())
        .noneMatch(StringUtils::isBlank);
    }).toList();
    if (payableInstallmentDTOList.size() > 1) {
      log.warn("getPayableDebtPositionByOrganizationAndNav [{}/{}]: multiple payable debt positions found", organization.getOrgFiscalCode(), noticeNumber);
      throw new SynchronousPaymentException(PagoPaNodeFaults.PAA_PAGAMENTO_DUPLICATO.code(), organization.getOrgFiscalCode());
    } else if (payableInstallmentDTOList.size() == 1) {
      return payableInstallmentDTOList.getFirst();
    } else if (installmentDTOList.stream().anyMatch(i -> Objects.equals(i.getStatus(), PaymentStatus.EXPIRED.name()))) {
      throw new SynchronousPaymentException(PagoPaNodeFaults.PAA_PAGAMENTO_SCADUTO.code(), organization.getOrgFiscalCode());
    } else if (installmentDTOList.stream().anyMatch(i -> Objects.equals(i.getStatus(), PaymentStatus.PAID.name()))) {
      throw new SynchronousPaymentException(PagoPaNodeFaults.PAA_PAGAMENTO_SCONOSCIUTO.code(), organization.getOrgFiscalCode());
    } else if (installmentDTOList.stream().anyMatch(i -> Objects.equals(i.getStatus(), PaymentStatus.CANCELLED.name()))) {
      throw new SynchronousPaymentException(PagoPaNodeFaults.PAA_PAGAMENTO_ANNULLATO.code(), organization.getOrgFiscalCode());
    } else {
      throw new SynchronousPaymentException(PagoPaNodeFaults.PAA_PAGAMENTO_SCONOSCIUTO.code(), organization.getOrgFiscalCode());
    }
  }

}
