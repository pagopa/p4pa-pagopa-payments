package it.gov.pagopa.pu.pagopapayments.connector.pagopa.printpaymentnotice;

import it.gov.pagopa.nodo.printpaymentnotice.dto.generated.*;

public interface PrintPaymentNoticeService {
  byte[] generateNotice(Long brokerId, NoticeGenerationRequestItemDTO noticeGenerationRequestItemDTO, String accessToken);
  NoticeGenerationMassiveResourceDTO generateNoticeMassive(Long brokerId, String idempotencyKey, NoticeGenerationMassiveRequestDTO noticeMassive, String accessToken);
  GetGenerationRequestStatusResourceDTO getFolderStatus(Long brokerId, String folderId, String accessToken);
  GetSignedUrlResourceDTO getFolderSignedUrlResource(Long brokerId, String folderId, String accessToken);
}
