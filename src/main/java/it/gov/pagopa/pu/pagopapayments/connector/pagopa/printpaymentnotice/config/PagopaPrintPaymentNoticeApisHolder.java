package it.gov.pagopa.pu.pagopapayments.connector.pagopa.printpaymentnotice.config;

import it.gov.pagopa.pu.pagopapayments.config.RestTemplateConfig;
import it.gov.pagopa.pu.printpaymentnotice.connector.printpaymentnotice.generated.ApiClient;
import it.gov.pagopa.pu.printpaymentnotice.connector.printpaymentnotice.generated.api.NoticeGenerationRequestApisApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class PagopaPrintPaymentNoticeApisHolder {

  private final RestTemplate restTemplate;
  private final PagopaPrintPaymentNoticeApiClientConfig clientConfig;

  private final Map<String, NoticeGenerationRequestApisApi> noticeGenerationRequestApisApiMap = new ConcurrentHashMap<>();

  public PagopaPrintPaymentNoticeApisHolder(
    PagopaPrintPaymentNoticeApiClientConfig clientConfig,
    RestTemplateBuilder restTemplateBuilder){
    this.restTemplate = restTemplateBuilder.build();
    this.clientConfig = clientConfig;

    if (clientConfig.isPrintBodyWhenError()) {
      restTemplate.setErrorHandler(RestTemplateConfig.bodyPrinterWhenError("SEND"));
    }
  }

  public NoticeGenerationRequestApisApi getNoticeGenerationRequestApisApiMap(String apiKey) {
    return noticeGenerationRequestApisApiMap.computeIfAbsent(apiKey, key ->
      new NoticeGenerationRequestApisApi(buildApiClient(key)));
  }

  private ApiClient buildApiClient(String apiKey) {
    ApiClient apiClient = new ApiClient(restTemplate);
    apiClient.setBasePath(clientConfig.getBaseUrl());
    apiClient.setApiKey(apiKey);
    apiClient.setMaxAttemptsForRetry(Math.max(1, clientConfig.getMaxAttempts()));
    apiClient.setWaitTimeMillis(clientConfig.getWaitTimeMillis());
    return apiClient;
  }
}
