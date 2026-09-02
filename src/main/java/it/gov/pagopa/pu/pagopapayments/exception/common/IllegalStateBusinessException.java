package it.gov.pagopa.pu.pagopapayments.exception.common;

public class IllegalStateBusinessException extends BaseBusinessException {
  public IllegalStateBusinessException(String code, String message) {
    this(code, message, null);
  }

  public IllegalStateBusinessException(String code, String message, Throwable cause) {
    super(code, message, cause);
  }
}
