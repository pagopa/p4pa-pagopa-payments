package it.gov.pagopa.pu.pagopapayments.controller;

import it.gov.pagopa.pu.pagopapayments.controller.generated.PaymentsReportingApi;
import it.gov.pagopa.pu.pagopapayments.dto.generated.PaymentsReportingIdDTO;
import it.gov.pagopa.pu.pagopapayments.service.paymentsreporting.PaymentsReportingService;
import it.gov.pagopa.pu.pagopapayments.util.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
public class PaymentsReportingController implements PaymentsReportingApi {

  private final PaymentsReportingService paymentsReportingService;

  public PaymentsReportingController(PaymentsReportingService paymentsReportingService) {
    this.paymentsReportingService = paymentsReportingService;
  }

  @Override
  public ResponseEntity<List<PaymentsReportingIdDTO>> getPaymentsReportingList(@PathVariable Long organizationId) {
    log.info("User requested getPaymentsReportingList, organizationId[{}]", organizationId);
    List<PaymentsReportingIdDTO> reportingList = paymentsReportingService.getPaymentsReportingList(organizationId, SecurityUtils.getAccessToken());
    return ResponseEntity.ok(reportingList);
  }

  @Override
  public ResponseEntity<Long> fetchPaymentReporting(@PathVariable Long organizationId, @RequestParam String flowId){
    log.info("invoking uploadPaymentsReporting, organizationId[{}], flowId[{}]", organizationId, flowId);
    Long result = paymentsReportingService.fetchPaymentReporting(organizationId, flowId, SecurityUtils.getAccessToken());
    return ResponseEntity.ok(result);
  }

}
