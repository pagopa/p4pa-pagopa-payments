package it.gov.pagopa.pu.pagopapayments.connector.pagopa.printpaymentnotice.config;

import it.gov.pagopa.pu.pagopapayments.config.rest.HttpClientErrorJsonBodyHandler;
import it.gov.pagopa.nodo.printpaymentnotice.generated.ApiClient;
import it.gov.pagopa.nodo.printpaymentnotice.client.generated.NoticeGenerationRequestApisApi;
import it.gov.pagopa.nodo.printpaymentnotice.dto.generated.ProblemJsonDTO;
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

    restTemplate.setErrorHandler(new HttpClientErrorJsonBodyHandler<>(jsonMapper, "NODO-PRINT-PAYMENT-NOTICE", clientConfig.isPrintBodyWhenError(),
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
