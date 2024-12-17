package it.gov.pagopa.pu.pagopapayments.config;

import it.gov.pagopa.pu.p4pa_organization.controller.ApiClient;
import it.gov.pagopa.pu.p4pa_organization.controller.generated.BrokerEntityControllerApi;
import it.gov.pagopa.pu.p4pa_organization.controller.generated.OrganizationEntityControllerApi;
import it.gov.pagopa.pu.p4pa_organization.controller.generated.OrganizationSearchControllerApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class ApiClientConfig {

  @Value("${app.organization.base-url}")
  String organizationBaseUrl;

  private final RestTemplate restTemplate;

  @Bean
  public OrganizationEntityControllerApi organizationEntityControllerApiClient() {
    return new OrganizationEntityControllerApi(
      new ApiClient(restTemplate).setBasePath(organizationBaseUrl)
    );
  }

  @Bean
  public OrganizationSearchControllerApi organizationSearchControllerApi() {
    return new OrganizationSearchControllerApi(
      new ApiClient(restTemplate).setBasePath(organizationBaseUrl)
    );
  }

  @Bean
  public BrokerEntityControllerApi brokerEntityControllerApiClient() {
    return new BrokerEntityControllerApi(
      new ApiClient(restTemplate).setBasePath(organizationBaseUrl)
    );
  }
}
