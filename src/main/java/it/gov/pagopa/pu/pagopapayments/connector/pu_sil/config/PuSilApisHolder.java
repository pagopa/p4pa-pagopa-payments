package it.gov.pagopa.pu.pagopapayments.connector.pu_sil.config;

import it.gov.pagopa.pu.pagopapayments.config.rest.RestTemplateConfig;
import it.gov.pagopa.pu.pusil.controller.ApiClient;
import it.gov.pagopa.pu.pusil.controller.BaseApi;
import it.gov.pagopa.pu.pusil.controller.generated.ActualizationApi;
import jakarta.annotation.PreDestroy;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class PuSilApisHolder {

  private final ActualizationApi actualizationApi;
  private final ThreadLocal<String> bearerTokenHolder = new ThreadLocal<>();


  public PuSilApisHolder(
    PuSilApiClientConfig clientConfig,
    RestTemplateBuilder restTemplateBuilder) {

    RestTemplate restTemplate = restTemplateBuilder.build();
    ApiClient apiClient = new ApiClient(restTemplate);
    apiClient.setBasePath(clientConfig.getBaseUrl());
    apiClient.setBearerToken(bearerTokenHolder::get);
    apiClient.setMaxAttemptsForRetry(Math.max(1, clientConfig.getMaxAttempts()));
    apiClient.setWaitTimeMillis(clientConfig.getWaitTimeMillis());
    if (clientConfig.isPrintBodyWhenError()) {
      restTemplate.setErrorHandler(RestTemplateConfig.bodyPrinterWhenError("PU_SIL"));
    }

    this.actualizationApi = new ActualizationApi(apiClient);
  }

  @PreDestroy
  public void unload(){
    bearerTokenHolder.remove();
  }

  /** It will return a {@link ActualizationApi} instrumented with the provided accessToken. Use null if auth is not required */
  public ActualizationApi getActualizationApi(String accessToken){
    return getApi(accessToken, actualizationApi);
  }

  private <T extends BaseApi> T getApi(String accessToken, T api) {
    bearerTokenHolder.set(accessToken);
    return api;
  }
}
