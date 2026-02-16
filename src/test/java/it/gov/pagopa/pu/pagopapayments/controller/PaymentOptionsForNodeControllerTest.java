package it.gov.pagopa.pu.pagopapayments.controller;

import it.gov.pagopa.pu.orgfornode.dto.generated.PaymentOptionsResponseForNode;
import it.gov.pagopa.pu.pagopapayments.service.orgfornode.PaymentOptionsForNodeService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentOptionsForNodeControllerTest {

  @Mock
  private PaymentOptionsForNodeService serviceMock;

  @InjectMocks
  private PaymentOptionsForNodeController controller;

  @AfterEach
  void verifyNoMore() {
    verifyNoMoreInteractions(serviceMock);
  }

  @Test
  void givenServiceReturnsResponseWhenGetPaymentOptionsByNoticeNumberThenOk() {
    // given
    String noticeNumber = "NAV123";
    String organizationFiscalCode = "ORG_FISCAL_CODE";

    PaymentOptionsResponseForNode expected = new PaymentOptionsResponseForNode();
    when(serviceMock.getPaymentOptions(noticeNumber, organizationFiscalCode)).thenReturn(expected);

    // when
    ResponseEntity<PaymentOptionsResponseForNode> response =
      controller.getPaymentOptionsByNoticeNumber(noticeNumber, organizationFiscalCode);

    // then
    Assertions.assertNotNull(response);
    Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    Assertions.assertSame(expected, response.getBody());
  }

  @Test
  void givenServiceReturnsNullWhenGetPaymentOptionsByNoticeNumberThenNotFound() {
    // given
    String noticeNumber = "NAV123";
    String organizationFiscalCode = "ORG_FISCAL_CODE";

    when(serviceMock.getPaymentOptions(noticeNumber, organizationFiscalCode)).thenReturn(null);

    // when
    ResponseEntity<PaymentOptionsResponseForNode> response =
      controller.getPaymentOptionsByNoticeNumber(noticeNumber, organizationFiscalCode);

    // then
    Assertions.assertNotNull(response);
    Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    Assertions.assertNull(response.getBody());
  }
}
