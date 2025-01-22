package it.gov.pagopa.pu.pagopapayments.endpoint;

import it.gov.pagopa.pagopa_api.pa.pafornode.*;
import it.gov.pagopa.pagopa_api.xsd.common_types.v1_0.CtFaultBean;
import it.gov.pagopa.pagopa_api.xsd.common_types.v1_0.CtResponse;
import it.gov.pagopa.pagopa_api.xsd.common_types.v1_0.StOutcome;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.pagopapayments.dto.PaSendRtDTO;
import it.gov.pagopa.pu.pagopapayments.dto.RetrievePaymentDTO;
import it.gov.pagopa.pu.pagopapayments.enums.PagoPaNodeFaults;
import it.gov.pagopa.pu.pagopapayments.exception.PagoPaNodeFaultException;
import it.gov.pagopa.pu.pagopapayments.mapper.PaGetPaymentMapper;
import it.gov.pagopa.pu.pagopapayments.mapper.PaSendRTMapper;
import it.gov.pagopa.pu.pagopapayments.mapper.PaVerifyPaymentNoticeMapper;
import it.gov.pagopa.pu.pagopapayments.service.receipt.ReceiptService;
import it.gov.pagopa.pu.pagopapayments.service.synchronouspayments.SynchronousPaymentService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

@Endpoint
@Slf4j
public class PaForNodeEndpoint {
  public static final String NAMESPACE_URI = "http://pagopa-api.pagopa.gov.it/pa/paForNode.xsd";
  public static final String NAME = "PaForNode";

  private final SynchronousPaymentService synchronousPaymentService;
  private final ReceiptService receiptService;
  private final PaSendRTMapper paSendRTMapper;


  public PaForNodeEndpoint(SynchronousPaymentService synchronousPaymentService, ReceiptService receiptService, PaSendRTMapper paSendRTMapper) {
    this.synchronousPaymentService = synchronousPaymentService;
    this.receiptService = receiptService;
    this.paSendRTMapper = paSendRTMapper;
  }

  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "PaDemandPaymentNoticeRequest")
  @ResponsePayload
  public PaDemandPaymentNoticeResponse paDemandPaymentNotice(@RequestPayload PaDemandPaymentNoticeRequest request){
    log.info("processing paDemandPaymentNotice idPA[{}] servizio[{}/{}]", request.getIdPA(), request.getIdSoggettoServizio(), request.getIdServizio());
    return handleFault(PagoPaNodeFaults.PAA_SYSTEM_ERROR, request.getIdPA(), new PaDemandPaymentNoticeResponse());
  }

  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "paVerifyPaymentNoticeReq")
  @ResponsePayload
  public PaVerifyPaymentNoticeRes paVerifyPaymentNotice(@RequestPayload PaVerifyPaymentNoticeReq request){
    long startTime = System.currentTimeMillis();
    try {
    log.info("processing paVerifyPaymentNotice idPA[{}] notice[{}/{}]", request.getIdPA(), request.getQrCode().getFiscalCode(), request.getQrCode().getNoticeNumber());
      RetrievePaymentDTO retrievePaymentDTO = PaVerifyPaymentNoticeMapper.paVerifyPaymentNoticeReq2RetrievePaymentDTO(request);
      Pair<InstallmentDTO, Organization> installmentAndOrganization = synchronousPaymentService.retrievePayment(retrievePaymentDTO);
      return PaVerifyPaymentNoticeMapper.installmentDto2PaVerifyPaymentNoticeRes(
        installmentAndOrganization.getLeft(), installmentAndOrganization.getRight());
    } catch(PagoPaNodeFaultException spe) {
      log.error("Fault in paVerifyPaymentNotice [{}/{}] {}", request.getQrCode().getFiscalCode(), request.getQrCode().getNoticeNumber(), spe.getErrorCode());
      return handleFault(spe.getErrorCode(), spe.getErrorEmitter(), new PaVerifyPaymentNoticeRes());
    } catch(Exception e) {
      log.error("Error in paGetPayment [{}/{}]", request.getQrCode().getFiscalCode(), request.getQrCode().getNoticeNumber(), e);
      return handleFault(PagoPaNodeFaults.PAA_SYSTEM_ERROR, request.getIdPA(), new PaVerifyPaymentNoticeRes());
    } finally {
      long elapsed = System.currentTimeMillis() - startTime;
      log.info("SOAP WS paVerifyPaymentNotice, elapsed time[{}]", elapsed);
    }
  }

  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "PaGetPaymentReq")
  @ResponsePayload
  public PaGetPaymentRes paGetPayment(@RequestPayload PaGetPaymentReq request) {
    // this operation is just supported for retro compatibility and ideally the broker should be configured to use paGetPaymentV2;
    // it's implementation is similar to paGetPaymentV2, only differences are:
    // - marcadabollo is not supported
    // - metadata is not supported
    long startTime = System.currentTimeMillis();
    try {
      RetrievePaymentDTO retrievePaymentDTO = PaGetPaymentMapper.paPaGetPaymentReq2RetrievePaymentDTO(request);
      //invoke V2 service
      Pair<InstallmentDTO, Organization> installmentAndOrganization = synchronousPaymentService.retrievePayment(retrievePaymentDTO);
      //verify response is compatible with V1
      if(installmentAndOrganization.getLeft().getTransfers().stream().anyMatch(transfer -> transfer.getStampHashDocument() != null)) {
        log.warn("paGetPaymentV1 [{}/{}]: marcadabollo is not supported", retrievePaymentDTO.getFiscalCode(), retrievePaymentDTO.getNoticeNumber());
        return handleFault(PagoPaNodeFaults.PAA_SEMANTICA, retrievePaymentDTO.getIdPA(), new PaGetPaymentRes());
      }
      return PaGetPaymentMapper.installmentDto2PaGetPaymentRes(
        installmentAndOrganization.getLeft(), installmentAndOrganization.getRight(), request.getTransferType());
    } catch(PagoPaNodeFaultException spe) {
      log.error("Fault in paGetPayment [{}/{}] {}", request.getQrCode().getFiscalCode(), request.getQrCode().getNoticeNumber(), spe.getErrorCode());
      return handleFault(spe.getErrorCode(), spe.getErrorEmitter(), new PaGetPaymentRes());
    } catch(Exception e) {
      log.error("Error in paGetPayment [{}/{}]", request.getQrCode().getFiscalCode(), request.getQrCode().getNoticeNumber(), e);
      return handleFault(PagoPaNodeFaults.PAA_SYSTEM_ERROR, request.getIdPA(), new PaGetPaymentRes());
    } finally {
      long elapsed = System.currentTimeMillis() - startTime;
      log.info("SOAP WS paGetPayment, elapsed time[{}]", elapsed);
    }
  }

  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "PaGetPaymentV2Request")
  @ResponsePayload
  public PaGetPaymentV2Response paGetPaymentV2(@RequestPayload PaGetPaymentV2Request request) {
    long startTime = System.currentTimeMillis();
    try {
      log.info("processing paGetPaymentV2 idPA[{}] notice[{}/{}]", request.getIdPA(), request.getQrCode().getFiscalCode(), request.getQrCode().getNoticeNumber());
      RetrievePaymentDTO retrievePaymentDTO = PaGetPaymentMapper.paPaGetPaymentV2Request2RetrievePaymentDTO(request);
      Pair<InstallmentDTO, Organization> installmentAndOrganization = synchronousPaymentService.retrievePayment(retrievePaymentDTO);
      return PaGetPaymentMapper.installmentDto2PaGetPaymentV2Response(
        installmentAndOrganization.getLeft(), installmentAndOrganization.getRight(), request.getTransferType());
    } catch(PagoPaNodeFaultException spe) {
      log.error("Fault in paGetPaymentV2 [{}/{}] {}", request.getQrCode().getFiscalCode(), request.getQrCode().getNoticeNumber(), spe.getErrorCode());
      return handleFault(spe.getErrorCode(), spe.getErrorEmitter(), new PaGetPaymentV2Response());
    } catch(Exception e) {
      log.error("Error in paGetPaymentV2 [{}/{}]", request.getQrCode().getFiscalCode(), request.getQrCode().getNoticeNumber(), e);
      return handleFault(PagoPaNodeFaults.PAA_SYSTEM_ERROR, request.getIdPA(), new PaGetPaymentV2Response());
    } finally {
      long elapsed = System.currentTimeMillis() - startTime;
      log.info("SOAP WS paGetPaymentV2, elapsed time[{}]", elapsed);
    }
  }

  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "PaSendRTV2Request")
  @ResponsePayload
  public PaSendRTV2Response paSendRTV2(@RequestPayload PaSendRTV2Request request) {
    long startTime = System.currentTimeMillis();
    try {
      log.info("processing paSendRTV2 idPA[{}] notice[{}/{}]", request.getIdPA(), request.getReceipt().getFiscalCode(), request.getReceipt().getNoticeNumber());
      PaSendRtDTO paSendRtDTO = paSendRTMapper.paSendRtV2Request2PaSendRtDTO(request);
      receiptService.processReceivedReceipt(paSendRtDTO);
      PaSendRTV2Response response = new PaSendRTV2Response();
      response.setOutcome(StOutcome.OK);
      return response;
    } catch(PagoPaNodeFaultException spe) {
      log.error("Fault in paSendRTV2 [{}/{}] {}", request.getReceipt().getNoticeNumber(), request.getReceipt().getFiscalCode(), spe.getErrorCode());
      return handleFault(spe.getErrorCode(), spe.getErrorEmitter(), new PaSendRTV2Response());
    } catch(Exception e) {
      log.error("Error in paSendRTV2 [{}/{}] {}", request.getReceipt().getNoticeNumber(), request.getReceipt().getFiscalCode(), request.getReceipt().getReceiptId(), e);
      return handleFault(PagoPaNodeFaults.PAA_SYSTEM_ERROR, request.getIdPA(), new PaSendRTV2Response());
    } finally {
      long elapsed = System.currentTimeMillis() - startTime;
      log.info("SOAP WS paSendRTV2, elapsed time[{}]", elapsed);
    }
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

}
