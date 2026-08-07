package it.gov.pagopa.pu.pagopapayments.config.rest;

import it.gov.pagopa.pu.pagopapayments.config.json.JsonConfig;
import it.gov.pagopa.pu.pagopapayments.dto.generated.ErrorFieldDTO;
import it.gov.pagopa.pu.pagopapayments.dto.generated.PagoPaPaymentsErrorDTO;
import it.gov.pagopa.pu.pagopapayments.dto.generated.PagoPaPaymentsErrorDTO.CategoryEnum;
import it.gov.pagopa.pu.pagopapayments.exception.common.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.client.MockClientHttpResponse;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import tools.jackson.databind.json.JsonMapper;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

class HttpClientErrorJsonBodyHandlerTest {

  private final JsonMapper jsonMapper = new JsonConfig().objectMapperJackson3();

  HttpClientErrorJsonBodyHandlerTest() throws URISyntaxException {
  }

  private HttpClientErrorJsonBodyHandler<PagoPaPaymentsErrorDTO> buildHttpClientErrorHandler(boolean bodyPrinterWhenError) {
    return new HttpClientErrorJsonBodyHandler<>(jsonMapper, "APPNAME", bodyPrinterWhenError,
      PagoPaPaymentsErrorDTO.class, e -> new PuErrorDTO(e.getCategory().getValue(), e.getCode(), e.getMessage(), e.getFields()));
  }

  private final URI url = new URI("http://www.sample.com");
  private final PagoPaPaymentsErrorDTO expectedErrorDTO = new PagoPaPaymentsErrorDTO(CategoryEnum.PAGOPA_PAYMENTS_BAD_REQUEST, "BADREQUEST", "MESSAGE", List.of(new ErrorFieldDTO("FIELD", "FIELDERRORCODE", "FIELDERRORMESSAGE")), "TRACEID");

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void testNo4xxException(boolean bodyPrinterWhenError) {
    // Given
    HttpClientErrorJsonBodyHandler<PagoPaPaymentsErrorDTO> httpClientHandler = buildHttpClientErrorHandler(bodyPrinterWhenError);
    try (MockClientHttpResponse response = new MockClientHttpResponse(new byte[0], HttpStatus.SERVICE_UNAVAILABLE)) {

      // When
      HttpServerErrorException.ServiceUnavailable result = Assertions.assertThrows(HttpServerErrorException.ServiceUnavailable.class, () -> httpClientHandler.handleError(url, HttpMethod.GET, response));

      // Then
      Assertions.assertEquals("503 Service Unavailable on GET request for \"http://www.sample.com\": [no body]", result.getMessage());
    }
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void testNoBodyException(boolean bodyPrinterWhenError) {
    // Given
    HttpClientErrorJsonBodyHandler<PagoPaPaymentsErrorDTO> httpClientHandler = buildHttpClientErrorHandler(bodyPrinterWhenError);
    try (MockClientHttpResponse response = new MockClientHttpResponse(new byte[0], HttpStatus.BAD_REQUEST)) {

      // When
      HttpClientErrorException.BadRequest result = Assertions.assertThrows(HttpClientErrorException.BadRequest.class, () -> httpClientHandler.handleError(url, HttpMethod.GET, response));

      // Then
      Assertions.assertEquals("400 Bad Request on GET request for \"http://www.sample.com\": [no body]", result.getMessage());
    }
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void testNotFoundException(boolean bodyPrinterWhenError) {
    // Given
    HttpClientErrorJsonBodyHandler<PagoPaPaymentsErrorDTO> httpClientHandler = buildHttpClientErrorHandler(bodyPrinterWhenError);
    try (MockClientHttpResponse response = new MockClientHttpResponse(new byte[0], HttpStatus.NOT_FOUND)) {

      // When
      HttpClientErrorException.NotFound result = Assertions.assertThrows(HttpClientErrorException.NotFound.class, () -> httpClientHandler.handleError(url, HttpMethod.GET, response));

      // Then
      Assertions.assertEquals("404 Not Found on GET request for \"http://www.sample.com\": [no body]", result.getMessage());
    }
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void testBodyException(boolean bodyPrinterWhenError) {
    // Given
    HttpClientErrorJsonBodyHandler<PagoPaPaymentsErrorDTO> httpClientHandler = buildHttpClientErrorHandler(bodyPrinterWhenError);
    try (MockClientHttpResponse response = new MockClientHttpResponse(jsonMapper.writeValueAsBytes(expectedErrorDTO), HttpStatus.BAD_REQUEST)) {

      // When
      RestInvokeInvalidValueException result = Assertions.assertThrows(RestInvokeInvalidValueException.class, () -> httpClientHandler.handleError(url, HttpMethod.GET, response));

      // Then
      Assertions.assertEquals("APPNAME", result.getApplicationName());
      Assertions.assertEquals(expectedErrorDTO.getCategory().getValue(), result.getCategory());
      Assertions.assertEquals(expectedErrorDTO.getCode(), result.getCode());
      Assertions.assertEquals(expectedErrorDTO.getMessage(), result.getMessage());
      Assertions.assertEquals(expectedErrorDTO.getFields(), result.getFields());
    }
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void testNoJsonBodyException(boolean bodyPrinterWhenError) {
    // Given
    HttpClientErrorJsonBodyHandler<PagoPaPaymentsErrorDTO> httpClientHandler = buildHttpClientErrorHandler(bodyPrinterWhenError);
    try (MockClientHttpResponse response = new MockClientHttpResponse("INVALIDJSON".getBytes(), HttpStatus.BAD_REQUEST)) {

      // When
      HttpClientErrorException.BadRequest result = Assertions.assertThrows(HttpClientErrorException.BadRequest.class, () -> httpClientHandler.handleError(url, HttpMethod.GET, response));

      // Then
      Assertions.assertEquals("400 Bad Request on GET request for \"http://www.sample.com\": \"INVALIDJSON\"", result.getMessage());
    }
  }


  private final Map<HttpStatus, Class<? extends BaseBusinessException>> httpStatus2ExpectedException = Map.of(
    HttpStatus.CONFLICT, RestInvokeConflictException.class,
    HttpStatus.FORBIDDEN, RestInvokeForbiddenException.class,
    HttpStatus.UNAUTHORIZED, RestInvokeNotAuthorizedException.class
  );

  @Test
  void testBuildDefaultHttpClientExceptionTranscoder(){
    BiFunction<HttpStatusCodeException, PagoPaPaymentsErrorDTO, RuntimeException> httpErrorTranscoder = HttpClientErrorJsonBodyHandler.buildDefaultHttpClientExceptionTranscoder("TEST", PagoPaPaymentsErrorDTO::getCode, PagoPaPaymentsErrorDTO::getMessage);
    PagoPaPaymentsErrorDTO errorDTO = new PagoPaPaymentsErrorDTO(null, "BAD_REQUEST", "MESSAGE", null, null);

    for (HttpStatus httpStatus : HttpStatus.values()) {
      RuntimeException result = httpErrorTranscoder
        .apply(new HttpClientErrorException(httpStatus), errorDTO);

      Assertions.assertInstanceOf(BaseBusinessException.class, result);
      Assertions.assertSame(errorDTO.getCode(), ((BaseBusinessException)result).getCode());
      Assertions.assertSame(errorDTO.getMessage(), result.getMessage());

      Class<? extends BaseBusinessException> expectedException = httpStatus2ExpectedException.getOrDefault(httpStatus, InvalidValueException.class);
      Assertions.assertInstanceOf(expectedException, result);
    }
  }

  @Test
  void testBuildDefaultHttpClientExceptionTranscoder_noErrorCodeFunction(){
    BiFunction<HttpStatusCodeException, PagoPaPaymentsErrorDTO, RuntimeException> httpErrorTranscoder = HttpClientErrorJsonBodyHandler.buildDefaultHttpClientExceptionTranscoder("TEST", null, PagoPaPaymentsErrorDTO::getMessage);
    PagoPaPaymentsErrorDTO errorDTO = new PagoPaPaymentsErrorDTO(null, "BAD_REQUEST", "MESSAGE", null, null);

    for (HttpStatus httpStatus : HttpStatus.values()) {
      RuntimeException result = httpErrorTranscoder
        .apply(new HttpClientErrorException(httpStatus), errorDTO);

      Assertions.assertInstanceOf(BaseBusinessException.class, result);
      Assertions.assertEquals(
        "TEST_" + httpStatus.name(),
        ((BaseBusinessException)result).getCode());
      Assertions.assertSame(errorDTO.getMessage(), result.getMessage());

      Class<? extends BaseBusinessException> expectedException = httpStatus2ExpectedException.getOrDefault(httpStatus, InvalidValueException.class);
      Assertions.assertInstanceOf(expectedException, result);
    }
  }
}
