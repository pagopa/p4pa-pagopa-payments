package it.gov.pagopa.pu.pagopapayments.performancelogger;

import io.vavr.CheckedFunction3;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

/**
 * It will execute {@link PerformanceLogger} on each Api request
 */
@Service
@Order(-101)
// Set in order to be executed after ServerHttpObservationFilter (which will handle traceId): configured through properties management.observations.http.server.filter.order
public class ApiRequestPerformanceLogger implements Filter {

  private static final List<String> blackListPathPrefixList = List.of(
    "/actuator",
    "/favicon.ico",
    "/swagger"
  );

  private final CheckedFunction3<ServletRequest, ServletResponse, FilterChain, String> apiRequestHandler =
    (ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) -> {
      filterChain.doFilter(servletRequest, servletResponse);
      return "ok";
    };

  @Override
  public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws ServletException, IOException {
    if (servletRequest instanceof HttpServletRequest httpServletRequest &&
      servletResponse instanceof HttpServletResponse httpServletResponse &&
      isPerformanceLoggedRequest(httpServletRequest)
    ) {
      PerformanceLogger.execute3(
        "API_REQUEST",
        getRequestDetails(httpServletRequest),
        apiRequestHandler,
        x -> "HttpStatus: " + httpServletResponse.getStatus(),
        null,
        servletRequest, servletResponse, filterChain);
    } else {
      filterChain.doFilter(servletRequest, servletResponse);
    }
  }

  private boolean isPerformanceLoggedRequest(HttpServletRequest httpServletRequest) {
    String requestURI = httpServletRequest.getRequestURI();
    return blackListPathPrefixList.stream()
      .noneMatch(requestURI::startsWith);
  }

  static String getRequestDetails(HttpServletRequest request) {
    String soapAction = request.getHeader("soapaction");
    return "%s %s%s".formatted(
      request.getMethod(),
      request.getRequestURI(),
      soapAction != null
        ? "][SOAPACTION " + soapAction
        : "");
  }
}
