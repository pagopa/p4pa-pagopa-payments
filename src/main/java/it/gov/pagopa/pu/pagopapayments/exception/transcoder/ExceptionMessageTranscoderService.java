package it.gov.pagopa.pu.pagopapayments.exception.transcoder;

import it.gov.pagopa.pu.pagopapayments.exception.common.BaseBusinessException;
import it.gov.pagopa.pu.pagopapayments.exception.transcoder.handler.*;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

public class ExceptionMessageTranscoderService {

  private final HttpMessageNotReadableExceptionMessageTranscoder httpMessageNotReadableExceptionMessageTranscoder = new HttpMessageNotReadableExceptionMessageTranscoder();
  private final MethodArgumentNotValidExceptionMessageTranscoder methodArgumentNotValidExceptionMessageTranscoder = new MethodArgumentNotValidExceptionMessageTranscoder();
  private final MethodArgumentTypeMismatchExceptionMessageTranscoder methodArgumentTypeMismatchExceptionMessageTranscoder = new MethodArgumentTypeMismatchExceptionMessageTranscoder();
  private final ConstraintViolationExceptionMessageTranscoder constraintViolationExceptionMessageTranscoder = new ConstraintViolationExceptionMessageTranscoder();
  private final MissingServletRequestParameterExceptionMessageTranscoder missingServletRequestParameterExceptionMessageTranscoder = new MissingServletRequestParameterExceptionMessageTranscoder();
  private final HttpClientTooManyRequestExceptionMessageTranscoder httpClientTooManyRequestExceptionMessageTranscoder = new HttpClientTooManyRequestExceptionMessageTranscoder();
  private final BaseBusinessExceptionMessageTranscoder baseBusinessExceptionMessageTranscoder = new BaseBusinessExceptionMessageTranscoder();
  private final DefaultExceptionMessageTranscoder defaultExceptionMessageTranscoder = new DefaultExceptionMessageTranscoder();

  public ExceptionMessageTranscoded transcode(Exception ex) {
    switch (ex) {
      case HttpMessageNotReadableException httpMessageNotReadableException -> {
        return httpMessageNotReadableExceptionMessageTranscoder.transcode(httpMessageNotReadableException);
      }
      case MethodArgumentNotValidException methodArgumentNotValidException -> {
        return methodArgumentNotValidExceptionMessageTranscoder.transcode(methodArgumentNotValidException);
      }
      case MethodArgumentTypeMismatchException methodArgumentTypeMismatchException -> {
        return methodArgumentTypeMismatchExceptionMessageTranscoder.transcode(methodArgumentTypeMismatchException);
      }
      case ConstraintViolationException constraintViolationException -> {
        return constraintViolationExceptionMessageTranscoder.transcode(constraintViolationException);
      }
      case MissingServletRequestParameterException missingServletRequestParameterException -> {
        return missingServletRequestParameterExceptionMessageTranscoder.transcode(missingServletRequestParameterException);
      }
      case HttpClientErrorException.TooManyRequests tooManyRequestsException -> {
        return httpClientTooManyRequestExceptionMessageTranscoder.transcode(tooManyRequestsException);
      }
      case BaseBusinessException businessException -> {
        return baseBusinessExceptionMessageTranscoder.transcode(businessException);
      }
      default -> {
        return defaultExceptionMessageTranscoder.transcode(ex);
      }
    }
  }
}
