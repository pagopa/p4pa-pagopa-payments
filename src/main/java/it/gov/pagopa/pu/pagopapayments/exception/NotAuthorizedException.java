package it.gov.pagopa.pu.pagopapayments.exception;

public class NotAuthorizedException extends BaseBusinessException {
  public NotAuthorizedException(String code, String message) {
    super(code, message);
  }
}
