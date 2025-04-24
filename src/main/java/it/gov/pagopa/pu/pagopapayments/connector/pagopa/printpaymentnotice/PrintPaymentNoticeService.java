package it.gov.pagopa.pu.pagopapayments.connector.pagopa.printpaymentnotice;

import it.gov.pagopa.pu.printpaymentnotice.connector.printpaymentnotice.generated.dto.NoticeGenerationMassiveRequestDTO;
import it.gov.pagopa.pu.printpaymentnotice.connector.printpaymentnotice.generated.dto.NoticeGenerationMassiveResourceDTO;
import it.gov.pagopa.pu.printpaymentnotice.connector.printpaymentnotice.generated.dto.NoticeGenerationRequestItemDTO;

public interface PrintPaymentNoticeService {
  byte[] generateNotice(Long brokerId, NoticeGenerationRequestItemDTO noticeGenerationRequestItemDTO, String accessToken);
  NoticeGenerationMassiveResourceDTO generateNoticeMassive(Long brokerId, String idempotencyKey, NoticeGenerationMassiveRequestDTO noticeMassive, String accessToken);
}
