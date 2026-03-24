package it.gov.pagopa.pu.pagopapayments.performancelogger;

import io.vavr.CheckedFunction3;
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

  private final CheckedFunction3<HttpRequest, byte[], ClientHttpRequestExecution, ClientHttpResponse> restInvokeHandler =
    (HttpRequest request, byte[] body, ClientHttpRequestExecution execution) ->
      execution.execute(request, body);

  @Override
  @Nonnull
  public ClientHttpResponse intercept(@Nonnull HttpRequest request, @Nonnull byte[] body, @Nonnull ClientHttpRequestExecution execution) {
    return PerformanceLogger.execute3(
      "REST_INVOKE",
      getRequestDetails(request),
      restInvokeHandler,
      x -> "HttpStatus: " + x.getStatusCode().value(),
      null,
      request, body, execution);
  }

  static String getRequestDetails(HttpRequest request) {
    return "%s %s".formatted(request.getMethod(), SecurityUtils.removePiiFromURI(request.getURI()));
  }
}
