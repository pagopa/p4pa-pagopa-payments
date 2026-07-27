package it.gov.pagopa.pu.pagopapayments.exception;

import it.gov.pagopa.pu.pagopapayments.dto.generated.PagoPaPaymentsErrorDTO;
import it.gov.pagopa.pu.pagopapayments.dto.generated.PagoPaPaymentsErrorDTO.CategoryEnum;
import it.gov.pagopa.pu.pagopapayments.util.Utilities;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.hc.client5.http.HttpHostConnectException;
import org.slf4j.event.Level;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponse;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DatabindException;

import java.util.Objects;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class PagopaPaymentsExceptionHandler {

  private static final String ERROR_MESSAGE_FORMAT = "[%s] %s";

  @ExceptionHandler(NotFoundException.class)
  @ResponseStatus(value = HttpStatus.NOT_FOUND)
  public ResponseEntity<PagoPaPaymentsErrorDTO> handleResourceNotFoundException(NotFoundException ex, HttpServletRequest request) {
    return handleException(ex, request, HttpStatus.NOT_FOUND, CategoryEnum.PAGOPA_PAYMENTS_NOT_FOUND);
  }

  @ExceptionHandler(ConflictException.class)
  public ResponseEntity<PagoPaPaymentsErrorDTO> handleConflictException(ConflictException ex, HttpServletRequest request) {
    return handleException(ex, request, HttpStatus.CONFLICT, CategoryEnum.PAGOPA_PAYMENTS_CONFLICT);
  }

  @ExceptionHandler(ForbiddenException.class)
  public ResponseEntity<PagoPaPaymentsErrorDTO> handleForbiddenException(ForbiddenException ex, HttpServletRequest request) {
    return handleException(ex, request, HttpStatus.FORBIDDEN, CategoryEnum.PAGOPA_PAYMENTS_FORBIDDEN);
  }

  @ExceptionHandler({ValidationException.class, HttpMessageNotReadableException.class, MethodArgumentNotValidException.class, MethodArgumentTypeMismatchException.class, ConversionFailedException.class, InvalidValueException.class})
  public ResponseEntity<PagoPaPaymentsErrorDTO> handleViolationException(Exception ex, HttpServletRequest request) {
    return handleException(ex, request, HttpStatus.BAD_REQUEST, CategoryEnum.PAGOPA_PAYMENTS_BAD_REQUEST);
  }

  @ExceptionHandler(NotAuthorizedException.class)
  public ResponseEntity<PagoPaPaymentsErrorDTO> handleNotAuthorizedException(Exception ex, HttpServletRequest request) {
    return handleException(ex, request, HttpStatus.UNAUTHORIZED, CategoryEnum.PAGOPA_PAYMENTS_UNAUTHORIZED);
  }

  @ExceptionHandler(HttpClientErrorException.TooManyRequests.class)
  public ResponseEntity<PagoPaPaymentsErrorDTO> handleInvokedHttpClientTooManyRequestsError(Exception ex, HttpServletRequest request) {
    return handleException(ex, request, HttpStatus.TOO_MANY_REQUESTS, CategoryEnum.PAGOPA_PAYMENTS_TOO_MANY_REQUESTS);
  }

  @ExceptionHandler({ServletException.class, ErrorResponseException.class})
  public ResponseEntity<PagoPaPaymentsErrorDTO> handleServletException(Exception ex, HttpServletRequest request) {
    HttpStatusCode httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
    CategoryEnum errorCode = CategoryEnum.PAGOPA_PAYMENTS_GENERIC_ERROR;
    if (ex instanceof ErrorResponse errorResponse) {
      httpStatus = errorResponse.getStatusCode();
      if (httpStatus.isSameCodeAs(HttpStatus.NOT_FOUND)) {
        errorCode = CategoryEnum.PAGOPA_PAYMENTS_NOT_FOUND;
      } else if (httpStatus.is4xxClientError()) {
        errorCode = CategoryEnum.PAGOPA_PAYMENTS_BAD_REQUEST;
      }
    }
    return handleException(ex, request, httpStatus, errorCode);
  }

  @ExceptionHandler({RuntimeException.class, MissingApiKeyException.class})
  public ResponseEntity<PagoPaPaymentsErrorDTO> handleRuntimeException(RuntimeException ex, HttpServletRequest request) {
    return handleException(ex, request, HttpStatus.INTERNAL_SERVER_ERROR, CategoryEnum.PAGOPA_PAYMENTS_GENERIC_ERROR);
  }

  @ExceptionHandler({NotPayableSilActualizedAmountException.class})
  public ResponseEntity<PagoPaPaymentsErrorDTO> handleNotPayableSilActualizedAmountException(RuntimeException ex, HttpServletRequest request) {
    return handleException(ex, request, HttpStatus.CONFLICT, CategoryEnum.PAGOPA_PAYMENTS_NOT_PAYABLE);
  }

  static ResponseEntity<PagoPaPaymentsErrorDTO> handleException(Exception ex, HttpServletRequest request, HttpStatusCode httpStatus, CategoryEnum errorEnum) {
    logException(ex, request, httpStatus);

    Pair<String, String> code2message = buildReturnedMessage(ex);

    String code = Objects.requireNonNullElse(code2message.getLeft(), errorEnum.getValue());
    String message = code2message.getRight();

    return ResponseEntity
      .status(httpStatus)
      .contentType(MediaType.APPLICATION_JSON)
      .body(new PagoPaPaymentsErrorDTO(errorEnum, code, String.format(ERROR_MESSAGE_FORMAT, code, message), Utilities.getTraceId()));
  }

  private static void logException(Exception ex, HttpServletRequest request, HttpStatusCode httpStatus) {
    boolean printStackTrace = httpStatus.is5xxServerError();
    Level logLevel = printStackTrace ? Level.ERROR : Level.INFO;
    log.makeLoggingEventBuilder(logLevel)
      .log("A {} occurred handling request {}: HttpStatus {} - {}",
        ex.getClass(),
        getRequestDetails(request),
        httpStatus.value(),
        ex.getMessage(),
        printStackTrace ? ex : null
      );
    if (!printStackTrace && log.isDebugEnabled() && ex.getCause() != null) {
      log.debug("CausedBy: ", ex.getCause());
    }
  }

  private static Pair<String, String> buildReturnedMessage(Exception ex) {
    switch (ex) {
      case HttpMessageNotReadableException httpMessageNotReadableException -> {
        String errorMsg = "Required request body is missing";
        if (httpMessageNotReadableException.getCause() instanceof DatabindException jsonMappingException) {
          errorMsg = "Cannot parse body. " +
            jsonMappingException.getPath().stream()
              .map(JacksonException.Reference::getPropertyName)
              .collect(Collectors.joining(".")) +
            ": " + jsonMappingException.getOriginalMessage();
        } else if (httpMessageNotReadableException.getCause() instanceof JacksonException jacksonException) {
          errorMsg = "Cannot parse body. " + jacksonException.getOriginalMessage();
        }
        return Pair.of(CategoryEnum.PAGOPA_PAYMENTS_BAD_REQUEST.name(), errorMsg);
      }
      case MethodArgumentNotValidException methodArgumentNotValidException -> {
        return Pair.of(CategoryEnum.PAGOPA_PAYMENTS_BAD_REQUEST.name(),
          "Invalid request content." +
          methodArgumentNotValidException.getBindingResult()
            .getAllErrors().stream()
            .map(e -> " " +
              (e instanceof FieldError fieldError ? fieldError.getField() : e.getObjectName()) +
              ": " + e.getDefaultMessage())
            .sorted()
            .collect(Collectors.joining(";")));
      }
      case ConstraintViolationException constraintViolationException -> {
        return Pair.of(CategoryEnum.PAGOPA_PAYMENTS_BAD_REQUEST.name(),
          "Invalid request content." +
          constraintViolationException.getConstraintViolations()
            .stream()
            .map(e -> " " + e.getPropertyPath() + ": " + e.getMessage())
            .sorted()
            .collect(Collectors.joining(";")));
      }
      case HttpClientErrorException.TooManyRequests tooManyRequestsException -> {
        return Pair.of(CategoryEnum.PAGOPA_PAYMENTS_TOO_MANY_REQUESTS.name(), tooManyRequestsException.getMessage());
      }
      case BaseBusinessException businessException -> {
        return Pair.of(businessException.getCode(), businessException.getMessage());
      }
      default -> {
        if (ex.getCause() instanceof HttpHostConnectException) {
          return Pair.of("CONNECTION_ERROR", ex.getMessage());
        }
        return Pair.of(null, ex.getMessage());
      }
    }
  }

  static String getRequestDetails(HttpServletRequest request) {
    return "%s %s".formatted(request.getMethod(), request.getRequestURI());
  }

}
