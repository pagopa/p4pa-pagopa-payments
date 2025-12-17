package it.gov.pagopa.pu.pagopapayments.connector.pagopa.aca_gpd;

import it.gov.pagopa.nodo.gpd.controller.ApiClient;
import it.gov.pagopa.nodo.gpd.controller.generated.DebtPositionsApiApi;
import it.gov.pagopa.pu.pagopapayments.config.rest.RestTemplateConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public abstract class BaseApisHolder implements ApiClientProvider {

  private final RestTemplate restTemplate;
  private final String baseUrl;
  private final int maxAttempts;
  private final long waitTimeMillis;

  private final Map<String, DebtPositionsApiApi> apiMap = new ConcurrentHashMap<>();

  protected BaseApisHolder(
      RestTemplateBuilder restTemplateBuilder,
      String baseUrl,
      int maxAttempts,
      long waitTimeMillis,
      boolean printBodyWhenError,
      String serviceName
  ) {
    this.restTemplate = restTemplateBuilder.build();
    this.baseUrl = baseUrl;
    this.maxAttempts = maxAttempts;
    this.waitTimeMillis = waitTimeMillis;

    if (printBodyWhenError) {
      this.restTemplate.setErrorHandler(RestTemplateConfig.bodyPrinterWhenError(serviceName));
    }
  }

  @Override
  public DebtPositionsApiApi getApiClientByApiKey(String apiKey) {
    return apiMap.computeIfAbsent(apiKey, key -> {
      ApiClient apiClient = new ApiClient(restTemplate);
      apiClient.setBasePath(baseUrl);
      apiClient.setApiKey(key);
      apiClient.setMaxAttemptsForRetry(Math.max(1, maxAttempts));
      apiClient.setWaitTimeMillis(waitTimeMillis);
      return new DebtPositionsApiApi(apiClient);
    });
  }
}
