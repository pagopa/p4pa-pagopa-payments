package it.gov.pagopa.pu.pagopapayments.controller;

import it.gov.pagopa.pu.pagopapayments.controller.generated.PaymentsReportingApi;
import it.gov.pagopa.pu.pagopapayments.dto.generated.PaymentsReportingIdDTO;
import it.gov.pagopa.pu.pagopapayments.service.paymentsreporting.PaymentsReportingFacadeService;
import it.gov.pagopa.pu.pagopapayments.util.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@Slf4j
public class PaymentsReportingController implements PaymentsReportingApi {

  private final PaymentsReportingFacadeService paymentsReportingFacadeService;

  public PaymentsReportingController(
    PaymentsReportingFacadeService paymentsReportingFacadeService) {
    this.paymentsReportingFacadeService = paymentsReportingFacadeService;
  }

  @Override
  public ResponseEntity<List<PaymentsReportingIdDTO>> getPaymentsReportingList(Long organizationId, OffsetDateTime latestFlowDate) {
    log.info("User requested getPaymentsReportingList, organizationId[{}], latestFlowDate[{}]", organizationId, latestFlowDate);
    List<PaymentsReportingIdDTO> reportingList = paymentsReportingFacadeService.getPaymentsReportingList(organizationId, latestFlowDate, SecurityUtils.getAccessToken());
    return ResponseEntity.ok(reportingList);
  }

  @Override
  public ResponseEntity<Long> fetchPaymentReporting(Long organizationId, String flowId, String fileName, Long revision, String pspId){
    log.info("User requested fetchPaymentReporting, organizationId[{}], flowId[{}], revision[{}], pspId[{}], fileName[{}]", organizationId, flowId, revision, pspId, fileName);
    Long result = paymentsReportingFacadeService.fetchPaymentReporting(organizationId, flowId, revision, pspId, fileName, SecurityUtils.getAccessToken());
    return ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).body(result);
  }

}
