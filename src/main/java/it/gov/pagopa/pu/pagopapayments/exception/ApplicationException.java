package it.gov.pagopa.pu.pagopapayments.exception;

public class ApplicationException extends BaseBusinessException{
  public ApplicationException(String code, String message) {
    this(code, message, null);
  }

  public ApplicationException(String code, String message, Throwable cause) {
    super(code, message, cause);
  }
}
