package it.gov.pagopa.pu.pagopapayments.ws.server;

import it.gov.pagopa.pagopa_api.pa.pafornode.*;
import it.gov.pagopa.pagopa_api.xsd.common_types.v1_0.CtFaultBean;
import it.gov.pagopa.pagopa_api.xsd.common_types.v1_0.CtResponse;
import it.gov.pagopa.pagopa_api.xsd.common_types.v1_0.StOutcome;
import it.gov.pagopa.pu.pagopapayments.enums.PagoPaNodeFaults;
import it.gov.pagopa.pu.pagopapayments.mapper.PaGetPaymentMapper;
import it.gov.pagopa.pu.pagopapayments.service.synchronouspayments.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

@Endpoint
@Slf4j
public class PaForNodeEndpoint {
  public static final String NAMESPACE_URI = "http://pagopa-api.pagopa.gov.it/pa/paForNode.xsd";
  public static final String NAME = "PaForNode";

  private final PaymentService paymentService;

  public PaForNodeEndpoint(PaymentService paymentService) {
    this.paymentService = paymentService;
  }

  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "PaDemandPaymentNoticeRequest")
  @ResponsePayload
  public PaDemandPaymentNoticeResponse paDemandPaymentNotice(@RequestPayload PaDemandPaymentNoticeRequest request){
    throw new UnsupportedOperationException("paDemandPaymentNotice is not supported");
  }

  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "paVerifyPaymentNoticeReq")
  @ResponsePayload
  public PaVerifyPaymentNoticeRes paVerifyPaymentNotice(@RequestPayload PaVerifyPaymentNoticeReq request){
    long startTime = System.currentTimeMillis();
    try {
      return paymentService.paVerifyPaymentNotice(request);
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
      //convert V1 request to V2
      PaGetPaymentV2Request requestV2 = PaGetPaymentMapper.paGetPaymentReq2V2(request);
      //invoke V2 service
      PaGetPaymentV2Response responseV2 = paymentService.paGetPaymentV2(requestV2);
      //verify response is compatbile with V1
      if(responseV2.getData()!=null && responseV2.getData().getTransferList().getTransfers().stream().anyMatch(transfer -> transfer.getRichiestaMarcaDaBollo()!=null)) {
        log.warn("paGetPaymentV1 [{}/{}]: marcadabollo is not supported", request.getQrCode().getFiscalCode(), request.getQrCode().getNoticeNumber());
        return handleFault(PagoPaNodeFaults.PAA_SEMANTICA, request.getIdPA(), new PaGetPaymentRes());
      }
      //convert V2 response to V1
      return PaGetPaymentMapper.paGetPaymentV2Response2V1(responseV2);
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
      return paymentService.paGetPaymentV2(request);
    } finally {
      long elapsed = System.currentTimeMillis() - startTime;
      log.info("SOAP WS paGetPaymentV2, elapsed time[{}]", elapsed);
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
