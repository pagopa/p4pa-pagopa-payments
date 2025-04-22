package it.gov.pagopa.pu.pagopapayments.controller;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.pagopapayments.controller.generated.PrintPaymentNoticeApi;
import it.gov.pagopa.pu.pagopapayments.service.printpaymentnotice.GenerateNoticeService;
import it.gov.pagopa.pu.pagopapayments.util.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;

@RestController
@Slf4j
public class PrintPaymentNoticeController implements PrintPaymentNoticeApi {

  private final GenerateNoticeService generateNoticeService;

  public PrintPaymentNoticeController(GenerateNoticeService generateNoticeService){
    this.generateNoticeService = generateNoticeService;
  }

  @Override
  public ResponseEntity<Resource> generateNotice(Long organizationId, String iuv, DebtPositionDTO debtPosition) {
    log.info("invoking generateNotice, organizationId[{}], iuv[{}] debtPositionId[{}]", organizationId, iuv, debtPosition.getDebtPositionId());
    File notice = generateNoticeService.generateNotice(organizationId, iuv, debtPosition, SecurityUtils.getAccessToken());

    Resource resource = new FileSystemResource(notice);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentDisposition(ContentDisposition.attachment()
      .filename(notice.getName())
      .build());

    return ResponseEntity.ok()
      .contentType(MediaType.APPLICATION_OCTET_STREAM)
      .headers(headers)
      .body(resource);
  }
}
