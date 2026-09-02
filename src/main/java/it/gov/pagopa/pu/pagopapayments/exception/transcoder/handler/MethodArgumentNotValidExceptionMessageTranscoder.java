package it.gov.pagopa.pu.pagopapayments.exception.transcoder.handler;

import it.gov.pagopa.pu.pagopapayments.dto.generated.PagoPaPaymentsErrorDTO;
import it.gov.pagopa.pu.pagopapayments.dto.generated.ErrorFieldDTO;
import it.gov.pagopa.pu.pagopapayments.exception.transcoder.ExceptionMessageTranscoded;
import it.gov.pagopa.pu.pagopapayments.exception.transcoder.ExceptionMessageTranscoder;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class MethodArgumentNotValidExceptionMessageTranscoder implements ExceptionMessageTranscoder<MethodArgumentNotValidException> {

  @Override
  public ExceptionMessageTranscoded transcode(MethodArgumentNotValidException methodArgumentNotValidException) {
    List<ErrorFieldDTO> errorFields = methodArgumentNotValidException.getBindingResult()
      .getAllErrors().stream()
      .map(e -> (ErrorFieldDTO)ErrorFieldDTO.builder()
        .field(e instanceof FieldError fieldError ? fieldError.getField() : e.getObjectName())
        .error(Objects.requireNonNullElse(e.getCode(), "InvalidValue"))
        .message(Objects.requireNonNullElse(e.getDefaultMessage(), "value not valid"))
        .build()
      )
      .sorted(Comparator.comparing(ErrorFieldDTO::getField))
      .toList();

    String errorDescription = errorFields.stream()
      .map(e -> " " + e.getField() + ": " + e.getMessage())
      .collect(Collectors.joining(";"));

    return new ExceptionMessageTranscoded(
      PagoPaPaymentsErrorDTO.CategoryEnum.PAGOPA_PAYMENTS_BAD_REQUEST.getValue(),
      "Invalid request content." + errorDescription,
      errorFields
    );
  }
}
