package it.gov.pagopa.pu.pagopapayments.config.rest;

import it.gov.pagopa.pu.pagopapayments.config.json.JsonConfig;
import it.gov.pagopa.pu.pagopapayments.exception.BaseBusinessException;
import it.gov.pagopa.pu.pagopapayments.exception.ConflictException;
import it.gov.pagopa.pu.pagopapayments.exception.InvalidValueException;
import it.gov.pagopa.pu.pagopapayments.exception.TooManyRequestsException;
import it.gov.pagopa.pu.registries.dto.generated.CategoryEnum;
import it.gov.pagopa.pu.registries.dto.generated.ErrorDTO;
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
import java.util.Map;
import java.util.function.BiFunction;

class HttpClientErrorHandlerTest {

  private final JsonMapper jsonMapper = new JsonConfig().objectMapperJackson3();

  HttpClientErrorHandlerTest() throws URISyntaxException {
  }

  private HttpClientErrorHandler<ErrorDTO> buildHttpClientErrorHandler(boolean bodyPrinterWhenError) {
    return new HttpClientErrorHandler<>(jsonMapper, "APPNAME", bodyPrinterWhenError,
      ErrorDTO.class, ErrorDTO::getCode, ErrorDTO::getMessage);
  }

  private final URI url = new URI("http://www.sample.com");
  private final ErrorDTO expectedErrorDTO = new ErrorDTO(CategoryEnum.BAD_REQUEST, "BADREQUEST", "MESSAGE", "TRACEID");

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void testNo4xxException(boolean bodyPrinterWhenError) {
    // Given
    HttpClientErrorHandler<ErrorDTO> httpClientHandler = buildHttpClientErrorHandler(bodyPrinterWhenError);
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
    HttpClientErrorHandler<ErrorDTO> httpClientHandler = buildHttpClientErrorHandler(bodyPrinterWhenError);
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
    HttpClientErrorHandler<ErrorDTO> httpClientHandler = buildHttpClientErrorHandler(bodyPrinterWhenError);
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
    HttpClientErrorHandler<ErrorDTO> httpClientHandler = buildHttpClientErrorHandler(bodyPrinterWhenError);
    try (MockClientHttpResponse response = new MockClientHttpResponse(jsonMapper.writeValueAsBytes(expectedErrorDTO), HttpStatus.BAD_REQUEST)) {

      // When
      InvalidValueException result = Assertions.assertThrows(InvalidValueException.class, () -> httpClientHandler.handleError(url, HttpMethod.GET, response));

      // Then
      Assertions.assertEquals(expectedErrorDTO.getCode(), result.getCode());
      Assertions.assertEquals(expectedErrorDTO.getMessage(), result.getMessage());
    }
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void testNoJsonBodyException(boolean bodyPrinterWhenError) {
    // Given
    HttpClientErrorHandler<ErrorDTO> httpClientHandler = buildHttpClientErrorHandler(bodyPrinterWhenError);
    try (MockClientHttpResponse response = new MockClientHttpResponse("INVALIDJSON".getBytes(), HttpStatus.BAD_REQUEST)) {

      // When
      HttpClientErrorException.BadRequest result = Assertions.assertThrows(HttpClientErrorException.BadRequest.class, () -> httpClientHandler.handleError(url, HttpMethod.GET, response));

      // Then
      Assertions.assertEquals("400 Bad Request on GET request for \"http://www.sample.com\": \"INVALIDJSON\"", result.getMessage());
    }
  }

  @Test
  void testBuildDefaultHttpClientExceptionTranscoder(){
    BiFunction<HttpStatusCodeException, ErrorDTO, RuntimeException> httpErrorTranscoder = HttpClientErrorHandler.buildDefaultHttpClientExceptionTranscoder("TEST", ErrorDTO::getCode, ErrorDTO::getMessage);
    ErrorDTO errorDTO = new ErrorDTO(null, "BAD_REQUEST", "MESSAGE", null);
    Map<HttpStatus, Class<? extends BaseBusinessException>> httpStatus2ExpectedException = Map.of(
      HttpStatus.CONFLICT, ConflictException.class,
      HttpStatus.TOO_MANY_REQUESTS, TooManyRequestsException.class
    );

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
    BiFunction<HttpStatusCodeException, ErrorDTO, RuntimeException> httpErrorTranscoder = HttpClientErrorHandler.buildDefaultHttpClientExceptionTranscoder("TEST", null, ErrorDTO::getMessage);
    ErrorDTO errorDTO = new ErrorDTO(null, "BAD_REQUEST", "MESSAGE", null);
    Map<HttpStatus, Class<? extends BaseBusinessException>> httpStatus2ExpectedException = Map.of(
      HttpStatus.CONFLICT, ConflictException.class,
      HttpStatus.TOO_MANY_REQUESTS, TooManyRequestsException.class
    );

    for (HttpStatus httpStatus : HttpStatus.values()) {
      RuntimeException result = httpErrorTranscoder
        .apply(new HttpClientErrorException(httpStatus), errorDTO);

      Assertions.assertInstanceOf(BaseBusinessException.class, result);
      Assertions.assertSame("TEST_" + httpStatus.getReasonPhrase().toUpperCase(), ((BaseBusinessException)result).getCode());
      Assertions.assertSame(errorDTO.getMessage(), result.getMessage());

      Class<? extends BaseBusinessException> expectedException = httpStatus2ExpectedException.getOrDefault(httpStatus, InvalidValueException.class);
      Assertions.assertInstanceOf(expectedException, result);
    }
  }
}
