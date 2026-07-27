package it.gov.pagopa.pu.pagopapayments.exception;

public class MissingApiKeyException extends BaseBusinessException {

  public MissingApiKeyException(String code, String message) {
    super(code, message);
  }
}
