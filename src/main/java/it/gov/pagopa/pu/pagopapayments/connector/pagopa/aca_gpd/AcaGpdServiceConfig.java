package it.gov.pagopa.pu.pagopapayments.connector.pagopa.aca_gpd;

import it.gov.pagopa.pu.pagopapayments.connector.pagopa.aca.AcaService;
import it.gov.pagopa.pu.pagopapayments.connector.pagopa.aca.AcaServiceImpl;
import it.gov.pagopa.pu.pagopapayments.connector.pagopa.aca.client.AcaClient;
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

  @Bean
  public AcaClient acaClient(AcaApisHolder acaApisHolder, RegistryLogger registryLogger) {
    return new AcaClient(acaApisHolder, registryLogger);
  }

  @Bean("gpdService")
  public GpdService gpdService(@Qualifier("gpdClient") GpdClient gpdClient) {
    return new GpdServiceImpl(gpdClient);
  }

  @Bean("acaService")
  public AcaService acaService(@Qualifier("acaClient") AcaClient acaClient) {
    return new AcaServiceImpl(acaClient);
  }
}
