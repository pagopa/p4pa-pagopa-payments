package it.gov.pagopa.pu.pagopapayments.performancelogger;

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
@Order(-101) // Set in order to be executed after ServerHttpObservationFilter (which will handle traceId): configured through properties management.observations.http.server.filter.order
public class ApiRequestPerformanceLogger implements Filter {

    private static final List<String> blackListPathPrefixList = List.of(
            "/actuator",
            "/favicon.ico",
            "/swagger"
    );

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

    static String getRequestDetails(HttpServletRequest request) {
        return "%s %s".formatted(request.getMethod(), request.getRequestURI());
    }
}
