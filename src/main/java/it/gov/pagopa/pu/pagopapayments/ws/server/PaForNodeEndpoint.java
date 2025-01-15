package it.gov.pagopa.pu.pagopapayments.ws.server;

import it.gov.pagopa.pagopa_api.pa.pafornode.*;
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
    long startTime = System.currentTimeMillis();
    try {
      return paymentService.paGetPayment(request);
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

}
