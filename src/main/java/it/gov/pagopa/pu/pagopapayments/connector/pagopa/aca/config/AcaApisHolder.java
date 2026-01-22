package it.gov.pagopa.pu.pagopapayments.connector.pagopa.aca.config;

import it.gov.pagopa.pu.aca.gpd.v1.controller.generated.DebtPositionsApiApi;
import it.gov.pagopa.pu.aca.gpd.v1.generated.ApiClient;
import it.gov.pagopa.pu.pagopapayments.config.rest.RestTemplateConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class AcaApisHolder {
  private final AcaApiClientConfig clientConfig;
  private final RestTemplate restTemplate;
  private final Map<String, DebtPositionsApiApi> apiMap = new ConcurrentHashMap<>();

  public AcaApisHolder(AcaApiClientConfig clientConfig, RestTemplateBuilder restTemplateBuilder) {
    this.clientConfig = clientConfig;
    this.restTemplate = restTemplateBuilder.build();

    if (clientConfig.isPrintBodyWhenError()) {
      restTemplate.setErrorHandler(RestTemplateConfig.bodyPrinterWhenError("ACA"));
    }
  }

  public DebtPositionsApiApi getApiClientByApiKey(String apiKey) {
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
