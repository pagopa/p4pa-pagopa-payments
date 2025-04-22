package it.gov.pagopa.pu.pagopapayments.connector.pagopa.printpaymentnotice;

import it.gov.pagopa.pu.printpaymentnotice.connector.printpaymentnotice.generated.dto.NoticeGenerationRequestItemDTO;

public interface PrintPaymentNoticeService {
  byte[] generateNotice(Long brokerId, NoticeGenerationRequestItemDTO noticeGenerationRequestItemDTO, String accessToken);
}
