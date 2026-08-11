package it.gov.pagopa.pu.pagopapayments.connector.pagopa.printpaymentnotice;

import it.gov.pagopa.pu.pagopapayments.connector.pagopa.printpaymentnotice.client.PrintPaymentNoticeClient;
import it.gov.pagopa.nodo.printpaymentnotice.dto.generated.*;
import org.springframework.stereotype.Service;

@Service
public class PrintPaymentNoticeServiceImpl implements PrintPaymentNoticeService {
  private final PrintPaymentNoticeClient printPaymentNoticeClient;

  public PrintPaymentNoticeServiceImpl(PrintPaymentNoticeClient printPaymentNoticeClient) {
    this.printPaymentNoticeClient = printPaymentNoticeClient;
  }

  @Override
  public byte[] generateNotice(Long organizationId, NoticeGenerationRequestItemDTO noticeGenerationRequestItemDTO, String accessToken) {
    return printPaymentNoticeClient.generateNotice(organizationId, noticeGenerationRequestItemDTO, accessToken);
  }

  @Override
  public NoticeGenerationMassiveResourceDTO generateNoticeMassive(Long organizationId, String idempotencyKey, NoticeGenerationMassiveRequestDTO noticeMassive, String accessToken) {
    return printPaymentNoticeClient.generateNoticeMassive(organizationId, idempotencyKey, noticeMassive, accessToken);
  }

  @Override
  public GetGenerationRequestStatusResourceDTO getFolderStatus(Long organizationId, String folderId, String accessToken) {
    return printPaymentNoticeClient.getFolderStatus(organizationId, folderId, accessToken);
  }

  @Override
  public GetSignedUrlResourceDTO getFolderSignedUrlResource(Long organizationId, String folderId, String accessToken) {
    return printPaymentNoticeClient.getFolderSignedUrlResource(organizationId, folderId, accessToken);
  }
}
