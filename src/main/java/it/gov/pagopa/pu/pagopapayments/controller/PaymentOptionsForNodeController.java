package it.gov.pagopa.pu.pagopapayments.controller;

import it.gov.pagopa.pu.orgfornode.controller.generated.PaymentOptionsApi;
import it.gov.pagopa.pu.orgfornode.dto.generated.PaymentOptionsResponseForNode;
import it.gov.pagopa.pu.pagopapayments.service.orgfornode.PaymentOptionsForNodeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class PaymentOptionsForNodeController implements PaymentOptionsApi {

  private final PaymentOptionsForNodeService service;

  public PaymentOptionsForNodeController(PaymentOptionsForNodeService service) {
    this.service = service;
  }

  @Override
  public ResponseEntity<PaymentOptionsResponseForNode> getPaymentOptionsByNoticeNumber(String noticeNumber, String organizationFiscalCode) {
    log.info("Retrieve Payment Options that have notice number {} and organization fiscal code {}", noticeNumber, organizationFiscalCode);
    return ResponseEntity.ofNullable(service.getPaymentOptions(noticeNumber, organizationFiscalCode));
  }
}
