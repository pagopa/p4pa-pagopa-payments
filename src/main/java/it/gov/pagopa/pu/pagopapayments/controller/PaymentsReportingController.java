package it.gov.pagopa.pu.pagopapayments.controller;

import it.gov.pagopa.pu.pagopapayments.controller.generated.PaymentsReportingApi;
import it.gov.pagopa.pu.pagopapayments.dto.generated.PaymentsReportingIdDTO;
import it.gov.pagopa.pu.pagopapayments.service.paymentsreporting.PaymentsReportingRestService;
import it.gov.pagopa.pu.pagopapayments.service.paymentsreporting.PaymentsReportingSoapService;
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

  private final PaymentsReportingSoapService paymentsReportingSoapService;
  private final PaymentsReportingRestService paymentsReportingRestService;

  public PaymentsReportingController(
    PaymentsReportingSoapService paymentsReportingSoapService,
    PaymentsReportingRestService paymentsReportingRestService) {
    this.paymentsReportingSoapService = paymentsReportingSoapService;
    this.paymentsReportingRestService = paymentsReportingRestService;
  }

  @Override
  public ResponseEntity<List<PaymentsReportingIdDTO>> soapGetPaymentsReportingList(Long organizationId) {
    log.info("User requested getPaymentsReportingList, organizationId[{}]", organizationId);
    List<PaymentsReportingIdDTO> reportingList = paymentsReportingSoapService.getPaymentsReportingList(organizationId, SecurityUtils.getAccessToken());
    return ResponseEntity.ok(reportingList);
  }

  @Override
  public ResponseEntity<Long> soapFetchPaymentReporting(Long organizationId, String flowId, String fileName){
    log.info("invoking uploadPaymentsReporting, organizationId[{}], flowId[{}], fileName[{}]", organizationId, flowId, fileName);
    Long result = paymentsReportingSoapService.fetchPaymentReporting(organizationId, flowId, fileName, SecurityUtils.getAccessToken());
    return ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).body(result);
  }

  @Override
  public ResponseEntity<List<PaymentsReportingIdDTO>> restGetPaymentsReportingList(Long organizationId, OffsetDateTime latestFlowDate) {
    log.info("User requested getPaymentsReportingList, organizationId[{}], latestFlowDate[{}]", organizationId, latestFlowDate);
    List<PaymentsReportingIdDTO> reportingList = paymentsReportingRestService.getPaymentsReportingList(organizationId, latestFlowDate, SecurityUtils.getAccessToken());
    return ResponseEntity.ok(reportingList);
  }

  @Override
  public ResponseEntity<Long> restFetchPaymentReporting(Long organizationId, String flowId, String fileName, Long revision, String pspId){
    log.info("invoking uploadPaymentsReporting, organizationId[{}], flowId[{}], revision[{}], pspId[{}], fileName[{}]", organizationId, flowId, revision, pspId, fileName);
    Long result = paymentsReportingRestService.fetchPaymentReporting(organizationId, flowId, revision, pspId, fileName, SecurityUtils.getAccessToken());
    return ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).body(result);
  }

}
