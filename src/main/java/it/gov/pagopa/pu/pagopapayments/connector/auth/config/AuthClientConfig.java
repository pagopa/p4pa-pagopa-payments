package it.gov.pagopa.pu.pagopapayments.connector.auth.config;

import it.gov.pagopa.pu.pagopapayments.connector.config.ClientConfig;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "rest.auth")
@SuperBuilder
@NoArgsConstructor
public class AuthClientConfig extends ClientConfig {
}
