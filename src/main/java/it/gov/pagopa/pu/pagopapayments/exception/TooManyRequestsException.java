package it.gov.pagopa.pu.pagopapayments.exception;

public class TooManyRequestsException extends BaseBusinessException {
  public TooManyRequestsException(String code, String message) {
    super(code, message);
  }
}
