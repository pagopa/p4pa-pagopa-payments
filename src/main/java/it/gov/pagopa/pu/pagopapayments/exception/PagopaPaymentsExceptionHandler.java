package it.gov.pagopa.pu.pagopapayments.exception;

import it.gov.pagopa.pu.pagopapayments.dto.generated.PagoPaPaymentsErrorDTO;
import it.gov.pagopa.pu.pagopapayments.dto.generated.PagoPaPaymentsErrorDTO.CategoryEnum;
import it.gov.pagopa.pu.pagopapayments.exception.common.CommonExceptionHandler;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class PagopaPaymentsExceptionHandler extends CommonExceptionHandler {


  @ExceptionHandler({MissingApiKeyException.class})
  public ResponseEntity<PagoPaPaymentsErrorDTO> handleMissingApiKeyException(MissingApiKeyException ex, HttpServletRequest request) {
    return handleException(ex, request, HttpStatus.INTERNAL_SERVER_ERROR, CategoryEnum.PAGOPA_PAYMENTS_GENERIC_ERROR);
  }

  @ExceptionHandler({NotPayableSilActualizedAmountException.class})
  public ResponseEntity<PagoPaPaymentsErrorDTO> handleNotPayableSilActualizedAmountException(RuntimeException ex, HttpServletRequest request) {
    return handleException(ex, request, HttpStatus.CONFLICT, CategoryEnum.PAGOPA_PAYMENTS_NOT_PAYABLE);
  }

}
