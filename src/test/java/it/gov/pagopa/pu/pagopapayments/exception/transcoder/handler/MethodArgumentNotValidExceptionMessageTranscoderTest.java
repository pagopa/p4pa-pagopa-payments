package it.gov.pagopa.pu.pagopapayments.exception.transcoder.handler;

import it.gov.pagopa.pu.pagopapayments.dto.generated.ErrorFieldDTO;
import it.gov.pagopa.pu.pagopapayments.exception.transcoder.ExceptionMessageTranscoded;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.mockito.Mockito.mock;

class MethodArgumentNotValidExceptionMessageTranscoderTest {

  private final MethodArgumentNotValidExceptionMessageTranscoder transcoder = new MethodArgumentNotValidExceptionMessageTranscoder();

  @Test
  void testTranscode() {
    // Given
    BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(null, "dummy");
    bindingResult.addError(new ObjectError("objectName1", new String[]{"errorCode"}, null, "object error message"));
    bindingResult.addError(new FieldError("objectName2", "fieldName2", null, true, new String[]{"errorCode2"}, null, "object2 error message"));
    bindingResult.addError(new ObjectError("objectName3", null));

    MethodArgumentNotValidException exception = new MethodArgumentNotValidException(mock(MethodParameter.class), bindingResult);

    // When
    ExceptionMessageTranscoded result = transcoder.transcode(exception);

    // Then
    Assertions.assertEquals(
      new ExceptionMessageTranscoded(
        "PAGOPA_PAYMENTS_BAD_REQUEST",
        "Invalid request content. fieldName2: object2 error message; objectName1: object error message; objectName3: value not valid",
        List.of(
          new ErrorFieldDTO("fieldName2", "errorCode2", "object2 error message"),
          new ErrorFieldDTO("objectName1", "errorCode", "object error message"),
          new ErrorFieldDTO("objectName3", "InvalidValue", "value not valid")
        )
      ),
      result
    );
  }
}
