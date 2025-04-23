package it.gov.pagopa.pu.pagopapayments.connector.pagopa.printpaymentnotice.client;

import it.gov.pagopa.pu.organization.dto.generated.BrokerApiKeyType;
import it.gov.pagopa.pu.pagopapayments.connector.organization.BrokerService;
import it.gov.pagopa.pu.pagopapayments.connector.pagopa.printpaymentnotice.config.PagopaPrintPaymentNoticeApisHolder;
import it.gov.pagopa.pu.printpaymentnotice.connector.printpaymentnotice.generated.dto.NoticeGenerationMassiveRequestDTO;
import it.gov.pagopa.pu.printpaymentnotice.connector.printpaymentnotice.generated.dto.NoticeGenerationMassiveResourceDTO;
import it.gov.pagopa.pu.printpaymentnotice.connector.printpaymentnotice.generated.dto.NoticeGenerationRequestItemDTO;
import org.springframework.stereotype.Service;

@Service
public class PrintPaymentNoticeClient {

  private final PagopaPrintPaymentNoticeApisHolder apisHolder;
  private final BrokerService brokerService;

  public PrintPaymentNoticeClient(PagopaPrintPaymentNoticeApisHolder apisHolder, BrokerService brokerService) {
    this.apisHolder = apisHolder;
    this.brokerService = brokerService;
  }

  private String getApiKeyFromBroker(Long brokerId, String accessToken) {
    return brokerService.getBrokerApiKey(brokerId, BrokerApiKeyType.GENERATE_NOTICE, accessToken);
  }

  public byte[] generateNotice(Long brokerId, NoticeGenerationRequestItemDTO noticeGenerationRequestItemDTO, String accessToken) {
    String apiKey = getApiKeyFromBroker(brokerId, accessToken);
    return apisHolder.getNoticeGenerationRequestApisApiMap(apiKey)
      .generateNotice(noticeGenerationRequestItemDTO, null, null);
  }

  public NoticeGenerationMassiveResourceDTO generateNoticeMassive(Long brokerId, String idempotencyKey, NoticeGenerationMassiveRequestDTO noticeMassive, String accessToken) {
    String apiKey = getApiKeyFromBroker(brokerId, accessToken);
    return apisHolder.getNoticeGenerationRequestApisApiMap(apiKey)
      .generateNoticeMassiveRequest(idempotencyKey, noticeMassive, null);
  }
}
