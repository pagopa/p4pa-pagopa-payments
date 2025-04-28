package it.gov.pagopa.pu.pagopapayments.connector.organization.config;

import it.gov.pagopa.pu.organization.controller.ApiClient;
import it.gov.pagopa.pu.organization.controller.BaseApi;
import it.gov.pagopa.pu.organization.controller.generated.BrokerApi;
import it.gov.pagopa.pu.organization.controller.generated.BrokerEntityControllerApi;
import it.gov.pagopa.pu.organization.controller.generated.OrganizationApi;
import it.gov.pagopa.pu.organization.controller.generated.OrganizationEntityControllerApi;
import it.gov.pagopa.pu.organization.controller.generated.OrganizationSearchControllerApi;
import it.gov.pagopa.pu.pagopapayments.config.rest.RestTemplateConfig;
import jakarta.annotation.PreDestroy;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class OrganizationApisHolder {

  private final OrganizationEntityControllerApi organizationEntityControllerApi;
  private final OrganizationSearchControllerApi organizationSearchControllerApi;
  private final BrokerEntityControllerApi brokerEntityControllerApi;
  private final BrokerApi brokerApi;
  private final OrganizationApi organizationApi;

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
    this.brokerEntityControllerApi = new BrokerEntityControllerApi(apiClient);
    this.brokerApi = new BrokerApi(apiClient);
    this.organizationApi = new OrganizationApi(apiClient);
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

  public BrokerEntityControllerApi getBrokerEntityControllerApi(String accessToken) {
    bearerTokenHolder.set(accessToken);
    return getApi(accessToken, brokerEntityControllerApi);
  }

  public BrokerApi getBrokerApi(String accessToken) {
    bearerTokenHolder.set(accessToken);
    return getApi(accessToken, brokerApi);
  }

  public OrganizationApi getOrganizationApi(String accessToken) {
    return getApi(accessToken, organizationApi);
  }

  private <T extends BaseApi> T getApi(String accessToken, T api) {
    bearerTokenHolder.set(accessToken);
    return api;
  }
}
