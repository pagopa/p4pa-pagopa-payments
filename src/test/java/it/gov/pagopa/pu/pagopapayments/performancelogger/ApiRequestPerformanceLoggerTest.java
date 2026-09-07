package it.gov.pagopa.pu.pagopapayments.performancelogger;

import it.gov.pagopa.pu.pagopapayments.util.MemoryAppender;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApiRequestPerformanceLoggerTest {
    public static final String APPENDER_NAME = "API_REQUEST";

    private ServletRequest httpServletRequestMock;
    private ServletResponse httpServletResponseMock;
    @Mock
    private FilterChain filterChainMock;

    private MemoryAppender memoryAppender;

    private ApiRequestPerformanceLogger filter;

    @BeforeEach
    void init() {
        httpServletRequestMock = mock(HttpServletRequest.class);
        httpServletResponseMock = mock(HttpServletResponse.class);
        filter = new ApiRequestPerformanceLogger();

        this.memoryAppender = PerformanceLoggerTest.buildPerformanceLoggerMemoryAppender(APPENDER_NAME);
    }

    @AfterEach
    void verifyNoMoreInteractions() throws ServletException, IOException {
        verify(filterChainMock)
                .doFilter(httpServletRequestMock, httpServletResponseMock);

        Mockito.verifyNoMoreInteractions(
                httpServletRequestMock,
                httpServletResponseMock,
                filterChainMock
        );
    }

    @Test
    void givenNotHttpServletRequestWhenDoFilterThenDontPerformanceLog() throws ServletException, IOException {
        // Given
        httpServletRequestMock = mock(ServletRequest.class);

        // When
        filter.doFilter(httpServletRequestMock, httpServletResponseMock, filterChainMock);

        // Then
        Assertions.assertEquals(0, memoryAppender.getLoggedEvents().size());
    }

    @Test
    void givenNotHttpServletResponseWhenDoFilterThenDontPerformanceLog() throws ServletException, IOException {
        // Given
        httpServletResponseMock = mock(ServletResponse.class);

        // When
        filter.doFilter(httpServletRequestMock, httpServletResponseMock, filterChainMock);

        // Then
        Assertions.assertEquals(0, memoryAppender.getLoggedEvents().size());
    }

    @Test
    void givenNotCoveredPathWhenDoFilterThenDontPerformanceLog() throws ServletException, IOException {
        // Given
        configureRequestPath("/actuator");

        // When
        filter.doFilter(httpServletRequestMock, httpServletResponseMock, filterChainMock);

        // Then
        Assertions.assertEquals(0, memoryAppender.getLoggedEvents().size());
    }

    @Test
    void givenCoveredPathNoRestInvokeHeadersWhenDoFilterThenDontPerformanceLog() throws ServletException, IOException {
        // Given
        configureRequestPath("/api/test");
        when(((HttpServletRequest)httpServletRequestMock).getHeader(RestInvokePerformanceLogger.REST_INVOKE_HEADER_APP_NAME))
                .thenReturn(null);
        when(((HttpServletRequest)httpServletRequestMock).getHeader(RestInvokePerformanceLogger.REST_INVOKE_HEADER_CORRELATION_ID))
                .thenReturn(null);

        // When
        filter.doFilter(httpServletRequestMock, httpServletResponseMock, filterChainMock);

        // Then
        PerformanceLoggerTest.assertPerformanceLogMessage(APPENDER_NAME, "GET /api/test", "HttpStatus: 200", memoryAppender);

        verify(((HttpServletRequest)httpServletRequestMock), times(2))
                .getRequestURI();
        verify(((HttpServletRequest)httpServletRequestMock), times(1))
                .getMethod();
        verify(((HttpServletResponse)httpServletResponseMock))
                .getStatus();
    }

    @Test
    void givenCoveredPathHeadersWhenDoFilterThenDontPerformanceLog() throws ServletException, IOException {
        // Given
        configureRequestPath("/api/test");

        when(((HttpServletRequest)httpServletRequestMock).getHeader(RestInvokePerformanceLogger.REST_INVOKE_HEADER_APP_NAME))
                .thenReturn("RESTINVOKEAPPNAME");
        when(((HttpServletRequest)httpServletRequestMock).getHeader(RestInvokePerformanceLogger.REST_INVOKE_HEADER_CORRELATION_ID))
                .thenReturn("PARENTSPANID");

        // When
        filter.doFilter(httpServletRequestMock, httpServletResponseMock, filterChainMock);

        // Then
        PerformanceLoggerTest.assertPerformanceLogMessage(APPENDER_NAME, "GET /api/test]\\[parentApp=RESTINVOKEAPPNAME]\\[parentId=PARENTSPANID", "HttpStatus: 200", memoryAppender);

        verify(((HttpServletRequest)httpServletRequestMock), times(2))
                .getRequestURI();
        verify(((HttpServletRequest)httpServletRequestMock), times(1))
                .getMethod();
        verify(((HttpServletResponse)httpServletResponseMock))
                .getStatus();
    }

    private void configureRequestPath(String path) {
        when(((HttpServletRequest)httpServletRequestMock).getRequestURI())
                .thenReturn(path);
        Mockito.lenient().when(((HttpServletRequest) httpServletRequestMock).getMethod())
                .thenReturn("GET");
        Mockito.lenient().when(((HttpServletResponse)httpServletResponseMock).getStatus())
                .thenReturn(200);
    }

}
