package it.gov.pagopa.pu.pagopapayments.connector.fileshare.config;

import it.gov.pagopa.pu.pagopapayments.connector.config.ClientConfig;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "rest.fileshare")
@SuperBuilder
@NoArgsConstructor
public class FileShareClientConfig extends ClientConfig {
}
