package it.gov.pagopa.pu.pagopapayments.performancelogger;

import it.gov.pagopa.pu.pagopapayments.util.SecurityUtils;
import jakarta.annotation.Nonnull;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

/**
 * It will execute {@link PerformanceLogger} on each RestTemplate invocation
 */
public class RestInvokePerformanceLogger implements ClientHttpRequestInterceptor {

    @Override
    @Nonnull
    public ClientHttpResponse intercept(@Nonnull HttpRequest request, @Nonnull byte[] body, @Nonnull ClientHttpRequestExecution execution) {
        return PerformanceLogger.execute(
                "REST_INVOKE",
                getRequestDetails(request),
                () -> execution.execute(request, body),
                x -> "HttpStatus: " + x.getStatusCode().value(),
                null);
    }

    static String getRequestDetails(HttpRequest request) {
        return "%s %s".formatted(request.getMethod(), SecurityUtils.removePiiFromURI(request.getURI()));
    }
}
