package it.gov.pagopa.pu.pagopapayments.connector.pagopa.paymentsreporting.config;

import it.gov.pagopa.nodo.fdrorganization.controller.ApiClient;
import it.gov.pagopa.nodo.fdrorganization.controller.auth.ApiKeyAuth;
import it.gov.pagopa.nodo.fdrorganization.controller.generated.OrganizationsApi;
import it.gov.pagopa.nodo.fdrorganization.dto.generated.ErrorMessage;
import it.gov.pagopa.nodo.fdrorganization.dto.generated.ErrorResponse;
import it.gov.pagopa.pu.pagopapayments.config.rest.HttpClientErrorHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.json.JsonMapper;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PaymentsReportingApisHolder {
  private final RestTemplate restTemplate;
  private final PaymentsReportingApiClientConfig clientConfig;

  private final Map<String, OrganizationsApi> paymentsReportingApisApiMap = new ConcurrentHashMap<>();

  public PaymentsReportingApisHolder(
    RestTemplateBuilder restTemplateBuilder,
    PaymentsReportingApiClientConfig clientConfig,
    JsonMapper jsonMapper) {
    this.restTemplate = restTemplateBuilder.build();
    this.clientConfig = clientConfig;

    restTemplate.setErrorHandler(new HttpClientErrorHandler<>(jsonMapper, "PAGOPA_PAYMENTS_REPORTING", clientConfig.isPrintBodyWhenError(),
      ErrorResponse.class, null,
      errorDTO -> Optional.ofNullable(errorDTO.getErrors())
        .map(e -> e.stream().map(ErrorMessage::getMessage).collect(Collectors.joining(",")))
        .orElse(errorDTO.getHttpStatusDescription())
    ));
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
