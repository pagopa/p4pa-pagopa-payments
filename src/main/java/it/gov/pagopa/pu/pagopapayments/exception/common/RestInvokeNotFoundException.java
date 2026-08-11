package it.gov.pagopa.pu.pagopapayments.exception.common;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@SuppressWarnings("java:S110") // Suppress "Inheritance tree of classes should not be too deep": allowed for exception hierarchy
public class RestInvokeNotFoundException extends NotFoundException implements RestInvokeHttpClientException {

  private final String applicationName;
  private final HttpStatus httpStatus;
  private final String category;

  public RestInvokeNotFoundException(String applicationName, HttpStatus httpStatus, String category, String code, String message) {
    super(code, message);
    this.applicationName = applicationName;
    this.httpStatus = httpStatus;
    this.category = category;
  }
}
