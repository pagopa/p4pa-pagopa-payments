package it.gov.pagopa.pu.pagopapayments.connector;

import it.gov.pagopa.pu.p4pa_organization.controller.ApiClient;
import it.gov.pagopa.pu.p4pa_organization.controller.generated.BrokerApi;
import it.gov.pagopa.pu.p4pa_organization.controller.generated.OrganizationEntityControllerApi;
import it.gov.pagopa.pu.p4pa_organization.dto.generated.BrokerApiKeys;
import it.gov.pagopa.pu.p4pa_organization.dto.generated.Organization;
import it.gov.pagopa.pu.pagopapayments.util.RestUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class OrganizationClientImpl implements OrganizationClient{

  private final OrganizationEntityControllerApi organizationEntityControllerApi;
  private final BrokerApi brokerApi;
  private final ThreadLocal<String> bearerTokenHolder = new ThreadLocal<>();

  public OrganizationClientImpl(@Value("${app.organization.base-url}") String organizationBaseUrl,
                                RestTemplateBuilder restTemplateBuilder){
    RestTemplate restTemplate = restTemplateBuilder.build();
    ApiClient apiClient = new ApiClient(restTemplate)
      .setBasePath(organizationBaseUrl);
    apiClient.setBearerToken(bearerTokenHolder::get);
    this.organizationEntityControllerApi = new OrganizationEntityControllerApi(apiClient);
    this.brokerApi = new BrokerApi(apiClient);
  }


  @Override
  public BrokerApiKeys getApiKeyByBrokerId(Long brokerId, String accessToken) {
    bearerTokenHolder.set(accessToken);
    return RestUtil.handleRestException(
      () -> brokerApi.getBrokerApiKeys(brokerId),
      () -> "getBrokerApiKeys[%s]".formatted(brokerId)
    );
  }

  @Override
  public Organization getOrganizationById(Long organizationId, String accessToken) {
    bearerTokenHolder.set(accessToken);
    return RestUtil.handleRestException(
      () -> organizationEntityControllerApi.getItemResourceOrganizationGet(String.valueOf(organizationId)),
      () -> "getOrganizationById[%s]".formatted(organizationId)
    );
  }
}
