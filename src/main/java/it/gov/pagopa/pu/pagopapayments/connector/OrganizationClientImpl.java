package it.gov.pagopa.pu.pagopapayments.connector;

import it.gov.pagopa.pu.organization.controller.ApiClient;
import it.gov.pagopa.pu.organization.controller.generated.BrokerApi;
import it.gov.pagopa.pu.organization.controller.generated.BrokerEntityControllerApi;
import it.gov.pagopa.pu.organization.controller.generated.OrganizationEntityControllerApi;
import it.gov.pagopa.pu.organization.controller.generated.OrganizationSearchControllerApi;
import it.gov.pagopa.pu.organization.dto.generated.Broker;
import it.gov.pagopa.pu.organization.dto.generated.BrokerApiKeys;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.pagopapayments.util.RestUtil;
import jakarta.annotation.PreDestroy;
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
  private final BrokerApi brokerApi;
  private final ThreadLocal<String> bearerTokenHolder = new ThreadLocal<>();

  public OrganizationClientImpl(@Value("${rest.organization.base-url}") String organizationBaseUrl,
                                RestTemplateBuilder restTemplateBuilder){
    RestTemplate restTemplate = restTemplateBuilder.build();
    ApiClient apiClient = new ApiClient(restTemplate)
      .setBasePath(organizationBaseUrl);
    apiClient.setBearerToken(bearerTokenHolder::get);
    this.organizationEntityControllerApi = new OrganizationEntityControllerApi(apiClient);
    this.organizationSearchControllerApi = new OrganizationSearchControllerApi(apiClient);
    this.brokerEntityControllerApi = new BrokerEntityControllerApi(apiClient);
    this.brokerApi = new BrokerApi(apiClient);
  }

  @PreDestroy
  public void unload(){
    bearerTokenHolder.remove();
  }

  @Override
  public BrokerApiKeys getApiKeyByBrokerId(Long brokerId, String accessToken) {
    bearerTokenHolder.set(accessToken);
    return RestUtil.handleRestException(
      () -> brokerApi.getBrokerApiKeys(brokerId),
      "getBrokerApiKeys[%s]".formatted(brokerId)
    );
  }

  @Override
  public Broker getBrokerById(Long brokerId, String accessToken) {
    bearerTokenHolder.set(accessToken);
    return RestUtil.handleRestException(
      () -> brokerEntityControllerApi.crudGetBroker(String.valueOf(brokerId)),
      "getBrokerById[%s]".formatted(brokerId)
    );
  }

  @Override
  public Organization getOrganizationById(Long organizationId, String accessToken) {
    bearerTokenHolder.set(accessToken);
    return RestUtil.handleRestException(
      () -> organizationEntityControllerApi.crudGetOrganization(String.valueOf(organizationId)),
      "getOrganizationById[%s]".formatted(organizationId)
    );
  }

  @Override
  public Organization getOrganizationByFiscalCode(String organizationFiscalCode, String accessToken) {
    bearerTokenHolder.set(accessToken);
    return RestUtil.handleRestException(
      () -> organizationSearchControllerApi.crudOrganizationsFindByOrgFiscalCode(organizationFiscalCode),
      "getOrganizationByFiscalCode[%s]".formatted(organizationFiscalCode)
    );
  }
}
