package it.gov.pagopa.pu.pagopapayments.service.broker;

import it.gov.pagopa.pu.p4pa_organization.dto.generated.BrokerApiKeys;
import it.gov.pagopa.pu.p4pa_organization.dto.generated.Organization;
import it.gov.pagopa.pu.pagopapayments.connector.OrganizationClient;
import it.gov.pagopa.pu.pagopapayments.exception.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class BrokerService {

  private final OrganizationClient organizationClient;

  public BrokerService(OrganizationClient organizationClient) {
    this.organizationClient = organizationClient;
  }

  @Cacheable("brokerApiKey")
  public Pair<BrokerApiKeys, String> getBrokerApiKeyAndSegregationCodesByOrganizationId(Long organizationId){
    Organization organization = organizationClient.getOrganizationById(organizationId);
    if(organization==null){
      log.error("organization not found [{}]",organizationId);
      throw new NotFoundException("organization [%s]".formatted(organizationId));
    }
    BrokerApiKeys apiKeys = organizationClient.getApiKeyByBrokerId(organization.getBrokerId());
    String segregationCodes = organization.getApplicationCode();
    return Pair.of(apiKeys, segregationCodes);
  }
}
