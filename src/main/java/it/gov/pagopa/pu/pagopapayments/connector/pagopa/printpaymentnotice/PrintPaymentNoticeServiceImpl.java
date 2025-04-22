package it.gov.pagopa.pu.pagopapayments.connector.pagopa.printpaymentnotice;

import it.gov.pagopa.pu.pagopapayments.connector.pagopa.printpaymentnotice.client.PrintPaymentNoticeClient;
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
}
