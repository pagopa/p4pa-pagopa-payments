package it.gov.pagopa.pu.pagopapayments.exception;

import it.gov.pagopa.pu.pagopapayments.enums.PagoPaNodeFaults;
import it.gov.pagopa.pu.pagopapayments.exception.common.BaseBusinessException;
import lombok.Getter;

@Getter
public class PagoPaNodeFaultException extends BaseBusinessException {
  private final PagoPaNodeFaults errorCode;
  private final String errorEmitter;

  public PagoPaNodeFaultException(PagoPaNodeFaults errorCode, String errorEmitter) {
    super(errorCode.code(), errorCode.description());
    this.errorCode = errorCode;
    this.errorEmitter = errorEmitter;
  }
}
