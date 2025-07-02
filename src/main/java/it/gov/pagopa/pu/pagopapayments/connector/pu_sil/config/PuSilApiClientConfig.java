package it.gov.pagopa.pu.pagopapayments.connector.pu_sil.config;

import it.gov.pagopa.pu.pagopapayments.config.rest.ApiClientConfig;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "rest.pu-sil")
@SuperBuilder
@NoArgsConstructor
public class PuSilApiClientConfig extends ApiClientConfig {
}
