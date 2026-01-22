package it.gov.pagopa.pu.pagopapayments.connector.pagopa.aca.config;

import it.gov.pagopa.pu.pagopapayments.connector.pagopa.aca.AcaService;
import it.gov.pagopa.pu.pagopapayments.connector.pagopa.aca.AcaServiceImpl;
import it.gov.pagopa.pu.pagopapayments.connector.pagopa.aca.client.AcaClient;
import it.gov.pagopa.pu.pagopapayments.registry.RegistryLogger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AcaServiceConfig {

  @Bean
  public AcaClient acaClient(AcaApisHolder acaApisHolder, RegistryLogger registryLogger) {
    return new AcaClient(acaApisHolder, registryLogger);
  }

  @Bean
  public AcaService acaService(AcaClient acaClient) {
    return new AcaServiceImpl(acaClient);
  }
}
