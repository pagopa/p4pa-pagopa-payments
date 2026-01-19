package it.gov.pagopa.pu.pagopapayments.connector.pagopa.paymentsreporting.config;

import it.gov.pagopa.nodo.fdrorganization.controller.ApiClient;
import it.gov.pagopa.nodo.fdrorganization.controller.auth.ApiKeyAuth;
import it.gov.pagopa.nodo.fdrorganization.controller.generated.OrganizationsApi;
import it.gov.pagopa.pu.pagopapayments.config.rest.RestTemplateConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class NodePaymentsReportingApisHolder {
  private final RestTemplate restTemplate;
  private final NodePaymentsReportingApiClientConfig clientConfig;

  private final Map<String, OrganizationsApi> paymentsReportingApisApiMap = new ConcurrentHashMap<>();

  public NodePaymentsReportingApisHolder(
    RestTemplateBuilder restTemplateBuilder,
    NodePaymentsReportingApiClientConfig clientConfig) {
    this.restTemplate = restTemplateBuilder.build();
    this.clientConfig = clientConfig;

    if (clientConfig.isPrintBodyWhenError()) {
      restTemplate.setErrorHandler(RestTemplateConfig.bodyPrinterWhenError("NODE-SYNC-PAYMENTS-REPORTING"));
    }
  }

  public OrganizationsApi getOrganizationApiByApiKey(String apiKey) {
    return paymentsReportingApisApiMap.computeIfAbsent(
      apiKey,
      key -> new OrganizationsApi(buildApiClient(key))
    );
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
