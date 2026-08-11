package it.gov.pagopa.pu.pagopapayments.exception.transcoder.handler;

import it.gov.pagopa.pu.pagopapayments.dto.generated.PagoPaPaymentsErrorDTO;
import it.gov.pagopa.pu.pagopapayments.dto.generated.ErrorFieldDTO;
import it.gov.pagopa.pu.pagopapayments.exception.transcoder.ExceptionMessageTranscoded;
import it.gov.pagopa.pu.pagopapayments.exception.transcoder.ExceptionMessageTranscoder;
import org.springframework.http.converter.HttpMessageNotReadableException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DatabindException;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class HttpMessageNotReadableExceptionMessageTranscoder implements ExceptionMessageTranscoder<HttpMessageNotReadableException> {

  @Override
  public ExceptionMessageTranscoded transcode(HttpMessageNotReadableException httpMessageNotReadableException) {
    String errorMsg = "Required request body is missing";
    List<ErrorFieldDTO> fields = null;
    if (httpMessageNotReadableException.getCause() instanceof DatabindException jsonMappingException) {
      String errorPath = jsonMappingException.getPath().stream()
        .map(JacksonException.Reference::getPropertyName)
        .collect(Collectors.joining("."));

      String errorCode = Objects.requireNonNullElse(jsonMappingException.getCause(), jsonMappingException)
        .getClass().getSimpleName().replace("Exception", "");

      ErrorFieldDTO errorField = new ErrorFieldDTO(
        errorPath,
        errorCode,
        jsonMappingException.getOriginalMessage());

      errorMsg = "Cannot parse body. " + errorPath + ": " + errorField.getMessage();
      fields = List.of(errorField);
    } else if (httpMessageNotReadableException.getCause() instanceof JacksonException jacksonException) {
      errorMsg = "Cannot parse body. " + jacksonException.getOriginalMessage();
    }
    return new ExceptionMessageTranscoded(PagoPaPaymentsErrorDTO.CategoryEnum.PAGOPA_PAYMENTS_BAD_REQUEST.getValue(), errorMsg, fields);
  }
}
