package it.gov.pagopa.pu.pagopapayments.exception.transcoder.handler;

import it.gov.pagopa.pu.pagopapayments.config.json.JsonConfig;
import it.gov.pagopa.pu.pagopapayments.dto.generated.ErrorFieldDTO;
import it.gov.pagopa.pu.pagopapayments.exception.transcoder.ExceptionMessageTranscoded;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageNotReadableException;
import tools.jackson.core.exc.StreamReadException;
import tools.jackson.databind.DatabindException;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.mock;

class HttpMessageNotReadableExceptionMessageTranscoderTest {

  private final HttpMessageNotReadableExceptionMessageTranscoder transcoder = new HttpMessageNotReadableExceptionMessageTranscoder();

  @Test
  void givenNoInputMessageWhenTranscodeThenReturnMissingBodyMessage() {
    // Given
    HttpMessageNotReadableException exception = new HttpMessageNotReadableException("", mock(HttpInputMessage.class));

    // When
    ExceptionMessageTranscoded result = transcoder.transcode(exception);

    // Then
    Assertions.assertEquals(
      new ExceptionMessageTranscoded(
        "PAGOPA_PAYMENTS_BAD_REQUEST",
        "Required request body is missing",
        null),
      result);
  }

  @Test
  void givenUnparsableMessageWhenTranscodeThenReturnUnparsableBodyMessage() {
    // Given
    HttpMessageNotReadableException exception = new HttpMessageNotReadableException("", new StreamReadException("read exception message"), mock(HttpInputMessage.class));

    // When
    ExceptionMessageTranscoded result = transcoder.transcode(exception);

    // Then
    Assertions.assertEquals(
      new ExceptionMessageTranscoded(
        "PAGOPA_PAYMENTS_BAD_REQUEST",
        "Cannot parse body. read exception message",
        null),
      result);
  }

  @Data
  @NoArgsConstructor
  private static class SampleDTO {
    private LocalDateTime dateTimeField;
    private Integer integerField;
  }

  @ParameterizedTest
  @CsvSource({
    //'invalidJsonBody'                        , fieldName    , errorCode    , 'errorMessage'
    " '{\"dateTimeField\": \"invalid-date\"}'  , dateTimeField, DateTimeParse, 'Text ''invalid-date'' could not be parsed at index 0'",
    " '{\"integerField\": \"invalid-integer\"}', integerField , InvalidFormat, 'Cannot deserialize value of type `java.lang.Integer` from String \"invalid-integer\": not a valid `java.lang.Integer` value'",
  })
  void givenDataBindExceptionWhenTranscodeThenReturnUnparsableBodyMessage(String invalidJsonBody, String fieldName, String expectedError, String expectedMessage) {
    // Given
    HttpMessageNotReadableException exception = new HttpMessageNotReadableException("", buildDatabindException(invalidJsonBody), mock(HttpInputMessage.class));

    // When
    ExceptionMessageTranscoded result = transcoder.transcode(exception);

    // Then
    Assertions.assertEquals(
      new ExceptionMessageTranscoded(
        "PAGOPA_PAYMENTS_BAD_REQUEST",
        "Cannot parse body. " + fieldName + ": " + expectedMessage,
        List.of(
          new ErrorFieldDTO(fieldName, expectedError, expectedMessage)
        )),
      result);
  }

  private DatabindException buildDatabindException(String invalidJsonBody) {
    try {
      new JsonConfig().objectMapperJackson3().readerFor(SampleDTO.class).readValue(invalidJsonBody);
    } catch (DatabindException e) {
      return e;
    }
    throw new IllegalStateException("DatabindException was expected but not thrown");
  }
}
