package it.gov.pagopa.pu.pagopapayments.exception.transcoder.handler;

import it.gov.pagopa.pu.pagopapayments.exception.transcoder.ExceptionMessageTranscoded;
import it.gov.pagopa.pu.pagopapayments.exception.transcoder.ExceptionMessageTranscoder;
import org.apache.hc.client5.http.HttpHostConnectException;

public class DefaultExceptionMessageTranscoder implements ExceptionMessageTranscoder<Exception> {
  @Override
  public ExceptionMessageTranscoded transcode(Exception exception) {
    if (exception.getCause() instanceof HttpHostConnectException) {
      return new ExceptionMessageTranscoded("PAGOPA_PAYMENTS_CONNECTION_ERROR", exception.getMessage(), null);
    }
    return new ExceptionMessageTranscoded(null, exception.getMessage(), null);
  }
}
