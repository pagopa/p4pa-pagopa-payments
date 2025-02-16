package it.gov.pagopa.pu.pagopapayments.connector.pagopa.aca.config;

import it.gov.pagopa.nodo.pacreateposition.controller.ApiClient;
import it.gov.pagopa.nodo.pacreateposition.controller.generated.AcaApi;
import it.gov.pagopa.pu.pagopapayments.config.RestTemplateConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class AcaApisHolder {

  private final RestTemplate restTemplate;
  private final AcaApiClientConfig clientConfig;

  private final Map<String, AcaApi> acaApiMap = new ConcurrentHashMap<>();

  public AcaApisHolder(
    AcaApiClientConfig clientConfig,
    RestTemplateBuilder restTemplateBuilder){
    this.restTemplate = restTemplateBuilder.build();
    this.clientConfig = clientConfig;

    if (clientConfig.isPrintBodyWhenError()) {
      restTemplate.setErrorHandler(RestTemplateConfig.bodyPrinterWhenError("ACA"));
    }
  }

  public AcaApi getAcaApiClientByApiKey(String apiKey) {
    return acaApiMap.computeIfAbsent(apiKey, key -> {
      ApiClient apiClient = new ApiClient(restTemplate);
      apiClient.setBasePath(clientConfig.getBaseUrl());
      apiClient.setApiKey(key);
      apiClient.setMaxAttemptsForRetry(Math.max(1, clientConfig.getMaxAttempts()));
      apiClient.setWaitTimeMillis(clientConfig.getWaitTimeMillis());
      return new AcaApi(apiClient);
    });
  }

}
