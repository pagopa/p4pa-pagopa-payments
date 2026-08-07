package it.gov.pagopa.pu.pagopapayments.exception;

import it.gov.pagopa.pu.pagopapayments.exception.common.CommonExceptionHandlerTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.mockito.Mockito.doThrow;

class PagoPaPaymentsExceptionHandlerTest extends CommonExceptionHandlerTest {

  @Test
  void handleNotPayableSilActualizedAmountException() throws Exception {
    doThrow(new NotPayableSilActualizedAmountException("Error")).when(testControllerSpy).testEndpoint(DATA, BODY);

    performRequest(DATA, MediaType.APPLICATION_JSON)
      .andExpect(MockMvcResultMatchers.status().isConflict())
      .andExpect(MockMvcResultMatchers.jsonPath("$.category").value("PAGOPA_PAYMENTS_NOT_PAYABLE"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("ACTUALIZATION_ERROR"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Error"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.fields").doesNotExist())
      .andExpect(MockMvcResultMatchers.jsonPath("$.traceId").value(traceId));
  }

}
