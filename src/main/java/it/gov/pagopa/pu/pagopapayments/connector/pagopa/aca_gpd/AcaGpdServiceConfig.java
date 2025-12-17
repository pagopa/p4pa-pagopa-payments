package it.gov.pagopa.pu.pagopapayments.connector.pagopa.aca_gpd;

import it.gov.pagopa.pu.pagopapayments.connector.pagopa.aca.config.AcaApisHolder;
import it.gov.pagopa.pu.pagopapayments.connector.pagopa.gpd.GpdService;
import it.gov.pagopa.pu.pagopapayments.connector.pagopa.gpd.GpdServiceImpl;
import it.gov.pagopa.pu.pagopapayments.connector.pagopa.gpd.client.GpdClient;
import it.gov.pagopa.pu.pagopapayments.connector.pagopa.gpd.config.GpdApisHolder;
import it.gov.pagopa.pu.pagopapayments.registry.RegistryLogger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AcaGpdServiceConfig {

  @Bean("gpdClient")
  public GpdClient gpdClient(GpdApisHolder gpdApisHolder, RegistryLogger registryLogger) {
    return new GpdClient(gpdApisHolder, registryLogger);
  }

  @Bean("acaClient")
  public GpdClient acaClient(AcaApisHolder acaApisHolder, RegistryLogger registryLogger) {
    return new GpdClient(acaApisHolder, registryLogger);
  }

  @Bean("gpdService")
  public GpdService gpdService(@Qualifier("gpdClient") GpdClient client) {
    return new GpdServiceImpl(client);
  }

  @Bean("acaServiceWrapper")
  public GpdService acaServiceWrapper(@Qualifier("acaClient") GpdClient client) {
    return new GpdServiceImpl(client);
  }
}
