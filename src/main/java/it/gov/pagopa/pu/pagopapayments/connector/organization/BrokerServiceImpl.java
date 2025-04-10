package it.gov.pagopa.pu.pagopapayments.connector.organization;

import it.gov.pagopa.pu.organization.dto.generated.Broker;
import it.gov.pagopa.pu.organization.dto.generated.BrokerApiKeyType;
import it.gov.pagopa.pu.organization.dto.generated.BrokerApiKeys;
import it.gov.pagopa.pu.pagopapayments.connector.organization.client.BrokerClient;
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
  public String getBrokerApiKey(Long brokerId, BrokerApiKeyType brokerApiKeyType, String accessToken) {
    return brokerClient.getBrokerApiKey(brokerId, brokerApiKeyType, accessToken);
  }

}
