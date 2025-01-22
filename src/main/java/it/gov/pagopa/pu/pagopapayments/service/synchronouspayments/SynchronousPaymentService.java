package it.gov.pagopa.pu.pagopapayments.service.synchronouspayments;

import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.pagopapayments.connector.DebtPositionClient;
import it.gov.pagopa.pu.pagopapayments.connector.auth.AuthnService;
import it.gov.pagopa.pu.pagopapayments.dto.RetrievePaymentDTO;
import it.gov.pagopa.pu.pagopapayments.enums.PagoPaNodeFaults;
import it.gov.pagopa.pu.pagopapayments.exception.PagoPaNodeFaultException;
import it.gov.pagopa.pu.pagopapayments.service.PaForNodeRequestValidatorService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class SynchronousPaymentService {

  private final DebtPositionClient debtPositionClient;
  private final PaForNodeRequestValidatorService paForNodeRequestValidatorService;
  private final SynchronousPaymentStatusVerifierService synchronousPaymentStatusVerifierService;
  private final AuthnService authnService;

  public SynchronousPaymentService(DebtPositionClient debtPositionClient,
                                   PaForNodeRequestValidatorService paForNodeRequestValidatorService,
                                   SynchronousPaymentStatusVerifierService synchronousPaymentStatusVerifierService,
                                   AuthnService authnService) {
    this.debtPositionClient = debtPositionClient;
    this.paForNodeRequestValidatorService = paForNodeRequestValidatorService;
    this.synchronousPaymentStatusVerifierService = synchronousPaymentStatusVerifierService;
    this.authnService = authnService;
  }

  public Pair<InstallmentDTO, Organization> retrievePayment(RetrievePaymentDTO request) {
    String accessToken = authnService.getAccessToken();
    String requestIdMessage = request.getFiscalCode()+"/"+request.getNoticeNumber();
    if (!Objects.equals(request.getIdPA(), request.getFiscalCode())) {
      log.warn("paymentRequestValidate [{}]: unexpected idPA[{}]", requestIdMessage, request.getIdPA());
      throw new PagoPaNodeFaultException(PagoPaNodeFaults.PAA_ID_DOMINIO_ERRATO, request.getFiscalCode());
    }
    Organization organization = paForNodeRequestValidatorService.paForNodeRequestValidate(request, accessToken);
    InstallmentDTO installment = getPayableDebtPositionByOrganizationAndNav(organization, request.getNoticeNumber(), request.getPostalTransfer(), accessToken);
    return Pair.of(installment, organization);
  }

  private InstallmentDTO getPayableDebtPositionByOrganizationAndNav(Organization organization, String noticeNumber, Boolean postalTransfer, String accessToken) {
    List<InstallmentDTO> installmentDTOList = debtPositionClient.getDebtPositionsByOrganizationIdAndNav(organization.getOrganizationId(), noticeNumber, accessToken);
    return synchronousPaymentStatusVerifierService.verifyPaymentStatus(organization, installmentDTOList, noticeNumber, postalTransfer);
  }

}
