package it.gov.pagopa.pu.pagopapayments.connector.pu_sil.config;

import it.gov.pagopa.pu.pagopapayments.config.rest.HttpClientErrorJsonBodyHandler;
import it.gov.pagopa.pu.pusil.generated.ApiClient;
import it.gov.pagopa.pu.pusil.generated.BaseApi;
import it.gov.pagopa.pu.pusil.client.generated.ActualizationApi;
import it.gov.pagopa.pu.pusil.dto.generated.PuSilErrorDTO;
import jakarta.annotation.PreDestroy;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.json.JsonMapper;

@Service
public class PuSilApisHolder {

  private final ActualizationApi actualizationApi;
  private final ThreadLocal<String> bearerTokenHolder = new ThreadLocal<>();


  public PuSilApisHolder(
    PuSilApiClientConfig clientConfig,
    RestTemplateBuilder restTemplateBuilder,
    JsonMapper jsonMapper
  ) {

    RestTemplate restTemplate = restTemplateBuilder.build();
    ApiClient apiClient = new ApiClient(restTemplate);
    apiClient.setBasePath(clientConfig.getBaseUrl());
    apiClient.setBearerToken(bearerTokenHolder::get);
    apiClient.setMaxAttemptsForRetry(Math.max(1, clientConfig.getMaxAttempts()));
    apiClient.setWaitTimeMillis(clientConfig.getWaitTimeMillis());
    restTemplate.setErrorHandler(new HttpClientErrorJsonBodyHandler<>(jsonMapper, "PU-SIL", clientConfig.isPrintBodyWhenError(),
      PuSilErrorDTO.class, PuSilErrorDTO::getCode, PuSilErrorDTO::getMessage)
    );

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
