package it.gov.pagopa.pu.pagopapayments.config;

import it.gov.pagopa.pu.pagopapayments.util.UtilitiesTest;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

@ExtendWith(MockitoExtension.class)
class TraceIdObservationFilterTest {

    @Mock
    private HttpServletRequest requestMock;
    @Mock
    private HttpServletResponse responseMock;
    @Mock
    private FilterChain filterChainMock;

    private final TraceIdObservationFilter filter = new TraceIdObservationFilter();

    @AfterEach
    void verifyNoMoreInteractions() throws ServletException, IOException {
        UtilitiesTest.clearTraceIdContext();
        Mockito.verify(filterChainMock)
                .doFilter(Mockito.same(requestMock), Mockito.same(responseMock));

        Mockito.verifyNoMoreInteractions(
                requestMock,
                responseMock,
                filterChainMock
        );
    }

    @Test
    void givenNotTraceIdWhendoFilterInternalThenDoNothing() throws ServletException, IOException {
        filter.doFilterInternal(requestMock, responseMock, filterChainMock);

        Mockito.verifyNoInteractions(responseMock);
    }

    @Test
    void givenTraceIdWhendoFilterInternalThenConfigureResponseHeader() throws ServletException, IOException {
        // Given
        String traceId = "TRACEID";
        UtilitiesTest.setTraceId(traceId);

        // When
        filter.doFilterInternal(requestMock, responseMock, filterChainMock);

        // Then
        Mockito.verify(responseMock)
                .setHeader("X-Trace-Id", traceId);
    }
}
