package it.gov.pagopa.pu.pagopapayments.config;

import it.gov.pagopa.pu.pagopapayments.performancelogger.RestInvokePerformanceLogger;
import jakarta.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.client.RestTemplateBuilderConfigurer;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.lang.Nullable;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResponseErrorHandler;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;

@Slf4j
@Configuration(proxyBeanMethods = false)
public class RestTemplateConfig {
    private final int connectTimeoutMillis;
    private final int readTimeoutHandlerMillis;

    public RestTemplateConfig(
            @Value("${rest.default-timeout.connect-millis}") int connectTimeoutMillis,
            @Value("${rest.default-timeout.read-millis}") int readTimeoutHandlerMillis) {
        this.connectTimeoutMillis = connectTimeoutMillis;
        this.readTimeoutHandlerMillis = readTimeoutHandlerMillis;
    }

    @Bean
    public RestTemplateBuilder restTemplateBuilder(RestTemplateBuilderConfigurer configurer) {
        return configurer.configure(new RestTemplateBuilder())
                .additionalInterceptors(new RestInvokePerformanceLogger())
                .connectTimeout(Duration.ofMillis(connectTimeoutMillis))
                .readTimeout(Duration.ofMillis(readTimeoutHandlerMillis));
    }

    public static ResponseErrorHandler bodyPrinterWhenError(String applicationName) {
        final Logger errorBodyLogger = LoggerFactory.getLogger("REST_INVOKE." + applicationName);
        return new DefaultResponseErrorHandler() {
            @Override
            protected void handleError(@Nonnull ClientHttpResponse response, @Nonnull HttpStatusCode statusCode,
                                       @Nullable URI url, @Nullable HttpMethod method) throws IOException {
                try {
                    super.handleError(response, statusCode, url, method);
                } catch (HttpStatusCodeException ex) {
                    errorBodyLogger.info("{} {} Returned status {} and resulted on exception {} - {}: {}",
                            method,
                            url,
                            ex.getStatusCode(),
                            ex.getClass().getSimpleName(),
                            ex.getMessage(),
                            ex.getResponseBodyAsString());
                    throw ex;
                }
            }
        };
    }
}
