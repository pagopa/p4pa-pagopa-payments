package it.gov.pagopa.pu.pagopapayments.connector.pagopa.gpd.config;

import it.gov.pagopa.nodo.gpd.controller.ApiClient;
import it.gov.pagopa.nodo.gpd.controller.generated.DebtPositionsApiApi;
import it.gov.pagopa.pu.pagopapayments.config.rest.RestTemplateConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class GpdApisHolder {

  private final RestTemplate restTemplate;
  private final GpdApiClientConfig clientConfig;

  private final Map<String, DebtPositionsApiApi> gpdApiMap = new ConcurrentHashMap<>();

  public GpdApisHolder(
    GpdApiClientConfig clientConfig,
    RestTemplateBuilder restTemplateBuilder){
    this.restTemplate = restTemplateBuilder.build();
    this.clientConfig = clientConfig;

    if (clientConfig.isPrintBodyWhenError()) {
      restTemplate.setErrorHandler(RestTemplateConfig.bodyPrinterWhenError("ACA"));
    }
  }

  public DebtPositionsApiApi getGpdApiClientByApiKey(String apiKey) {
    return gpdApiMap.computeIfAbsent(apiKey, key -> {
      ApiClient apiClient = new ApiClient(restTemplate);
      apiClient.setBasePath(clientConfig.getBaseUrl());
      apiClient.setApiKey(key);
      apiClient.setMaxAttemptsForRetry(Math.max(1, clientConfig.getMaxAttempts()));
      apiClient.setWaitTimeMillis(clientConfig.getWaitTimeMillis());
      return new DebtPositionsApiApi(apiClient);
    });
  }

}
