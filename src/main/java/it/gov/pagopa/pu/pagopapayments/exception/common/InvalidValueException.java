package it.gov.pagopa.pu.pagopapayments.exception.common;

import it.gov.pagopa.pu.pagopapayments.dto.generated.ErrorFieldDTO;

import java.util.List;

public class InvalidValueException extends BaseBusinessException {

  public InvalidValueException(String code, String message) {
    this(code, message, null, null);
  }

  public InvalidValueException(String code, String message, List<ErrorFieldDTO> fieldErrors) {
    this(code, message, fieldErrors, null);
  }

  public InvalidValueException(String code, String message, Throwable cause) {
    this(code, message, null, cause);
  }

  public InvalidValueException(String code, String message, List<ErrorFieldDTO> fieldErrors, Throwable cause) {
    super(code, message, fieldErrors, cause);
  }
}
