package it.gov.pagopa.pu.pagopapayments.controller;

import it.gov.pagopa.pu.pagopapayments.controller.generated.ReportingApi;
import it.gov.pagopa.pu.pagopapayments.dto.generated.ReportingIdDTO;
import it.gov.pagopa.pu.pagopapayments.service.reporting.ReportingService;
import it.gov.pagopa.pu.pagopapayments.util.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
public class ReportingController implements ReportingApi {


  private final ReportingService reportingService;

  public ReportingController(ReportingService reportingService) {
    this.reportingService = reportingService;
  }

  @Override
  public ResponseEntity<List<ReportingIdDTO>> getReportingList(@PathVariable Long organizationId) {
    log.info("invoking getReportingList, organizationId[{}]", organizationId);
    List<ReportingIdDTO> reportingList = reportingService.getReportingList(organizationId, SecurityUtils.getAccessToken());
    return ResponseEntity.ok(reportingList);
  }
}
