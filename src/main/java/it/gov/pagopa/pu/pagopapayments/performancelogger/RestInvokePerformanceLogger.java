package it.gov.pagopa.pu.pagopapayments.performancelogger;

import it.gov.pagopa.pu.pagopapayments.util.SecurityUtils;
import it.gov.pagopa.pu.pagopapayments.util.Utilities;
import jakarta.annotation.Nonnull;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

/**
 * It will execute {@link PerformanceLogger} on each RestTemplate invocation
 */
public class RestInvokePerformanceLogger implements ClientHttpRequestInterceptor {

    public static final String REST_INVOKE_HEADER_APP_NAME = "X-app-name";
    public static final String REST_INVOKE_HEADER_CORRELATION_ID = "X-correlation-id";

    private final String appName;

    public RestInvokePerformanceLogger(String appName) {
        this.appName = appName;
    }

    @Override
    @Nonnull
    public ClientHttpResponse intercept(@Nonnull HttpRequest request, @Nonnull byte[] body, @Nonnull ClientHttpRequestExecution execution) {
        request.getHeaders().add(REST_INVOKE_HEADER_APP_NAME, appName);
        String correlationId = Utilities.getSpanId();
        request.getHeaders().add(REST_INVOKE_HEADER_CORRELATION_ID, correlationId);
        return PerformanceLogger.execute(
                "REST_INVOKE",
                getRequestDetails(request, correlationId),
                () -> execution.execute(request, body),
                x -> "HttpStatus: " + x.getStatusCode().value(),
                null);
    }

    private String getRequestDetails(HttpRequest request, String correlationId) {
        return "%s %s][spanId=%s".formatted(
                request.getMethod(), SecurityUtils.removePiiFromURI(request.getURI()),
                correlationId);
    }
}
