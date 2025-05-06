package it.gov.pagopa.pu.pagopapayments.connector.pagopa.printpaymentnotice;

import it.gov.pagopa.pu.printpaymentnotice.connector.printpaymentnotice.generated.dto.*;

public interface PrintPaymentNoticeService {
  byte[] generateNotice(Long brokerId, NoticeGenerationRequestItemDTO noticeGenerationRequestItemDTO, String accessToken);
  NoticeGenerationMassiveResourceDTO generateNoticeMassive(Long brokerId, String idempotencyKey, NoticeGenerationMassiveRequestDTO noticeMassive, String accessToken);
  GetGenerationRequestStatusResourceDTO getFolderStatus(Long brokerId, String folderId, String accessToken);
  GetSignedUrlResourceDTO getFolderSignedUrlResource(Long brokerId, String folderId, String accessToken);
}
