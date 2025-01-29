package it.gov.pagopa.pu.pagopapayments.controller;

import it.gov.pagopa.pu.pagopapayments.controller.generated.ReportingApi;
import it.gov.pagopa.pu.pagopapayments.dto.generated.ReportingIdDTO;
import it.gov.pagopa.pu.pagopapayments.service.paymentsreporting.PaymentsReportingService;
import it.gov.pagopa.pu.pagopapayments.util.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
public class PaymentsReportingController implements ReportingApi {

  private final PaymentsReportingService paymentsReportingService;

  public PaymentsReportingController(PaymentsReportingService paymentsReportingService) {
    this.paymentsReportingService = paymentsReportingService;
  }

  @Override
  public ResponseEntity<List<ReportingIdDTO>> getPaymentsReportingList(@PathVariable Long organizationId) {
    log.info("User requested getPaymentsReportingList, organizationId[{}]", organizationId);
    List<ReportingIdDTO> reportingList = paymentsReportingService.getReportingList(organizationId, SecurityUtils.getAccessToken());
    return ResponseEntity.ok(reportingList);
  }



}
