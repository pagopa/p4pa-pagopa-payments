package it.gov.pagopa.pu.pagopapayments.connector;

import it.gov.pagopa.pu.p4pa_organization.controller.ApiClient;
import it.gov.pagopa.pu.p4pa_organization.controller.generated.BrokerEntityControllerApi;
import it.gov.pagopa.pu.p4pa_organization.controller.generated.OrganizationEntityControllerApi;
import it.gov.pagopa.pu.p4pa_organization.controller.generated.OrganizationSearchControllerApi;
import it.gov.pagopa.pu.p4pa_organization.dto.generated.EntityModelBroker;
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
  private final OrganizationSearchControllerApi organizationSearchControllerApi;
  private final BrokerEntityControllerApi brokerEntityControllerApi;

  public OrganizationClientImpl(@Value("${app.organization.base-url}") String organizationBaseUrl,
                                RestTemplateBuilder restTemplateBuilder){
    RestTemplate restTemplate = restTemplateBuilder.build();
    this.organizationEntityControllerApi = new OrganizationEntityControllerApi(
      new ApiClient(restTemplate).setBasePath(organizationBaseUrl) );
    this.organizationSearchControllerApi = new OrganizationSearchControllerApi(
      new ApiClient(restTemplate).setBasePath(organizationBaseUrl) );
    this.brokerEntityControllerApi = new BrokerEntityControllerApi(
      new ApiClient(restTemplate).setBasePath(organizationBaseUrl) );
  }


  @Override
  public String getAcaApiKeyByBrokerId(Long brokerId) {
    EntityModelBroker broker = RestUtil.handleRestException(
      () -> brokerEntityControllerApi.getItemResourceBrokerGet(String.valueOf(brokerId)),
      () -> "getBrokerById[%s]".formatted(brokerId)
    );
    return broker.getAcaKey().toString();
  }

  @Override
  public EntityModelOrganization getOrganizationById(Long organizationId) {
    return RestUtil.handleRestException(
      () -> organizationEntityControllerApi.getItemResourceOrganizationGet(String.valueOf(organizationId)),
      () -> "getOrganizationById[%s]".formatted(organizationId)
    );
  }
}
