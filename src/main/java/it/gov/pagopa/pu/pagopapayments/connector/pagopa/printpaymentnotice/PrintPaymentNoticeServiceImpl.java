package it.gov.pagopa.pu.pagopapayments.connector.pagopa.printpaymentnotice;

import it.gov.pagopa.pu.pagopapayments.connector.pagopa.printpaymentnotice.client.PrintPaymentNoticeClient;
import it.gov.pagopa.pu.printpaymentnotice.connector.printpaymentnotice.generated.dto.NoticeGenerationMassiveRequestDTO;
import it.gov.pagopa.pu.printpaymentnotice.connector.printpaymentnotice.generated.dto.NoticeGenerationMassiveResourceDTO;
import it.gov.pagopa.pu.printpaymentnotice.connector.printpaymentnotice.generated.dto.NoticeGenerationRequestItemDTO;
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
}
