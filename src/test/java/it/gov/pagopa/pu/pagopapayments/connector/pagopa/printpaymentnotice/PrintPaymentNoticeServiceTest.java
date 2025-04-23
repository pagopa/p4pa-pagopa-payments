package it.gov.pagopa.pu.pagopapayments.connector.pagopa.printpaymentnotice;

import it.gov.pagopa.pu.pagopapayments.connector.pagopa.printpaymentnotice.client.PrintPaymentNoticeClient;
import it.gov.pagopa.pu.printpaymentnotice.connector.printpaymentnotice.generated.dto.NoticeGenerationMassiveRequestDTO;
import it.gov.pagopa.pu.printpaymentnotice.connector.printpaymentnotice.generated.dto.NoticeGenerationMassiveResourceDTO;
import it.gov.pagopa.pu.printpaymentnotice.connector.printpaymentnotice.generated.dto.NoticeGenerationRequestItemDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PrintPaymentNoticeServiceTest {

  @Mock
  private PrintPaymentNoticeClient clientMock;

  private PrintPaymentNoticeService service;
  private static final String VALID_ACCESS_TOKEN = "VALID_ACCESS_TOKEN";

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
    byte[] expectedResult = "PDF-DATA".getBytes();

    Mockito.when(clientMock.generateNotice(brokerId, noticeGenerationRequestItemDTO, VALID_ACCESS_TOKEN))
      .thenReturn(expectedResult);

    // When
    byte[] result = service.generateNotice(brokerId, noticeGenerationRequestItemDTO, VALID_ACCESS_TOKEN);

    // Then
    Assertions.assertSame(expectedResult, result);
  }

  @Test
  void whenGenerateNoticeMassiveThenInvokeClient() {
    // Given
    Long brokerId = 1L;
    NoticeGenerationMassiveRequestDTO noticeMassive = new NoticeGenerationMassiveRequestDTO();
    NoticeGenerationMassiveResourceDTO expectedResult = new NoticeGenerationMassiveResourceDTO();
    expectedResult.setFolderId("123");
    String idempotenceKey = "f8c3de3d-1fea-4d7c-a8b0-29f63c4c3454";

    Mockito.when(clientMock.generateNoticeMassive(brokerId, idempotenceKey, noticeMassive, VALID_ACCESS_TOKEN))
      .thenReturn(expectedResult);

    // When
    NoticeGenerationMassiveResourceDTO result = service.generateNoticeMassive(brokerId, idempotenceKey, noticeMassive, VALID_ACCESS_TOKEN);

    // Then
    Assertions.assertSame(expectedResult, result);
  }
}
