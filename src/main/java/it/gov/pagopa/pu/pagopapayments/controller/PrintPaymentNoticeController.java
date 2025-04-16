package it.gov.pagopa.pu.pagopapayments.controller;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.pagopapayments.controller.generated.PrintPaymentNoticeApi;
import it.gov.pagopa.pu.pagopapayments.service.printpaymentnotice.GenerateNoticeService;
import it.gov.pagopa.pu.pagopapayments.util.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
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
  public ResponseEntity<Resource> generateNotice(Long organizationId, String taxCode, String iuv, DebtPositionDTO debtPosition) {
    log.info("invoking generateNotice, organizationId[{}], taxCode[{}], iuv[{}] debtPositionDTO[{}]", organizationId, taxCode, iuv, debtPosition);
    File result = generateNoticeService.generateNotice(organizationId, taxCode, iuv, debtPosition, SecurityUtils.getAccessToken());
    return null;
  }
}
