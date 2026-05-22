package it.gov.pagopa.pu.pagopapayments.connector.pagopa.paymentsreporting.config;

import it.gov.pagopa.nodo.fdrorganization.controller.ApiClient;
import it.gov.pagopa.nodo.fdrorganization.controller.auth.ApiKeyAuth;
import it.gov.pagopa.nodo.fdrorganization.controller.generated.OrganizationsApi;
import it.gov.pagopa.nodo.fdrorganization.dto.generated.ErrorMessage;
import it.gov.pagopa.nodo.fdrorganization.dto.generated.ErrorResponse;
import it.gov.pagopa.pu.pagopapayments.config.json.LocalDateDeserializerWithFallbackOnDateTime;
import it.gov.pagopa.pu.pagopapayments.config.rest.HttpClientErrorJsonBodyHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;

import java.time.LocalDate;
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
    this.clientConfig = clientConfig;

    SimpleModule localDateDeserializerWithFallbackOnDateTimeModule = new SimpleModule("LocalDateDeserializerWithFallbackOnDateTimeModule");
    localDateDeserializerWithFallbackOnDateTimeModule.addDeserializer(LocalDate.class, new LocalDateDeserializerWithFallbackOnDateTime());

    JsonMapper mapper = jsonMapper.rebuild()
      .addModule(localDateDeserializerWithFallbackOnDateTimeModule)
      .build();

    this.restTemplate = restTemplateBuilder
      .messageConverters(new JacksonJsonHttpMessageConverter(mapper))
      .build();

    restTemplate.setErrorHandler(new HttpClientErrorJsonBodyHandler<>(jsonMapper, "PAGOPA_PAYMENTS_REPORTING", clientConfig.isPrintBodyWhenError(),
      ErrorResponse.class, null,
      errorDTO -> Optional.ofNullable(errorDTO.getErrors())
        .map(e -> e.stream().map(ErrorMessage::getMessage).collect(Collectors.joining(",")))
        .orElse(errorDTO.getHttpStatusDescription())
    ));
  }

  public OrganizationsApi getOrganizationApi(String apiKey) {
    return paymentsReportingApisApiMap.computeIfAbsent(apiKey,
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
