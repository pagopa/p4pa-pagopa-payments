package it.gov.pagopa.pu.pagopapayments.connector.organization.config;

import it.gov.pagopa.pu.organization.controller.ApiClient;
import it.gov.pagopa.pu.organization.controller.BaseApi;
import it.gov.pagopa.pu.organization.controller.generated.*;
import it.gov.pagopa.pu.pagopapayments.config.rest.RestTemplateConfig;
import jakarta.annotation.PreDestroy;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class OrganizationApisHolder {

  private final OrganizationEntityControllerApi organizationEntityControllerApi;
  private final OrganizationSearchControllerApi organizationSearchControllerApi;
  private final OrganizationApi organizationApi;
  private final BrokerEntityControllerApi brokerEntityControllerApi;
  private final BrokerApi brokerApi;
  private final BrokerSearchControllerApi brokerSearchControllerApi;

  private final ThreadLocal<String> bearerTokenHolder = new ThreadLocal<>();

  public OrganizationApisHolder(
    OrganizationApiClientConfig clientConfig,
    RestTemplateBuilder restTemplateBuilder
  ) {
    RestTemplate restTemplate = restTemplateBuilder.build();
    ApiClient apiClient = new ApiClient(restTemplate);
    apiClient.setBasePath(clientConfig.getBaseUrl());
    apiClient.setBearerToken(bearerTokenHolder::get);
    apiClient.setMaxAttemptsForRetry(Math.max(1, clientConfig.getMaxAttempts()));
    apiClient.setWaitTimeMillis(clientConfig.getWaitTimeMillis());
    if (clientConfig.isPrintBodyWhenError()) {
      restTemplate.setErrorHandler(RestTemplateConfig.bodyPrinterWhenError("ORGANIZATION"));
    }

    this.organizationEntityControllerApi = new OrganizationEntityControllerApi(apiClient);
    this.organizationSearchControllerApi = new OrganizationSearchControllerApi(apiClient);
    this.organizationApi = new OrganizationApi(apiClient);
    this.brokerEntityControllerApi = new BrokerEntityControllerApi(apiClient);
    this.brokerApi = new BrokerApi(apiClient);
    this.brokerSearchControllerApi = new BrokerSearchControllerApi(apiClient);
  }

  @PreDestroy
  public void unload() {
    bearerTokenHolder.remove();
  }

  public OrganizationEntityControllerApi getOrganizationEntityControllerApi(String accessToken) {
    return getApi(accessToken, organizationEntityControllerApi);
  }

  public OrganizationSearchControllerApi getOrganizationSearchControllerApi(String accessToken) {
    bearerTokenHolder.set(accessToken);
    return getApi(accessToken, organizationSearchControllerApi);
  }

  public OrganizationApi getOrganizationApi(String accessToken) {
    return getApi(accessToken, organizationApi);
  }

  public BrokerEntityControllerApi getBrokerEntityControllerApi(String accessToken) {
    bearerTokenHolder.set(accessToken);
    return getApi(accessToken, brokerEntityControllerApi);
  }

  public BrokerApi getBrokerApi(String accessToken) {
    bearerTokenHolder.set(accessToken);
    return getApi(accessToken, brokerApi);
  }

  public BrokerSearchControllerApi getBrokerSearchControllerApi(String accessToken) {
    bearerTokenHolder.set(accessToken);
    return getApi(accessToken, brokerSearchControllerApi);
  }

  private <T extends BaseApi> T getApi(String accessToken, T api) {
    bearerTokenHolder.set(accessToken);
    return api;
  }
}
