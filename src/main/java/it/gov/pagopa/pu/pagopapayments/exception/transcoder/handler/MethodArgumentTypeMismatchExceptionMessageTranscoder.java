package it.gov.pagopa.pu.pagopapayments.exception.transcoder.handler;

import it.gov.pagopa.pu.pagopapayments.dto.generated.ErrorFieldDTO;
import it.gov.pagopa.pu.pagopapayments.dto.generated.PagoPaPaymentsErrorDTO;
import it.gov.pagopa.pu.pagopapayments.exception.transcoder.ExceptionMessageTranscoded;
import it.gov.pagopa.pu.pagopapayments.exception.transcoder.ExceptionMessageTranscoder;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class MethodArgumentTypeMismatchExceptionMessageTranscoder implements ExceptionMessageTranscoder<MethodArgumentTypeMismatchException> {

  @Override
  public ExceptionMessageTranscoded transcode(MethodArgumentTypeMismatchException methodArgumentTypeMismatchException) {
    List<ErrorFieldDTO> errorFields = List.of(
            ErrorFieldDTO.builder()
              .field(methodArgumentTypeMismatchException.getName())
              .error("InvalidValue")
              .message("Cannot convert value '" + methodArgumentTypeMismatchException.getValue() + "' to required type '" + Objects.requireNonNullElse(methodArgumentTypeMismatchException.getRequiredType(), String.class).getSimpleName() + "'")
              .build()
            );

    String errorDescription = errorFields.stream()
      .map(e -> " " + e.getField() + ": " + e.getMessage())
      .collect(Collectors.joining(";"));

    return new ExceptionMessageTranscoded(
      PagoPaPaymentsErrorDTO.CategoryEnum.PAGOPA_PAYMENTS_BAD_REQUEST.getValue(),
      "Invalid request parameter." + errorDescription,
      errorFields
    );
  }
}
