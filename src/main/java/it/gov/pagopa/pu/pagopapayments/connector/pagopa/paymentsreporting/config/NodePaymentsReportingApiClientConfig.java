package it.gov.pagopa.pu.pagopapayments.connector.pagopa.paymentsreporting.config;

import it.gov.pagopa.pu.pagopapayments.config.rest.ApiClientConfig;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "rest.pagopa-node-services.sync-payments-reporting")
@SuperBuilder
@NoArgsConstructor
public class NodePaymentsReportingApiClientConfig extends ApiClientConfig {
}
