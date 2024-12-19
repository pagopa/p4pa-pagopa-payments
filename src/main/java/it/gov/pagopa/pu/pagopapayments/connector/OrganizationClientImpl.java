package it.gov.pagopa.pu.pagopapayments.connector;

import it.gov.pagopa.pu.p4pa_organization.controller.ApiClient;
import it.gov.pagopa.pu.p4pa_organization.controller.generated.BrokerApi;
import it.gov.pagopa.pu.p4pa_organization.controller.generated.OrganizationEntityControllerApi;
import it.gov.pagopa.pu.p4pa_organization.dto.generated.BrokerApiKeys;
import it.gov.pagopa.pu.p4pa_organization.dto.generated.EntityModelOrganization;
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

  public OrganizationClientImpl(@Value("${app.organization.base-url}") String organizationBaseUrl,
                                RestTemplateBuilder restTemplateBuilder){
    RestTemplate restTemplate = restTemplateBuilder.build();
    this.organizationEntityControllerApi = new OrganizationEntityControllerApi(
      new ApiClient(restTemplate).setBasePath(organizationBaseUrl) );
    this.brokerApi = new BrokerApi(
      new ApiClient(restTemplate).setBasePath(organizationBaseUrl) );
  }


  @Override
  public String getAcaApiKeyByBrokerId(Long brokerId) {
    BrokerApiKeys apiKeys = RestUtil.handleRestException(
      () -> brokerApi.getBrokerApiKeys(brokerId),
      () -> "getBrokerApiKeys[%s]".formatted(brokerId)
    );
    return apiKeys.getAcaKey();
  }

  @Override
  public EntityModelOrganization getOrganizationById(Long organizationId) {
    return RestUtil.handleRestException(
      () -> organizationEntityControllerApi.getItemResourceOrganizationGet(String.valueOf(organizationId)),
      () -> "getOrganizationById[%s]".formatted(organizationId)
    );
  }
}
