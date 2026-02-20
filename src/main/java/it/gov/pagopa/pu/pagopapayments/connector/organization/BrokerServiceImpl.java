package it.gov.pagopa.pu.pagopapayments.connector.organization;

import it.gov.pagopa.pu.organization.dto.generated.Broker;
import it.gov.pagopa.pu.organization.dto.generated.BrokerApiKeyType;
import it.gov.pagopa.pu.organization.dto.generated.BrokerApiKeys;
import it.gov.pagopa.pu.pagopapayments.config.CacheConfig;
import it.gov.pagopa.pu.pagopapayments.connector.organization.client.BrokerClient;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class BrokerServiceImpl implements BrokerService {

  private final BrokerClient brokerClient;

  public BrokerServiceImpl(BrokerClient brokerClient) {
    this.brokerClient = brokerClient;
  }

  @Override
  public BrokerApiKeys getApiKeyByBrokerId(Long brokerId, String accessToken) {
    return brokerClient.getApiKeyByBrokerId(brokerId, accessToken);
  }

  @Override
  public Broker getBrokerById(Long brokerId, String accessToken) {
    return brokerClient.getBrokerById(brokerId, accessToken);
  }

  @Override
  @Cacheable(cacheNames = CacheConfig.Fields.brokerApiKeyAndSegregationCodes, key = "#brokerId + '-' + #brokerApiKeyType", unless="#result == null")
  public String getBrokerApiKey(Long brokerId, BrokerApiKeyType brokerApiKeyType, String accessToken) {
    return brokerClient.getBrokerApiKey(brokerId, brokerApiKeyType, accessToken);
  }

  @Override
  public Broker getBrokerByStationId(String stationId, String accessToken) {
    return brokerClient.getBrokerByStationId(stationId, accessToken);
  }
}
