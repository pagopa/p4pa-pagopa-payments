package it.gov.pagopa.pu.pagopapayments.exception.common;

import lombok.Getter;

@Getter
@SuppressWarnings("java:S110") // Suppress "Inheritance tree of classes should not be too deep": allowed for exception hierarchy
public class RestInvokeForbiddenException extends ForbiddenException {

  private final String applicationName;
  private final String category;

  public RestInvokeForbiddenException(String applicationName, String category, String code, String message) {
    super(code, message);
    this.applicationName = applicationName;
    this.category = category;
  }
}
