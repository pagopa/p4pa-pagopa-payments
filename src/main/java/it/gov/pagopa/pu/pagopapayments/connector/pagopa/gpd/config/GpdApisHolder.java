package it.gov.pagopa.pu.pagopapayments.connector.pagopa.gpd.config;

import it.gov.pagopa.nodo.gpd.client.generated.DebtPositionsApiInstallmentsAndPaymentOptionsManagerApi;
import it.gov.pagopa.nodo.gpd.dto.generated.ProblemJson;
import it.gov.pagopa.nodo.gpd.generated.ApiClient;
import it.gov.pagopa.nodo.gpd.generated.auth.ApiKeyAuth;
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
public class GpdApisHolder {
  private final GpdApiClientConfig clientConfig;
  private final RestTemplate restTemplate;
  private final Map<String, DebtPositionsApiInstallmentsAndPaymentOptionsManagerApi> apiMap = new ConcurrentHashMap<>();

  public GpdApisHolder(GpdApiClientConfig clientConfig, RestTemplateBuilder restTemplateBuilder, JsonMapper jsonMapper) {
    this.clientConfig = clientConfig;
    this.restTemplate = restTemplateBuilder.build();

    restTemplate.setErrorHandler(new HttpClientErrorJsonBodyHandler<>(jsonMapper, "NODO-GPD", clientConfig.isPrintBodyWhenError(),
      ProblemJson.class, null, ProblemJson::getDetail));
  }

  public DebtPositionsApiInstallmentsAndPaymentOptionsManagerApi getDebtPositionsApiInstallmentsAndPaymentOptionsManagerApi(String apiKey) {
    return apiMap.computeIfAbsent(apiKey, key ->
      new DebtPositionsApiInstallmentsAndPaymentOptionsManagerApi(buildApiClient(key)));
  }

  private ApiClient buildApiClient(String apiKey) {
    ApiClient apiClient = new ApiClient(restTemplate);
    apiClient.setBasePath(clientConfig.getBaseUrl());
    ((ApiKeyAuth)apiClient.getAuthentication("apiKeyHeader")).setApiKey(apiKey);
    apiClient.setMaxAttemptsForRetry(Math.max(1, clientConfig.getMaxAttempts()));
    apiClient.setWaitTimeMillis(clientConfig.getWaitTimeMillis());
    return apiClient;
  }
}
