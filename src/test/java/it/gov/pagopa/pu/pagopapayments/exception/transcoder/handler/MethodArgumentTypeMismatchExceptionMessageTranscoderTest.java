package it.gov.pagopa.pu.pagopapayments.exception.transcoder.handler;

import it.gov.pagopa.pu.pagopapayments.dto.generated.ErrorFieldDTO;
import it.gov.pagopa.pu.pagopapayments.exception.transcoder.ExceptionMessageTranscoded;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;

import static org.mockito.Mockito.mock;

class MethodArgumentTypeMismatchExceptionMessageTranscoderTest {

  private final MethodArgumentTypeMismatchExceptionMessageTranscoder transcoder = new MethodArgumentTypeMismatchExceptionMessageTranscoder();

  @Test
  void testTranscode() {
    // Given
    MethodArgumentTypeMismatchException exception = new MethodArgumentTypeMismatchException(
      "invalidValue",
      String.class,
      "fieldName",
      mock(MethodParameter.class),
      new IllegalArgumentException("Invalid value")
    );

    // When
    ExceptionMessageTranscoded result = transcoder.transcode(exception);

    // Then
    Assertions.assertEquals(
      new ExceptionMessageTranscoded(
        "PAGOPA_PAYMENTS_BAD_REQUEST",
        "Invalid request parameter. fieldName: Cannot convert value 'invalidValue' to required type 'String'",
        List.of(
          new ErrorFieldDTO("fieldName", "InvalidValue", "Cannot convert value 'invalidValue' to required type 'String'")
        )
      ),
      result
    );
  }
}
