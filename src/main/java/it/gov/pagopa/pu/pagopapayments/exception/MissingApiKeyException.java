package it.gov.pagopa.pu.pagopapayments.exception;

import it.gov.pagopa.pu.pagopapayments.exception.common.BaseBusinessException;

public class MissingApiKeyException extends BaseBusinessException {

  public MissingApiKeyException(String code, String message) {
    super(code, message);
  }
}
