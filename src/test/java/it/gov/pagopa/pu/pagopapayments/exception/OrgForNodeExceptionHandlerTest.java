package it.gov.pagopa.pu.pagopapayments.exception;

import it.gov.pagopa.pu.orgfornode.dto.generated.ErrorResponseForNode;
import it.gov.pagopa.pu.pagopapayments.enums.OrgForNodeError;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrgForNodeExceptionHandlerTest {

  @Mock
  private HttpServletRequest requestMock;

  private OrgForNodeExceptionHandler handler;

  @BeforeEach
  void setUp() {
    handler = new OrgForNodeExceptionHandler();
  }

  @Test
  void givenOrgForNodeExceptionWhenHandleOrgForNodeExceptionThenReturnMappedErrorResponse() {
    when(requestMock.getMethod()).thenReturn("GET");
    when(requestMock.getRequestURI()).thenReturn("/org-for-node/service/v1/payment-options/organizations/777/notices/123");

    OrgForNodeException ex = new OrgForNodeException(OrgForNodeError.ODP_107);

    ResponseEntity<ErrorResponseForNode> response = handler.handleOrgForNodeException(ex, requestMock);

    assertNotNull(response);
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    assertNotNull(response.getBody());

    ErrorResponseForNode body = response.getBody();
    assertEquals(404, body.getHttpStatusCode());
    assertEquals("Not Found", body.getHttpStatusDescription());
    assertEquals("ODP-107", body.getAppErrorCode());
    assertEquals("PAA_PAGAMENTO_SCONOSCIUTO Errore per pagamento sconosciuto", body.getErrorMessage());
    assertNotNull(body.getTimestamp());
    assertNotNull(body.getDateTime());
  }

  @Test
  void givenValidationExceptionWhenHandleBadRequestThenReturnOdp101() {
    when(requestMock.getMethod()).thenReturn("POST");
    when(requestMock.getRequestURI()).thenReturn("/org-for-node/service/v1/payment-options/organizations/777/notices/123");

    ValidationException ex = new ValidationException("validation error");

    ResponseEntity<ErrorResponseForNode> response = handler.handleBadRequest(ex, requestMock);

    assertNotNull(response);
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertNotNull(response.getBody());

    ErrorResponseForNode body = response.getBody();
    assertEquals(400, body.getHttpStatusCode());
    assertEquals("Bad Request", body.getHttpStatusDescription());
    assertEquals("ODP-101", body.getAppErrorCode());
    assertEquals("PAA_SINTASSI Errore di sintassi del modello nella richiesta", body.getErrorMessage());
    assertNotNull(body.getTimestamp());
    assertNotNull(body.getDateTime());
  }

  @Test
  void givenGenericExceptionWhenHandleGenericExceptionThenReturnOdp103() {
    when(requestMock.getMethod()).thenReturn("GET");
    when(requestMock.getRequestURI()).thenReturn("/org-for-node/service/v1/payment-options/organizations/777/notices/123");

    Exception ex = new RuntimeException("unexpected error");

    ResponseEntity<ErrorResponseForNode> response = handler.handleGenericException(ex, requestMock);

    assertNotNull(response);
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    assertNotNull(response.getBody());

    ErrorResponseForNode body = response.getBody();
    assertEquals(500, body.getHttpStatusCode());
    assertEquals("Internal Server Error", body.getHttpStatusDescription());
    assertEquals("ODP-103", body.getAppErrorCode());
    assertEquals("PAA_SYSTEM_ERROR Errore generico", body.getErrorMessage());
    assertNotNull(body.getTimestamp());
    assertNotNull(body.getDateTime());
  }
}
