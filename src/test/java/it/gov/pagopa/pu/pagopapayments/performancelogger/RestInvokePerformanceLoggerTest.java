package it.gov.pagopa.pu.pagopapayments.performancelogger;

import it.gov.pagopa.pu.pagopapayments.util.MemoryAppender;
import it.gov.pagopa.pu.pagopapayments.util.UtilitiesTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.net.URI;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RestInvokePerformanceLoggerTest {
    public static final String APPENDER_NAME = "REST_INVOKE";

    @Mock
    private HttpRequest httpRequestMock;
    private final byte[] bodyMock = new byte[0];
    @Mock
    private ClientHttpRequestExecution requestExecutionMock;

    private MemoryAppender memoryAppender;

    private RestInvokePerformanceLogger filter;

    private final String spanId = "SPANID";

    @BeforeEach
    void init() {
        filter = new RestInvokePerformanceLogger("APPNAME");

        this.memoryAppender = PerformanceLoggerTest.buildPerformanceLoggerMemoryAppender(APPENDER_NAME);

        UtilitiesTest.setTraceId(null, spanId);
    }

    @AfterEach
    void clear() {
        UtilitiesTest.clearTraceIdContext();
        verifyNoMoreInteractions();
    }

    void verifyNoMoreInteractions() {
        Mockito.verifyNoMoreInteractions(
                httpRequestMock,
                requestExecutionMock
        );
    }

    @Test
    void givenCoveredPathWhenDoFilterThenDontPerformanceLog() throws IOException {
        // Given
        ClientHttpResponse expectedResult = mock(ClientHttpResponse.class);
        HttpHeaders headers = new HttpHeaders();

        when(expectedResult.getStatusCode()).thenReturn(HttpStatus.OK);

        when(requestExecutionMock.execute(Mockito.same(httpRequestMock), Mockito.same(bodyMock)))
                .thenReturn(expectedResult);

        when(httpRequestMock.getMethod()).thenReturn(HttpMethod.GET);
        when(httpRequestMock.getURI()).thenReturn(URI.create("/api/test"));
        when(httpRequestMock.getHeaders()).thenReturn(headers);

        // When
        ClientHttpResponse result = filter.intercept(httpRequestMock, bodyMock, requestExecutionMock);

        // Then
        PerformanceLoggerTest.assertPerformanceLogMessage(APPENDER_NAME, "GET /api/test]\\[spanId=" + spanId, "HttpStatus: 200", memoryAppender);
        Assertions.assertEquals("APPNAME", headers.getFirst(RestInvokePerformanceLogger.REST_INVOKE_HEADER_APP_NAME));

        Assertions.assertSame(expectedResult, result);
    }

}
