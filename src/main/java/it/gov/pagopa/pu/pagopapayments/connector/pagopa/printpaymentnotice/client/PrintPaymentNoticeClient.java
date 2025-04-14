package it.gov.pagopa.pu.pagopapayments.connector.pagopa.printpaymentnotice.client;

import it.gov.pagopa.pu.organization.dto.generated.BrokerApiKeyType;
import it.gov.pagopa.pu.pagopapayments.connector.organization.BrokerService;
import it.gov.pagopa.pu.pagopapayments.connector.pagopa.printpaymentnotice.config.PagopaPrintPaymentNoticeApisHolder;
import it.gov.pagopa.pu.pagopapayments.util.SecurityUtils;
import it.gov.pagopa.pu.printpaymentnotice.connector.printpaymentnotice.generated.dto.NoticeGenerationRequestItemDTO;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class PrintPaymentNoticeClient {

  private final PagopaPrintPaymentNoticeApisHolder apisHolder;
  private final BrokerService brokerService;

  public PrintPaymentNoticeClient(PagopaPrintPaymentNoticeApisHolder apisHolder, BrokerService brokerService) {
    this.apisHolder = apisHolder;
    this.brokerService = brokerService;
  }

  public File generateNotice(Long brokerId, BrokerApiKeyType brokerApiKeyType, NoticeGenerationRequestItemDTO noticeGenerationRequestItemDTO) {
    String apiKey = getApiKeyFromBroker(brokerId, brokerApiKeyType);
    return apisHolder.getNoticeGenerationRequestApisApiMap(apiKey)
      .generateNotice(noticeGenerationRequestItemDTO, null, null);
  }

  private String getApiKeyFromBroker(Long brokerId, BrokerApiKeyType brokerApiKeyType) {
    String accessToken = SecurityUtils.getAccessToken();
    return brokerService.getBrokerApiKey(brokerId, brokerApiKeyType, accessToken);
  }

}
