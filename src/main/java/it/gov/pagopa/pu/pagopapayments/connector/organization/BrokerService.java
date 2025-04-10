package it.gov.pagopa.pu.pagopapayments.connector.organization;

import it.gov.pagopa.pu.organization.dto.generated.Broker;
import it.gov.pagopa.pu.organization.dto.generated.BrokerApiKeyType;
import it.gov.pagopa.pu.organization.dto.generated.BrokerApiKeys;

public interface BrokerService {
  BrokerApiKeys getApiKeyByBrokerId(Long brokerId, String accessToken);
  Broker getBrokerById(Long brokerId, String accessToken);
  String getBrokerApiKey(Long brokerId, BrokerApiKeyType brokerApiKeyType, String accessToken);
}
