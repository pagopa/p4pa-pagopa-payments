package it.gov.pagopa.pu.pagopapayments.controller;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.pagopapayments.controller.generated.PrintPaymentNoticeApi;
import it.gov.pagopa.pu.pagopapayments.dto.NoticeDataDTO;
import it.gov.pagopa.pu.pagopapayments.dto.generated.GeneratedNoticeMassiveFolderDTO;
import it.gov.pagopa.pu.pagopapayments.dto.generated.NoticeRequestMassiveDTO;
import it.gov.pagopa.pu.pagopapayments.dto.generated.SignedUrlResultDTO;
import it.gov.pagopa.pu.pagopapayments.service.printpaymentnotice.GenerateNoticeService;
import it.gov.pagopa.pu.pagopapayments.util.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class PrintPaymentNoticeController implements PrintPaymentNoticeApi {

  private final GenerateNoticeService generateNoticeService;

  public PrintPaymentNoticeController(GenerateNoticeService generateNoticeService) {
    this.generateNoticeService = generateNoticeService;
  }

  @Override
  public ResponseEntity<Resource> generateNotice(String nav, DebtPositionDTO debtPosition) {
    log.info("invoking generateNotice, nav[{}], debtPositionId[{}]", nav, debtPosition.getDebtPositionId());
    NoticeDataDTO notice = generateNoticeService.generateNotice(nav, debtPosition, SecurityUtils.getAccessToken());

    Resource resource = new ByteArrayResource(notice.getNotice());

    HttpHeaders headers = new HttpHeaders();
    headers.setContentDisposition(ContentDisposition.attachment()
      .filename(notice.getFileName())
      .build());

    return ResponseEntity.ok()
      .contentType(MediaType.APPLICATION_PDF)
      .headers(headers)
      .body(resource);

  }

  @Override
  public ResponseEntity<GeneratedNoticeMassiveFolderDTO> generateMassive(NoticeRequestMassiveDTO noticeRequestMassiveDTO) {
    log.info("invoking generateMassive, requestId[{}]", noticeRequestMassiveDTO.getRequestId());
    GeneratedNoticeMassiveFolderDTO noticeMassive = generateNoticeService.generateNoticeMassive(noticeRequestMassiveDTO, SecurityUtils.getAccessToken());

    return ResponseEntity.ok(noticeMassive);
  }

  @Override
  public ResponseEntity<SignedUrlResultDTO> getSignedUrl(Long organizationId, String folderId) {
    log.info("invoking getSignedUrl, organizationId[{}], folderId[{}]", organizationId, folderId);
    SignedUrlResultDTO response = generateNoticeService.getNoticeMassiveZip(organizationId, folderId, SecurityUtils.getAccessToken());
    if (response != null) {
      return new ResponseEntity<>(response, HttpStatus.OK);
    }
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

}
