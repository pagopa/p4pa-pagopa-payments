package it.gov.pagopa.pu.pagopapayments.controller;

import it.gov.pagopa.pu.fororgs.controller.generated.PaymentOptionsApi;
import it.gov.pagopa.pu.fororgs.dto.generated.PaymentOptionsResponse;
import it.gov.pagopa.pu.pagopapayments.service.paymentoptionsforpsp.PaymentOptionsForPSPService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class PaymentOptionsForPSPController implements PaymentOptionsApi {

  private final PaymentOptionsForPSPService service;

  public PaymentOptionsForPSPController(PaymentOptionsForPSPService service) {
    this.service = service;
  }

  @Override
  public ResponseEntity<PaymentOptionsResponse> getPaymentOptionsByNoticeNumber(String noticeNumber, String organizationFiscalCode) {
    log.info("Retrieve Payment Options that have notice number {} and organization fiscal code {}", noticeNumber, organizationFiscalCode);
    return ResponseEntity.ofNullable(service.getPaymentOptions(noticeNumber, organizationFiscalCode));
  }
}
