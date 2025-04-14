package it.gov.pagopa.pu.pagopapayments.util;

import it.gov.pagopa.pu.pagopapayments.config.rest.HttpClientConfig;
import org.apache.hc.client5.http.HttpRoute;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.ssl.TlsSocketStrategy;
import org.apache.hc.core5.function.Resolver;
import org.apache.hc.core5.util.Timeout;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

import java.lang.reflect.Field;

class HttpUtilsTest {

    @Test
    void whenGetPooledConnectionManagerBuilderThenReturnConfiguredConnectionManager() throws NoSuchFieldException, IllegalAccessException {
        // Given
        HttpClientConfig httpClientConfig = buildTestHttpClientConfig();
        TlsSocketStrategy tlsSocketStrategy = Mockito.mock(TlsSocketStrategy.class);

        // When
        PoolingHttpClientConnectionManager result = HttpUtils.getPooledConnectionManagerBuilder(httpClientConfig, tlsSocketStrategy).build();

        // Then
        assertHttpClientConnectionManager(result);
    }

    @Test
    void whenBuildPooledConnectionThenReturnConfiguredConnectionManager() throws NoSuchFieldException, IllegalAccessException {
        // Given
        HttpClientConfig httpClientConfig = buildTestHttpClientConfig();
        TlsSocketStrategy tlsSocketStrategy = Mockito.mock(TlsSocketStrategy.class);

        // When
        HttpComponentsClientHttpRequestFactory result = HttpUtils.buildPooledConnection(httpClientConfig, tlsSocketStrategy).build();

        // Then
        HttpClient httpClient = result.getHttpClient();
        Field connManagerField = httpClient.getClass().getDeclaredField("connManager");
        connManagerField.setAccessible(true);
        PoolingHttpClientConnectionManager pooledConnectionManager = (PoolingHttpClientConnectionManager) connManagerField.get(httpClient);
        assertHttpClientConnectionManager(pooledConnectionManager);
    }

    private static HttpClientConfig buildTestHttpClientConfig() {
        return HttpClientConfig.builder()
                .connectionPool(HttpClientConfig.HttpClientConnectionPoolConfig.builder()
                        .size(10)
                        .sizePerRoute(5)
                        .timeToLiveMinutes(3)
                        .build())
                .timeout(HttpClientConfig.HttpClientTimeoutConfig.builder()
                        .connectMillis(1000)
                        .readMillis(3000)
                        .build())
                .build();
    }

    @SuppressWarnings("unchecked")
    private static void assertHttpClientConnectionManager(PoolingHttpClientConnectionManager result) throws NoSuchFieldException, IllegalAccessException {
        Assertions.assertEquals(10, result.getMaxTotal());
        Assertions.assertEquals(5, result.getDefaultMaxPerRoute());

        Field connectionConfigResolverField = PoolingHttpClientConnectionManager.class.getDeclaredField("connectionConfigResolver");
        connectionConfigResolverField.setAccessible(true);
        Resolver<HttpRoute, ConnectionConfig> connectionConfigResolver = (Resolver<HttpRoute, ConnectionConfig>) connectionConfigResolverField.get(result);
        ConnectionConfig connectionConfig = connectionConfigResolver.resolve(null);

        Assertions.assertEquals(Timeout.ofMilliseconds(1_000), connectionConfig.getConnectTimeout());
        Assertions.assertEquals(Timeout.ofMilliseconds(3_000), connectionConfig.getSocketTimeout());
        Assertions.assertEquals(Timeout.ofMilliseconds(180_000), connectionConfig.getTimeToLive());
    }
}
