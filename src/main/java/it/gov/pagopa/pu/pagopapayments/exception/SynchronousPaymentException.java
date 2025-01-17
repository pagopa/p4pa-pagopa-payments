package it.gov.pagopa.pu.pagopapayments.exception;

import it.gov.pagopa.pu.pagopapayments.enums.PagoPaNodeFaults;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@RequiredArgsConstructor
@Data
public class SynchronousPaymentException extends ApplicationException {
  private final PagoPaNodeFaults errorCode;
  private final String errorEmitter;
}
