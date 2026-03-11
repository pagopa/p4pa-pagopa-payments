package it.gov.pagopa.pu.pagopapayments.exception;

import it.gov.pagopa.pu.orgfornode.dto.generated.ErrorResponseForNode;
import it.gov.pagopa.pu.pagopapayments.controller.OrgForNodeRestController;
import it.gov.pagopa.pu.pagopapayments.enums.OrgForNodeError;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.event.Level;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.OffsetDateTime;

@RestControllerAdvice(assignableTypes = OrgForNodeRestController.class)
@Slf4j
public class OrgForNodeExceptionHandler {

  @ExceptionHandler(OrgForNodeException.class)
  public ResponseEntity<ErrorResponseForNode> handleOrgForNodeException(OrgForNodeException ex, HttpServletRequest request) {
    OrgForNodeError error = ex.getError();
    logException(ex, request, error.getHttpStatus());

    return ResponseEntity
      .status(error.getHttpStatus())
      .contentType(MediaType.APPLICATION_JSON)
      .body(buildErrorResponse(error));
  }

  @ExceptionHandler({ValidationException.class, HttpMessageNotReadableException.class, MethodArgumentNotValidException.class, MethodArgumentTypeMismatchException.class, ConstraintViolationException.class})
  public ResponseEntity<ErrorResponseForNode> handleBadRequest(Exception ex, HttpServletRequest request) {
    logException(ex, request, HttpStatus.BAD_REQUEST);

    return ResponseEntity
      .status(HttpStatus.BAD_REQUEST)
      .contentType(MediaType.APPLICATION_JSON)
      .body(buildErrorResponse(OrgForNodeError.ODP_101));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponseForNode> handleGenericException(Exception ex, HttpServletRequest request) {
    logException(ex, request, HttpStatus.INTERNAL_SERVER_ERROR);

    return ResponseEntity
      .status(HttpStatus.INTERNAL_SERVER_ERROR)
      .contentType(MediaType.APPLICATION_JSON)
      .body(buildErrorResponse(OrgForNodeError.ODP_103));
  }

  private ErrorResponseForNode buildErrorResponse(OrgForNodeError error) {
    OffsetDateTime now = OffsetDateTime.now();

    ErrorResponseForNode response = new ErrorResponseForNode();
    response.setHttpStatusCode(error.getHttpStatus().value());
    response.setHttpStatusDescription(error.getHttpStatus().getReasonPhrase());
    response.setAppErrorCode(error.getAppErrorCode());
    response.setErrorMessage(error.getErrorMessage());
    response.setTimestamp(now.toEpochSecond());
    response.setDateTime(now.toLocalDateTime().toString());
    return response;
  }

  private static String sanitizeForLog(String value) {
    if (value == null) {
      return null;
    }
    // Remove carriage return and newline characters to prevent log injection
    return value
      .replace('\r', ' ')
      .replace('\n', ' ');
  }

  private static void logException(Exception ex, HttpServletRequest request, HttpStatus httpStatus) {
    boolean printStackTrace = httpStatus.is5xxServerError();
    Level logLevel = printStackTrace ? Level.ERROR : Level.INFO;

    String requestDescription = "%s %s".formatted(request.getMethod(), request.getRequestURI());
    String sanitizedRequestDescription = sanitizeForLog(requestDescription);

    log.makeLoggingEventBuilder(logLevel)
      .log("A {} occurred handling request {}: HttpStatus {} - {}",
        ex.getClass(),
        sanitizedRequestDescription,
        httpStatus.value(),
        ex.getMessage(),
        printStackTrace ? ex : null
      );

    if (!printStackTrace && log.isDebugEnabled() && ex.getCause() != null) {
      log.debug("CausedBy: ", ex.getCause());
    }
  }
}
