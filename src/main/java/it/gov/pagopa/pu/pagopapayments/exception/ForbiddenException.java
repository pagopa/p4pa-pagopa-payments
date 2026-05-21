package it.gov.pagopa.pu.pagopapayments.exception;

public class ForbiddenException extends BaseBusinessException {
  public ForbiddenException(String code, String message) {
    super(code, message);
  }
}
