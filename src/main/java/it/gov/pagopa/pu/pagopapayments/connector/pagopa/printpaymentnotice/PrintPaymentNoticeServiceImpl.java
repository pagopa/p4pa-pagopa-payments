package it.gov.pagopa.pu.pagopapayments.connector.pagopa.printpaymentnotice;

import it.gov.pagopa.pu.pagopapayments.connector.pagopa.printpaymentnotice.client.PrintPaymentNoticeClient;
import it.gov.pagopa.pu.printpaymentnotice.connector.printpaymentnotice.generated.dto.*;
import org.springframework.stereotype.Service;

@Service
public class PrintPaymentNoticeServiceImpl implements PrintPaymentNoticeService {
  private final PrintPaymentNoticeClient printPaymentNoticeClient;

  public PrintPaymentNoticeServiceImpl(PrintPaymentNoticeClient printPaymentNoticeClient) {
    this.printPaymentNoticeClient = printPaymentNoticeClient;
  }

  @Override
  public byte[] generateNotice(Long brokerId, NoticeGenerationRequestItemDTO noticeGenerationRequestItemDTO, String accessToken) {
    return printPaymentNoticeClient.generateNotice(brokerId, noticeGenerationRequestItemDTO, accessToken);
  }

  @Override
  public NoticeGenerationMassiveResourceDTO generateNoticeMassive(Long brokerId, String idempotencyKey, NoticeGenerationMassiveRequestDTO noticeMassive, String accessToken) {
    return printPaymentNoticeClient.generateNoticeMassive(brokerId, idempotencyKey, noticeMassive, accessToken);
  }

  @Override
  public GetGenerationRequestStatusResourceDTO getFolderStatus(Long brokerId, String folderId, String accessToken) {
    return printPaymentNoticeClient.getFolderStatus(brokerId, folderId, accessToken);
  }

  @Override
  public GetSignedUrlResourceDTO getFolderSignedUrlResource(Long brokerId, String folderId, String accessToken) {
    return printPaymentNoticeClient.getFolderSignedUrlResource(brokerId, folderId, accessToken);
  }
}
