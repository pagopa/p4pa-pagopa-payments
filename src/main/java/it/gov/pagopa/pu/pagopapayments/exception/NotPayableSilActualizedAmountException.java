package it.gov.pagopa.pu.pagopapayments.exception;

import it.gov.pagopa.pu.pagopapayments.util.ErrorCodeConstants;

public class NotPayableSilActualizedAmountException extends BaseBusinessException{

  public NotPayableSilActualizedAmountException(String message) {
    this(message, null);
  }

  public NotPayableSilActualizedAmountException(String message, Throwable cause) {
    super(ErrorCodeConstants.ERROR_CODE_ACTUALIZATION_ERROR, message, cause);
  }
}
