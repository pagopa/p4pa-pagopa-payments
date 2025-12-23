package it.gov.pagopa.pu.pagopapayments.connector.auth.config;

import it.gov.pagopa.pu.auth.controller.generated.AuthnApi;
import it.gov.pagopa.pu.auth.controller.generated.AuthzApi;
import it.gov.pagopa.pu.auth.generated.ApiClient;
import it.gov.pagopa.pu.auth.generated.BaseApi;
import it.gov.pagopa.pu.pagopapayments.config.rest.RestTemplateConfig;
import jakarta.annotation.PreDestroy;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class AuthApisHolder {

  private final AuthnApi authnApi;
  private final AuthzApi authzApi;

  private final ThreadLocal<String> bearerTokenHolder = new ThreadLocal<>();

  public AuthApisHolder(
    AuthApiClientConfig clientConfig,
    RestTemplateBuilder restTemplateBuilder
  ) {
    RestTemplate restTemplate = restTemplateBuilder.build();
    ApiClient apiClient = new ApiClient(restTemplate);
    apiClient.setBasePath(clientConfig.getBaseUrl());
    apiClient.setBearerToken(bearerTokenHolder::get);
    apiClient.setMaxAttemptsForRetry(Math.max(1, clientConfig.getMaxAttempts()));
    apiClient.setWaitTimeMillis(clientConfig.getWaitTimeMillis());
    if (clientConfig.isPrintBodyWhenError()) {
      restTemplate.setErrorHandler(RestTemplateConfig.bodyPrinterWhenError("AUTH"));
    }

    this.authnApi = new AuthnApi(apiClient);
    this.authzApi = new AuthzApi(apiClient);
  }

  @PreDestroy
  public void unload() {
    bearerTokenHolder.remove();
  }

  /**
   * It will return a {@link AuthnApi} instrumented with the provided accessToken. Use null if auth is not required
   */
  public AuthnApi getAuthnApi(String accessToken) {
    return getApi(accessToken, authnApi);
  }

  /**
   * It will return a {@link AuthzApi} instrumented with the provided accessToken. Use null if auth is not required
   */
  public AuthzApi getAuthzApi(String accessToken) {
    bearerTokenHolder.set(accessToken);
    return getApi(accessToken, authzApi);
  }

  private <T extends BaseApi> T getApi(String accessToken, T api) {
    bearerTokenHolder.set(accessToken);
    return api;
  }
}
