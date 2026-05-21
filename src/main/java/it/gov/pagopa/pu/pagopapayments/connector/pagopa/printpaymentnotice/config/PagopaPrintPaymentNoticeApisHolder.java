package it.gov.pagopa.pu.pagopapayments.connector.pagopa.printpaymentnotice.config;

import it.gov.pagopa.pu.pagopapayments.config.rest.HttpClientErrorJsonBodyHandler;
import it.gov.pagopa.pu.printpaymentnotice.connector.printpaymentnotice.generated.ApiClient;
import it.gov.pagopa.pu.printpaymentnotice.connector.printpaymentnotice.generated.api.NoticeGenerationRequestApisApi;
import it.gov.pagopa.pu.printpaymentnotice.connector.printpaymentnotice.generated.dto.ProblemJsonDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.json.JsonMapper;

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
    RestTemplateBuilder restTemplateBuilder,
    JsonMapper jsonMapper){
    this.restTemplate = restTemplateBuilder.build();
    this.clientConfig = clientConfig;

    restTemplate.setErrorHandler(new HttpClientErrorJsonBodyHandler<>(jsonMapper, "PAGOPA_PRINT_PAYMENT_NOTICE", clientConfig.isPrintBodyWhenError(),
      ProblemJsonDTO.class, null, ProblemJsonDTO::getDetail));
  }

  public NoticeGenerationRequestApisApi getNoticeGenerationRequestApi(String apiKey) {
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
