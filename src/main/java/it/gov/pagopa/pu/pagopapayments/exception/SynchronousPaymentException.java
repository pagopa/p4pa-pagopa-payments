package it.gov.pagopa.pu.pagopapayments.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@RequiredArgsConstructor
@Data
public class SynchronousPaymentException extends ApplicationException {
  private final String errorCode;
  private final String errorEmitter;
}
