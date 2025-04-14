package it.gov.pagopa.pu.pagopapayments.config.rest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.net.URI;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QueryParamsPlusEncoderInterceptorTest {

    @Mock
    private HttpRequest mockRequest;
    @Mock
    private ClientHttpRequestExecution mockExecution;

    byte[] mockBody = new byte[0];

    private QueryParamsPlusEncoderInterceptor queryParamsPlusEncoderInterceptor;

    @BeforeEach
    void setUp() {
        queryParamsPlusEncoderInterceptor = new QueryParamsPlusEncoderInterceptor();
    }

    @Test
    void givenRequestWhenInterceptThenUpdateQuery() throws IOException {
        //given
        ClientHttpResponse mockResponse = mock(ClientHttpResponse.class);
        OffsetDateTime offsetDateTime = OffsetDateTime.parse("2025-04-08T11:57:03.375275400+02:00");
        String formattedDateTime = offsetDateTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        URI uri = URI.create("http://example/api/resource?datetime=" + formattedDateTime);

        when(mockRequest.getURI()).thenReturn(uri);
        when(mockExecution.execute(Mockito.any(), Mockito.eq(mockBody))).thenReturn(mockResponse);

        //when
        ClientHttpResponse actualResponse = queryParamsPlusEncoderInterceptor.intercept(mockRequest, mockBody, mockExecution);

        //then
        assertEquals(mockResponse, actualResponse);

        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        Mockito.verify(mockExecution).execute(requestCaptor.capture(), Mockito.eq(mockBody));
        HttpRequest interceptedRequest = requestCaptor.getValue();

        URI interceptedUri = interceptedRequest.getURI();
        assertEquals("http://example/api/resource?datetime=2025-04-08T11:57:03.3752754%2B02:00", interceptedUri.toString());
    }

    @Test
    void givenNullQueryWhenInterceptThenReturnUri() throws IOException {
        //given
        ClientHttpResponse mockResponse = mock(ClientHttpResponse.class);

        URI uri = URI.create("http://example/api/resource");

        when(mockRequest.getURI()).thenReturn(uri);
        when(mockExecution.execute(Mockito.any(), Mockito.eq(mockBody))).thenReturn(mockResponse);

        //when
        ClientHttpResponse actualResponse = queryParamsPlusEncoderInterceptor.intercept(mockRequest, mockBody, mockExecution);

        //then
        assertEquals(mockResponse, actualResponse);

        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        Mockito.verify(mockExecution).execute(requestCaptor.capture(), Mockito.eq(mockBody));
        HttpRequest interceptedRequest = requestCaptor.getValue();

        URI interceptedUri = interceptedRequest.getURI();
        assertEquals("http://example/api/resource", interceptedUri.toString());
    }

}
