package it.gov.pagopa.pu.pagopapayments.performancelogger;

import io.micrometer.tracing.Tracer;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * It will execute {@link PerformanceLogger} on each Api request
 */
@Service
@Order(-101) // Set in order to be executed after ServerHttpObservationFilter (which will handle traceId): configured through properties management.observations.http.server.filter.order
public class ApiRequestPerformanceLogger implements Filter {

  private static final List<String> blackListPathPrefixList = List.of(
            "/actuator",
            "/favicon.ico",
            "/swagger"
    );

  private final Tracer tracer;

  public ApiRequestPerformanceLogger(Tracer tracer) {
    this.tracer = tracer;
  }

  @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws ServletException, IOException {
        if (servletRequest instanceof HttpServletRequest httpServletRequest &&
                servletResponse instanceof HttpServletResponse httpServletResponse &&
                isPerformanceLoggedRequest(httpServletRequest)
        ) {
            PerformanceLogger.execute(
                    "API_REQUEST",
                    getRequestDetails(httpServletRequest),
                    () -> {
                        filterChain.doFilter(servletRequest, servletResponse);
                        return "ok";
                    },
                    x -> "HttpStatus: " + httpServletResponse.getStatus(),
                    null);
        } else {
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }

    private boolean isPerformanceLoggedRequest(HttpServletRequest httpServletRequest) {
        String requestURI = httpServletRequest.getRequestURI();
        return blackListPathPrefixList.stream()
                .noneMatch(requestURI::startsWith);
    }

    private String getRequestDetails(HttpServletRequest request) {
      String parentAppTag = Optional.ofNullable(request.getHeader("X-app-name"))
        .map(n -> "][parentApp=" + n)
        .orElse("");

      String parentSpanIdTag = Optional.ofNullable(tracer.currentSpan())
        .map(span -> span.context().parentId())
        .map(n -> "][parentId=" + n)
        .orElse("");

      return "%s %s%s%s".formatted(
        request.getMethod(), request.getRequestURI(),
        parentAppTag,
        parentSpanIdTag);
    }
}
