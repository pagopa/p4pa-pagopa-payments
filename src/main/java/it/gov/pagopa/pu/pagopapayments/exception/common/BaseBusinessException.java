package it.gov.pagopa.pu.pagopapayments.exception.common;

import it.gov.pagopa.pu.pagopapayments.dto.generated.ErrorFieldDTO;
import lombok.Getter;

import java.util.List;

@Getter
public abstract class BaseBusinessException extends RuntimeException {

  protected final String code;
  protected final List<ErrorFieldDTO> fields;

  protected BaseBusinessException(String code, String message) {
    this(code, message, null);
  }

  protected BaseBusinessException(String code, String message, Throwable cause) {
    this(code, message, null, cause);
  }
  protected BaseBusinessException(String code, String message, List<ErrorFieldDTO> fields, Throwable cause) {
    super(message, cause);
    this.code = code;
    this.fields = fields;
  }
}
