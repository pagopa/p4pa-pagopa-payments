package it.gov.pagopa.pu.pagopapayments.connector.pagopa.gpd.config;

import it.gov.pagopa.pu.pagopapayments.connector.pagopa.gpd.GpdService;
import it.gov.pagopa.pu.pagopapayments.connector.pagopa.gpd.GpdServiceImpl;
import it.gov.pagopa.pu.pagopapayments.connector.pagopa.gpd.client.GpdClient;
import it.gov.pagopa.pu.pagopapayments.registry.RegistryLogger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GpdServiceConfig {

  @Bean
  public GpdClient gpdClient(GpdApisHolder gpdApisHolder, RegistryLogger registryLogger) {
    return new GpdClient(gpdApisHolder, registryLogger);
  }

  @Bean
  public GpdService gpdService(GpdClient gpdClient) {
    return new GpdServiceImpl(gpdClient);
  }
}
