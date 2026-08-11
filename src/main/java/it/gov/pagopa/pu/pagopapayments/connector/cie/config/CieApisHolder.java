package it.gov.pagopa.pu.pagopapayments.connector.cie.config;

import it.gov.pagopa.pu.cie.generated.ApiClient;
import it.gov.pagopa.pu.cie.generated.BaseApi;
import it.gov.pagopa.pu.cie.client.generated.DebtPositionCieApi;
import it.gov.pagopa.pu.cie.dto.generated.ErrorDTO;
import it.gov.pagopa.pu.pagopapayments.config.rest.HttpClientErrorJsonBodyHandler;
import jakarta.annotation.PreDestroy;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.json.JsonMapper;

@Service
public class CieApisHolder {

  private final DebtPositionCieApi debtPositionCieApi;

  private final ThreadLocal<String> bearerTokenHolder = new ThreadLocal<>();

  public CieApisHolder(
    CieApiClientConfig clientConfig,
    RestTemplateBuilder restTemplateBuilder,
    JsonMapper jsonMapper
  ) {
    RestTemplate restTemplate = restTemplateBuilder.build();
    ApiClient apiClient = new ApiClient(restTemplate);
    apiClient.setBasePath(clientConfig.getBaseUrl());
    apiClient.setBearerToken(bearerTokenHolder::get);
    apiClient.setMaxAttemptsForRetry(Math.max(1, clientConfig.getMaxAttempts()));
    apiClient.setWaitTimeMillis(clientConfig.getWaitTimeMillis());
    restTemplate.setErrorHandler(new HttpClientErrorJsonBodyHandler<>(jsonMapper, "CIE", clientConfig.isPrintBodyWhenError(),
      ErrorDTO.class, ErrorDTO::getCode, ErrorDTO::getMessage)
    );

    this.debtPositionCieApi = new DebtPositionCieApi(apiClient);
  }

  @PreDestroy
  public void unload() {
    bearerTokenHolder.remove();
  }

  /** It will return a {@link DebtPositionCieApi} instrumented with the provided accessToken. Use null if auth is not required */
  public DebtPositionCieApi getDebtPositionCieApi(String accessToken) {
    return getApi(accessToken, debtPositionCieApi);
  }

  private <T extends BaseApi> T getApi(String accessToken, T api) {
    bearerTokenHolder.set(accessToken);
    return api;
  }
}
