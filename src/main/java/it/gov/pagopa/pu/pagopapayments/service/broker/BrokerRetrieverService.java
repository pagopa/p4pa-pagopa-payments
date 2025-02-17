package it.gov.pagopa.pu.pagopapayments.service.broker;

import it.gov.pagopa.pu.organization.dto.generated.Broker;
import it.gov.pagopa.pu.organization.dto.generated.BrokerApiKeys;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.pagopapayments.connector.organization.BrokerService;
import it.gov.pagopa.pu.pagopapayments.connector.organization.OrganizationService;
import it.gov.pagopa.pu.pagopapayments.dto.BrokerForNodoPaDTO;
import it.gov.pagopa.pu.pagopapayments.exception.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class BrokerRetrieverService {

  private final BrokerService brokerService;
  private final OrganizationService organizationService;

  public BrokerRetrieverService(BrokerService brokerService, OrganizationService organizationService) {
    this.brokerService = brokerService;
    this.organizationService = organizationService;
  }

  @Cacheable("brokerApiKeyAndSegregationCodes")
  public Pair<BrokerApiKeys, String> getBrokerApiKeyAndSegregationCodesByOrganizationId(Long organizationId, String accessToken){
    Organization organization = organizationService.getOrganizationById(organizationId, accessToken);
    if(organization==null){
      throw new NotFoundException("organization [%s]".formatted(organizationId));
    }
    BrokerApiKeys apiKeys = brokerService.getApiKeyByBrokerId(organization.getBrokerId(), accessToken);
    String segregationCodes = organization.getSegregationCode();
    return Pair.of(apiKeys, segregationCodes);
  }

  @Cacheable("brokerApiKeyAndFiscalCode")
  public BrokerForNodoPaDTO getBrokerForNodoPaDTOByOrganizationId(Long organizationId, String accessToken){
    Organization organization = organizationService.getOrganizationById(organizationId, accessToken);
    if(organization==null){
      throw new NotFoundException("organization [%s]".formatted(organizationId));
    }
    BrokerApiKeys apiKeys = brokerService.getApiKeyByBrokerId(organization.getBrokerId(), accessToken);
    Broker broker = brokerService.getBrokerById(organization.getBrokerId(), accessToken);

    return BrokerForNodoPaDTO.builder()
      .broker(broker)
      .organization(organization)
      .brokerApiKeys(apiKeys)
      .build();
  }
}
