package it.gov.pagopa.pu.pagopapayments.util;

import it.gov.pagopa.pu.pagopapayments.config.HttpClientConfig;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.TlsSocketStrategy;
import org.apache.hc.core5.pool.PoolConcurrencyPolicy;
import org.apache.hc.core5.pool.PoolReusePolicy;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.http.client.HttpComponentsClientHttpRequestFactoryBuilder;

public class HttpUtils {
    private HttpUtils() {
    }

    public static PoolingHttpClientConnectionManagerBuilder getPooledConnectionManagerBuilder(HttpClientConfig httpClientConfig, TlsSocketStrategy tlsSocketStrategy) {
        return PoolingHttpClientConnectionManagerBuilder.create()
                .setTlsSocketStrategy(tlsSocketStrategy)
                .setPoolConcurrencyPolicy(PoolConcurrencyPolicy.STRICT)
                .setConnPoolPolicy(PoolReusePolicy.LIFO)
                .setMaxConnPerRoute(httpClientConfig.getConnectionPool().getSizePerRoute())
                .setMaxConnTotal(httpClientConfig.getConnectionPool().getSize())
                .setDefaultConnectionConfig(ConnectionConfig.custom()
                        .setSocketTimeout(Timeout.ofMilliseconds(httpClientConfig.getTimeout().getReadMillis()))
                        .setConnectTimeout(Timeout.ofMilliseconds(httpClientConfig.getTimeout().getConnectMillis()))
                        .setTimeToLive(TimeValue.ofMinutes(httpClientConfig.getConnectionPool().getTimeToLiveMinutes()))
                        .build());
    }

    public static HttpComponentsClientHttpRequestFactoryBuilder buildPooledConnection(HttpClientConfig httpClientConfig, TlsSocketStrategy tlsSocketStrategy) {
        return ClientHttpRequestFactoryBuilder.httpComponents()
                .withHttpClientCustomizer(configurer -> configurer
                        .setConnectionManager(getPooledConnectionManagerBuilder(httpClientConfig, tlsSocketStrategy).build()));
    }
}
