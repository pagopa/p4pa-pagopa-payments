package it.gov.pagopa.pu.pagopapayments.service.broker;

import it.gov.pagopa.pu.organization.dto.generated.*;
import it.gov.pagopa.pu.pagopapayments.config.CacheConfig;
import it.gov.pagopa.pu.pagopapayments.connector.organization.BrokerService;
import it.gov.pagopa.pu.pagopapayments.connector.organization.OrganizationService;
import it.gov.pagopa.pu.pagopapayments.dto.BrokerForNodoPaDTO;
import it.gov.pagopa.pu.pagopapayments.exception.NotFoundException;
import it.gov.pagopa.pu.pagopapayments.util.ErrorCodeConstants;
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

  @Cacheable(cacheNames = CacheConfig.Fields.brokerApiKeyAndSegregationCodes, key = "#organizationId", unless="#result == null")
  public Pair<BrokerApiKeys, String> getBrokerApiKeyAndSegregationCodesByOrganizationId(Long organizationId, String accessToken){
    Organization organization = organizationService.getOrganizationById(organizationId, accessToken);
    if(organization==null){
      throw new NotFoundException(ErrorCodeConstants.ERROR_CODE_ORGANIZATION_NOT_FOUND, "organization [%s]".formatted(organizationId));
    }
    BrokerApiKeys apiKeys = brokerService.getApiKeyByBrokerId(organization.getBrokerId(), accessToken);
    String segregationCodes = organization.getSegregationCode();
    return Pair.of(apiKeys, segregationCodes);
  }

  @Cacheable(cacheNames = CacheConfig.Fields.brokerApiKeyAndFiscalCode, key = "#organizationId", unless="#result == null")
  public BrokerForNodoPaDTO getBrokerForNodoPaDTOByOrganizationId(Long organizationId, String accessToken){
    OrganizationStationDTO defaultOrganizationStation = organizationService.getOrganizationStationDTO(organizationId, null, accessToken);
    if(defaultOrganizationStation == null){
      throw new NotFoundException(ErrorCodeConstants.ERROR_CODE_ORGANIZATION_STATION_NOT_FOUND, "organization station for organization [%s]".formatted(organizationId));
    }
    BrokerApiKeys apiKeys = brokerService.getApiKeyByBrokerId(defaultOrganizationStation.getBrokerId(), accessToken);
    Broker broker = brokerService.getBrokerById(defaultOrganizationStation.getBrokerId(), accessToken);

    return BrokerForNodoPaDTO.builder()
      .broker(broker)
      .brokerApiKeys(apiKeys)
      .organizationStation(defaultOrganizationStation)
      .build();
  }
}
