package it.gov.pagopa.pu.pagopapayments.connector.pagopa.printpaymentnotice;

import it.gov.pagopa.pu.pagopapayments.connector.pagopa.printpaymentnotice.client.PrintPaymentNoticeClient;
import it.gov.pagopa.pu.printpaymentnotice.connector.printpaymentnotice.generated.dto.NoticeGenerationRequestItemDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;

@ExtendWith(MockitoExtension.class)
class PrintPaymentNoticeServiceTest {

  @Mock
  private PrintPaymentNoticeClient clientMock;

  private PrintPaymentNoticeService service;

  @BeforeEach
  void init() {
    service = new PrintPaymentNoticeServiceImpl(clientMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(clientMock);
  }

  @Test
  void whenGenerateNoticeThenInvokeClient() {
    // Given
    Long brokerId = 1L;
    NoticeGenerationRequestItemDTO noticeGenerationRequestItemDTO = new NoticeGenerationRequestItemDTO();
    File expectedResult = new File("path");

    Mockito.when(clientMock.generateNotice(brokerId, noticeGenerationRequestItemDTO))
      .thenReturn(expectedResult);

    // When
    File result = service.generateNotice(brokerId, noticeGenerationRequestItemDTO);

    // Then
    Assertions.assertSame(expectedResult, result);
  }
}
