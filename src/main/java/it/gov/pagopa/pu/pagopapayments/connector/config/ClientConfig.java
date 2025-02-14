package it.gov.pagopa.pu.pagopapayments.connector.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ClientConfig {
    private String baseUrl;
    private int maxAttempts;
    private long waitTimeMillis;
    private boolean printBodyWhenError;
}
