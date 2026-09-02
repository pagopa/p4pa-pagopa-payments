package it.gov.pagopa.pu.pagopapayments.exception.transcoder;

import it.gov.pagopa.pu.pagopapayments.exception.common.BaseBusinessException;
import it.gov.pagopa.pu.pagopapayments.exception.transcoder.handler.*;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.client.HttpClientErrorException;

import java.lang.reflect.Field;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ExceptionMessageTranscoderServiceTest {

  @Mock private HttpMessageNotReadableExceptionMessageTranscoder httpMessageNotReadableExceptionMessageTranscoderMock;
  @Mock private MethodArgumentNotValidExceptionMessageTranscoder methodArgumentNotValidExceptionMessageTranscoderMock;
  @Mock private ConstraintViolationExceptionMessageTranscoder constraintViolationExceptionMessageTranscoderMock;
  @Mock private MissingServletRequestParameterExceptionMessageTranscoder missingServletRequestParameterExceptionMessageTranscoderMock;
  @Mock private HttpClientTooManyRequestExceptionMessageTranscoder httpClientTooManyRequestExceptionMessageTranscoderMock;
  @Mock private BaseBusinessExceptionMessageTranscoder baseBusinessExceptionMessageTranscoderMock;
  @Mock private DefaultExceptionMessageTranscoder defaultExceptionMessageTranscoderMock;

  private ExceptionMessageTranscoderService service;

  @BeforeEach
  void init() throws IllegalAccessException, NoSuchFieldException {
    service = new ExceptionMessageTranscoderService();

    mockField("httpMessageNotReadableExceptionMessageTranscoder", httpMessageNotReadableExceptionMessageTranscoderMock);
    mockField("methodArgumentNotValidExceptionMessageTranscoder", methodArgumentNotValidExceptionMessageTranscoderMock);
    mockField("constraintViolationExceptionMessageTranscoder", constraintViolationExceptionMessageTranscoderMock);
    mockField("missingServletRequestParameterExceptionMessageTranscoder", missingServletRequestParameterExceptionMessageTranscoderMock);
    mockField("httpClientTooManyRequestExceptionMessageTranscoder", httpClientTooManyRequestExceptionMessageTranscoderMock);
    mockField("baseBusinessExceptionMessageTranscoder", baseBusinessExceptionMessageTranscoderMock);
    mockField("defaultExceptionMessageTranscoder", defaultExceptionMessageTranscoderMock);
  }

  private void mockField(String fieldName, Object mock) throws IllegalAccessException, NoSuchFieldException {
    Field declaredField = ExceptionMessageTranscoderService.class.getDeclaredField(fieldName);
    declaredField.setAccessible(true);
    declaredField.set(service, mock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      httpMessageNotReadableExceptionMessageTranscoderMock,
        methodArgumentNotValidExceptionMessageTranscoderMock,
        constraintViolationExceptionMessageTranscoderMock,
        missingServletRequestParameterExceptionMessageTranscoderMock,
        httpClientTooManyRequestExceptionMessageTranscoderMock,
        baseBusinessExceptionMessageTranscoderMock,
        defaultExceptionMessageTranscoderMock
    );
  }

  @Test
  void givenHttpMessageNotReadableExceptionWhenTranscodeThenCallMock(){
    HttpMessageNotReadableException exception = mock(HttpMessageNotReadableException.class);
    service.transcode(exception);
    verify(httpMessageNotReadableExceptionMessageTranscoderMock).transcode(exception);
  }

  @Test
    void givenMethodArgumentNotValidExceptionWhenTranscodeThenCallMock(){
    MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
    service.transcode(exception);
    verify(methodArgumentNotValidExceptionMessageTranscoderMock).transcode(exception);
  }

  @Test
  void givenConstraintViolationExceptionWhenTranscodeThenCallMock(){
    ConstraintViolationException exception = mock(ConstraintViolationException.class);
    service.transcode(exception);
    verify(constraintViolationExceptionMessageTranscoderMock).transcode(exception);
  }

  @Test
  void givenMissingServletRequestParameterExceptionWhenTranscodeThenCallMock(){
    MissingServletRequestParameterException exception = mock(MissingServletRequestParameterException.class);
    service.transcode(exception);
    verify(missingServletRequestParameterExceptionMessageTranscoderMock).transcode(exception);
  }

  @Test
    void givenHttpClientTooManyRequestExceptionWhenTranscodeThenCallMock(){
    HttpClientErrorException.TooManyRequests exception = mock(HttpClientErrorException.TooManyRequests.class);
    service.transcode(exception);
    verify(httpClientTooManyRequestExceptionMessageTranscoderMock).transcode(exception);
  }

  @Test
  void givenBaseBusinessExceptionWhenTranscodeThenCallMock(){
    BaseBusinessException exception = mock(BaseBusinessException.class);
    service.transcode(exception);
    verify(baseBusinessExceptionMessageTranscoderMock).transcode(exception);
  }

  @Test
  void givenUnhandledExceptionWhenTranscodeThenCallDefaultExceptionMessageTranscoder(){
    Exception exception = new Exception();
    service.transcode(exception);
    verify(defaultExceptionMessageTranscoderMock).transcode(exception);
  }
}
