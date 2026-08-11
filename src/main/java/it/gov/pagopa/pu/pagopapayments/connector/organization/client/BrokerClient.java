package it.gov.pagopa.pu.pagopapayments.connector.organization.client;

import it.gov.pagopa.pu.organization.dto.generated.Broker;
import it.gov.pagopa.pu.organization.dto.generated.BrokerApiKeyType;
import it.gov.pagopa.pu.organization.dto.generated.BrokerApiKeys;
import it.gov.pagopa.pu.pagopapayments.connector.organization.config.OrganizationApisHolder;
import it.gov.pagopa.pu.pagopapayments.exception.common.RestInvokeNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class BrokerClient {

  private final OrganizationApisHolder apisHolder;

  public BrokerClient(OrganizationApisHolder apisHolder){
    this.apisHolder = apisHolder;
  }

  public BrokerApiKeys getApiKeyByBrokerId(Long brokerId, String accessToken) {
    try{
      return apisHolder.getBrokerApi(accessToken)
        .getBrokerApiKeys(brokerId);
    } catch (RestInvokeNotFoundException e){
      log.info("Cannot find Broker having id {}", brokerId);
      return null;
    }
  }

  public Broker getBrokerById(Long brokerId, String accessToken) {
    try{
      return apisHolder.getBrokerEntityControllerApi(accessToken)
        .crudGetBroker(String.valueOf(brokerId));
    } catch (RestInvokeNotFoundException e){
      log.info("Cannot find Broker having id {}", brokerId);
      return null;
    }
  }

  public String getBrokerApiKey(Long brokerId, BrokerApiKeyType brokerKeyType, String accessToken) {
    try{
      return apisHolder.getBrokerApi(accessToken)
        .getBrokerApiKey(brokerId, brokerKeyType);
    } catch (RestInvokeNotFoundException e){
      log.info("Cannot find Broker having id {} and brokerApiKeyType {}", brokerId, brokerKeyType);
      return null;
    }
  }

  public Broker getBrokerByBrokerFiscalCode(String brokerFiscalCode, String accessToken) {
    try {
      return apisHolder.getBrokerSearchControllerApi(accessToken)
        .crudBrokersFindByBrokerFiscalCode(brokerFiscalCode);
    } catch (RestInvokeNotFoundException e) {
      log.info("Cannot find Broker having fiscalCode {}", brokerFiscalCode);
      return null;
    }
  }
}
