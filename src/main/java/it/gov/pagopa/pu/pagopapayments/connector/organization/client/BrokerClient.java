package it.gov.pagopa.pu.pagopapayments.connector.organization.client;

import it.gov.pagopa.pu.organization.dto.generated.Broker;
import it.gov.pagopa.pu.organization.dto.generated.BrokerApiKeys;
import it.gov.pagopa.pu.pagopapayments.connector.organization.config.OrganizationApisHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

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
    } catch (HttpClientErrorException.NotFound e){
      log.info("Cannot find Broker having id {}", brokerId);
      return null;
    }
  }

  public Broker getBrokerById(Long brokerId, String accessToken) {
    try{
      return apisHolder.getBrokerEntityControllerApi(accessToken)
        .crudGetBroker(String.valueOf(brokerId));
    } catch (HttpClientErrorException.NotFound e){
      log.info("Cannot find Broker having id {}", brokerId);
      return null;
    }
  }

}
