package it.gov.pagopa.pu.pagopapayments.service.synchronouspayments;

import it.gov.pagopa.pagopa_api.pa.pafornode.*;
import it.gov.pagopa.pagopa_api.xsd.common_types.v1_0.CtFaultBean;
import it.gov.pagopa.pagopa_api.xsd.common_types.v1_0.CtResponse;
import it.gov.pagopa.pagopa_api.xsd.common_types.v1_0.StOutcome;
import it.gov.pagopa.pu.organization.dto.generated.Broker;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.pagopapayments.connector.DebtPositionClient;
import it.gov.pagopa.pu.pagopapayments.connector.OrganizationClient;
import it.gov.pagopa.pu.pagopapayments.connector.auth.AuthnService;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.pagopapayments.mapper.PaGetPaymentMapper;
import it.gov.pagopa.pu.pagopapayments.mapper.PaVerifyPaymentNoticeMapper;
import it.gov.pagopa.pu.pagopapayments.util.PagoPaNodeFaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Slf4j
public class PaymentService {

  public enum PaymentStatus {
    UNPAID,
    EXPIRED,
    PAID,
    CANCELLED,
    DRAFT
  }

  private final DebtPositionClient debtPositionClient;
  private final OrganizationClient organizationClient;
  private final AuthnService authnService;

  public PaymentService(DebtPositionClient debtPositionClient, OrganizationClient organizationClient, AuthnService authnService) {
    this.debtPositionClient = debtPositionClient;
    this.organizationClient = organizationClient;
    this.authnService = authnService;
  }

  public PaVerifyPaymentNoticeRes paVerifyPaymentNotice(@RequestPayload PaVerifyPaymentNoticeReq request) {
    PaVerifyPaymentNoticeRes response = new PaVerifyPaymentNoticeRes();

    String accessToken = authnService.getAccessToken();
    Organization organization = organizationClient.getOrganizationByFiscalCode(request.getQrCode().getFiscalCode(), accessToken);
    if(organization==null) {
      log.warn("paVerifyPaymentNotice [{}/{}]: organization is not found",
        request.getQrCode().getFiscalCode(), request.getQrCode().getNoticeNumber());
      return handleFault(PagoPaNodeFaults.PAA_ID_DOMINIO_ERRATO, request.getIdBrokerPA(), response);
    }
    if(!Objects.equals(Organization.StatusEnum.ACTIVE, organization.getStatus())){
      log.warn("paVerifyPaymentNotice [{}/{}]: organization is not active [{}]",
        request.getQrCode().getFiscalCode(), request.getQrCode().getNoticeNumber(),
        organization.getStatus());
      return handleFault(PagoPaNodeFaults.PAA_ID_DOMINIO_ERRATO, organization.getOrgFiscalCode(), response);
    }
    Broker broker = organizationClient.getBrokerById(organization.getBrokerId(), accessToken);
    if(broker==null || !Objects.equals(request.getIdBrokerPA(), broker.getBrokerFiscalCode())) {
      log.warn("paVerifyPaymentNotice [{}/{}]: invalid broken for organization expected/actual[{}/{}]",
        request.getQrCode().getFiscalCode(), request.getQrCode().getNoticeNumber(),
        request.getIdBrokerPA(), broker!=null?broker.getBrokerFiscalCode():null);
      return handleFault(PagoPaNodeFaults.PAA_ID_INTERMEDIARIO_ERRATO,
        Optional.ofNullable(broker).map(Broker::getBrokerFiscalCode).orElse(request.getIdBrokerPA()), response);
    }
    if(!Objects.equals(request.getIdStation(), broker.getStationId())) {
      log.warn("paVerifyPaymentNotice [{}/{}]: invalid stationId for organization broker expected/actual[{}/{}]",
        request.getQrCode().getFiscalCode(), request.getQrCode().getNoticeNumber(),
        request.getIdStation(), broker.getStationId());
      return handleFault(PagoPaNodeFaults.PAA_ID_DOMINIO_ERRATO, broker.getBrokerFiscalCode(), response);
    }

    Pair<InstallmentDTO, PagoPaNodeFaults> installmentOrFault = getPayableDebtPositionByOrganizationIdAndNav(organization.getOrganizationId(), request.getQrCode().getNoticeNumber(), accessToken);
    if(installmentOrFault.getRight()!=null) {
      return handleFault(installmentOrFault.getRight(), request.getIdBrokerPA(), response);
    }
    InstallmentDTO installment = installmentOrFault.getLeft();
    response = PaVerifyPaymentNoticeMapper.installmentDto2PaVerifyPaymentNoticeRes(installment, organization);
    return response;
  }

  public PaGetPaymentRes paGetPayment(@RequestPayload PaGetPaymentReq request) {
    // paGetPayment is just supported for retro compatibility;
    // it's implementation is similar to paGetPaymentV2, only differences are:
    // - marcadabollo is not supported
    // - metadata is not supported
    PaGetPaymentV2Request requestV2 = PaGetPaymentMapper.paGetPaymentReq2V2(request);
    PaGetPaymentV2Response responseV2 = paGetPaymentImpl(requestV2);
    if(responseV2.getData()!=null && responseV2.getData().getTransferList().getTransfers().stream().anyMatch(transfer -> transfer.getRichiestaMarcaDaBollo()!=null)) {
      log.warn("paGetPaymentV1 [{}/{}]: marcadabollo is not supported", request.getQrCode().getFiscalCode(), request.getQrCode().getNoticeNumber());
      return handleFault(PagoPaNodeFaults.PAA_SEMANTICA, request.getIdPA(), new PaGetPaymentRes());
    }
    return PaGetPaymentMapper.paGetPaymentV2Response2V1(responseV2);
  }

  public PaGetPaymentV2Response paGetPaymentV2(@RequestPayload PaGetPaymentV2Request request) {
    return paGetPaymentImpl(request);
  }

  private PaGetPaymentV2Response paGetPaymentImpl(PaGetPaymentV2Request request) {
    PaGetPaymentV2Response response = new PaGetPaymentV2Response();

    String accessToken = authnService.getAccessToken();
    Organization organization = organizationClient.getOrganizationByFiscalCode(request.getQrCode().getFiscalCode(), accessToken);
    if(organization==null) {
      return handleFault(PagoPaNodeFaults.PAA_ID_DOMINIO_ERRATO, request.getIdBrokerPA(), response);
    }
    if(!Objects.equals(organization.getStatus(), Organization.StatusEnum.ACTIVE)){
      log.warn("paGetPayment [{}/{}]: organization is not active", request.getQrCode().getFiscalCode(), request.getQrCode().getNoticeNumber());
      return handleFault(PagoPaNodeFaults.PAA_ID_DOMINIO_ERRATO, organization.getOrgFiscalCode(), response);
    }
    Broker broker = organizationClient.getBrokerById(organization.getBrokerId(), accessToken);
    if(broker==null || !Objects.equals(broker.getBrokerFiscalCode(), request.getIdBrokerPA())) {
      return handleFault(PagoPaNodeFaults.PAA_ID_INTERMEDIARIO_ERRATO,
        Optional.ofNullable(broker).map(Broker::getBrokerFiscalCode).orElse(request.getIdBrokerPA()), response);
    }
    if(!Objects.equals(broker.getStationId(), request.getIdStation())) {
      log.warn("paGetPayment [{}/{}]: invalid broker for organization", request.getQrCode().getFiscalCode(), request.getQrCode().getNoticeNumber());
      return handleFault(PagoPaNodeFaults.PAA_ID_DOMINIO_ERRATO, broker.getBrokerFiscalCode(), response);
    }



    Pair<InstallmentDTO, PagoPaNodeFaults> installmentOrFault = getPayableDebtPositionByOrganizationIdAndNav(organization.getOrganizationId(), request.getQrCode().getNoticeNumber(), accessToken);
    if(installmentOrFault.getRight()!=null) {
      return handleFault(installmentOrFault.getRight(), request.getIdBrokerPA(), response);
    }
    InstallmentDTO installmentDTO = installmentOrFault.getLeft();
    response = PaGetPaymentMapper.installmentDto2PaGetPaymentV2Response(installmentDTO, organization, request.getTransferType());
    return response;
  }

  private <T extends CtResponse> T handleFault(PagoPaNodeFaults fault, String idFaultEmitter, T responseObj){
    responseObj.setFault(new CtFaultBean());
    responseObj.getFault().setFaultCode(fault.code());
    responseObj.getFault().setDescription(fault.description());
    responseObj.getFault().setFaultString(fault.description());
    responseObj.getFault().setId(idFaultEmitter);
    responseObj.getFault().setSerial(0);
    responseObj.setOutcome(StOutcome.KO);
    return responseObj;
  }

  private Pair<InstallmentDTO, PagoPaNodeFaults> getPayableDebtPositionByOrganizationIdAndNav(Long organizationId, String noticeNumber, String accessToken){
    List<InstallmentDTO> installmentDTOList = debtPositionClient.getDebtPositionsByOrganizationIdAndNav(organizationId, noticeNumber, accessToken);

    if(installmentDTOList.isEmpty()){
      log.debug("getPayableDebtPositionByOrganizationIdAndNav [{}/{}]: no debt positions found", organizationId, noticeNumber);
      return Pair.of(null, PagoPaNodeFaults.PAA_PAGAMENTO_SCONOSCIUTO);
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

    List<InstallmentDTO> payableInstallmentDTOList = installmentDTOList.stream().filter(i -> i.getStatus().equals(PaymentStatus.UNPAID.name())).toList();
    if(payableInstallmentDTOList.size()>1){
      log.warn("getPayableDebtPositionByOrganizationIdAndNav [{}/{}]: multiple payable debt positions found", organizationId, noticeNumber);
      return Pair.of(null, PagoPaNodeFaults.PAA_PAGAMENTO_DUPLICATO);
    } else if(payableInstallmentDTOList.size()==1){
      return Pair.of(payableInstallmentDTOList.getFirst(), null);
    } else if(installmentDTOList.stream().anyMatch(i -> i.getStatus().equals(PaymentStatus.EXPIRED.name()))){
      return Pair.of(null, PagoPaNodeFaults.PAA_PAGAMENTO_SCADUTO);
    } else if(installmentDTOList.stream().anyMatch(i -> i.getStatus().equals(PaymentStatus.PAID.name()))){
      return Pair.of(null, PagoPaNodeFaults.PAA_PAGAMENTO_SCONOSCIUTO);
    } else if(installmentDTOList.stream().anyMatch(i -> i.getStatus().equals(PaymentStatus.CANCELLED.name()))){
      return Pair.of(null, PagoPaNodeFaults.PAA_PAGAMENTO_ANNULLATO);
    } else {
      return Pair.of(null, PagoPaNodeFaults.PAA_PAGAMENTO_SCONOSCIUTO);
    }
  }

}
