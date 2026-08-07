package it.gov.pagopa.pu.pagopapayments.exception.common;

import it.gov.pagopa.pu.pagopapayments.dto.generated.ErrorFieldDTO;
import lombok.Getter;

import java.util.List;

@Getter
@SuppressWarnings("java:S110") // Suppress "Inheritance tree of classes should not be too deep": allowed for exception hierarchy
public class RestInvokeInvalidValueException extends InvalidValueException {

  private final String applicationName;
  private final String category;

  public RestInvokeInvalidValueException(String applicationName, String category, String code, String message, List<ErrorFieldDTO> fields) {
    super(code, message, fields);
    this.applicationName = applicationName;
    this.category = category;
  }
}
