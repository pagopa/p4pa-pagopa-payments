package it.gov.pagopa.pu.pagopapayments.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class HttpClientConfig {
    @NestedConfigurationProperty
    private HttpClientConnectionPoolConfig connectionPool;
    @NestedConfigurationProperty
    private HttpClientTimeoutConfig timeout;

    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HttpClientConnectionPoolConfig {
        private int size;
        private int sizePerRoute;
        private long timeToLiveMinutes;
    }

    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HttpClientTimeoutConfig {
        private long connectMillis;
        private long readMillis;
    }
}
