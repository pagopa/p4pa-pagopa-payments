package it.gov.pagopa.pu.pagopapayments.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.api.ErrorMessage;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class ControllerExceptionHandler {

  @ExceptionHandler(NotFoundException.class)
  @ResponseStatus(value = HttpStatus.NOT_FOUND)
  public ErrorMessage resourceNotFoundException(NotFoundException ex, HttpServletRequest request) {
    if (log.isInfoEnabled()) {
      String logMessage = "A ResourceNotFoundException occurred handling request %s - HttpStatus %s - %s"
        .formatted(getRequestDetails(request), HttpStatus.NOT_FOUND.value(), ex.getMessage());
      if (log.isDebugEnabled())
        log.debug(logMessage, ex);
      else
        log.info(logMessage);
    }
    return new ErrorMessage("resource not found: %s".formatted(ex.getMessage()));
  }

  private String getRequestDetails(HttpServletRequest request) {
    return "%s %s".formatted(request.getMethod(), request.getRequestURI());
  }

}
