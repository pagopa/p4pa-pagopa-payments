package it.gov.pagopa.pu.pagopapayments.connector.pagopa.aca.config;

import it.gov.pagopa.pu.aca.client.generated.DebtPositionsApiApi;
import it.gov.pagopa.pu.aca.dto.generated.ProblemJson;
import it.gov.pagopa.pu.aca.generated.ApiClient;
import it.gov.pagopa.pu.pagopapayments.config.rest.HttpClientErrorJsonBodyHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.json.JsonMapper;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class AcaApisHolder {
  private final AcaApiClientConfig clientConfig;
  private final RestTemplate restTemplate;
  private final Map<String, DebtPositionsApiApi> apiMap = new ConcurrentHashMap<>();

  public AcaApisHolder(AcaApiClientConfig clientConfig, RestTemplateBuilder restTemplateBuilder, JsonMapper jsonMapper) {
    this.clientConfig = clientConfig;
    this.restTemplate = restTemplateBuilder.build();

    restTemplate.setErrorHandler(new HttpClientErrorJsonBodyHandler<>(jsonMapper, "ACA", clientConfig.isPrintBodyWhenError(),
      ProblemJson.class, null, ProblemJson::getDetail));
  }

  public DebtPositionsApiApi getDebtPositionsApi(String apiKey) {
    return apiMap.computeIfAbsent(apiKey, key ->
      new DebtPositionsApiApi(buildApiClient(key)));
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
