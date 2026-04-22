package it.gov.pagopa.pu.pagopapayments.exception;

public class InvalidValueException extends BaseBusinessException{
  public InvalidValueException(String code, String message) {
    super(code, message);
  }
}
