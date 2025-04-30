package it.gov.pagopa.pu.pagopapayments.connector.pagopa.printpaymentnotice.client;

import it.gov.pagopa.pu.organization.dto.generated.BrokerApiKeyType;
import it.gov.pagopa.pu.pagopapayments.connector.organization.BrokerService;
import it.gov.pagopa.pu.pagopapayments.connector.pagopa.printpaymentnotice.config.PagopaPrintPaymentNoticeApisHolder;
import it.gov.pagopa.pu.printpaymentnotice.connector.printpaymentnotice.generated.api.NoticeGenerationRequestApisApi;
import it.gov.pagopa.pu.printpaymentnotice.connector.printpaymentnotice.generated.dto.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertSame;

@ExtendWith(MockitoExtension.class)
class PrintPaymentNoticeClientTest {

  @Mock
  private PagopaPrintPaymentNoticeApisHolder apisHolder;

  @Mock
  private NoticeGenerationRequestApisApi noticeGenerationRequestApisApiMock;

  @Mock
  private BrokerService brokerServiceMock;

  private PrintPaymentNoticeClient printPaymentNoticeClient;
  private static final String VALID_ACCESS_TOKEN = "VALID_ACCESS_TOKEN";

  @BeforeEach
  void setUp() {
    printPaymentNoticeClient = new PrintPaymentNoticeClient(apisHolder, brokerServiceMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      apisHolder,
      brokerServiceMock
    );
  }

  @Test
  void givenValidRequestWhenGenerateNoticeThenVerifyResponse() {
    // Given
    NoticeGenerationRequestItemDTO requestDTO = new NoticeGenerationRequestItemDTO();
    requestDTO.setData(new NoticeRequestDataDTO());
    requestDTO.setTemplateId("TemplateSingleInstalment");

    Long brokerId = 1L;
    String apiKey = "apiKey";
    byte[] response = "PDF-DATA".getBytes();

    Mockito.when(brokerServiceMock.getBrokerApiKey(brokerId, BrokerApiKeyType.GENERATE_NOTICE, VALID_ACCESS_TOKEN))
      .thenReturn(apiKey);
    Mockito.when(apisHolder.getNoticeGenerationRequestApisApiMap(apiKey))
      .thenReturn(noticeGenerationRequestApisApiMock);
    Mockito.when(noticeGenerationRequestApisApiMock.generateNotice(requestDTO, null, null))
      .thenReturn(response);

    // When
    byte[] result = printPaymentNoticeClient.generateNotice(brokerId, requestDTO, VALID_ACCESS_TOKEN);

    // Then
    assertSame(response, result);
  }

  @Test
  void givenValidRequestWhenGenerateNoticeMassiveThenVerifyResponse() {
    // Given
    Long brokerId = 1L;
    String apiKey = "apiKey";
    NoticeGenerationMassiveRequestDTO noticeMassive = new NoticeGenerationMassiveRequestDTO();
    NoticeGenerationMassiveResourceDTO expectedResult = new NoticeGenerationMassiveResourceDTO();
    expectedResult.setFolderId("123");
    String idempotenceKey = "f8c3de3d-1fea-4d7c-a8b0-29f63c4c3454";

    Mockito.when(brokerServiceMock.getBrokerApiKey(brokerId, BrokerApiKeyType.GENERATE_NOTICE, VALID_ACCESS_TOKEN))
      .thenReturn(apiKey);
    Mockito.when(apisHolder.getNoticeGenerationRequestApisApiMap(apiKey))
      .thenReturn(noticeGenerationRequestApisApiMock);
    Mockito.when(noticeGenerationRequestApisApiMock.generateNoticeMassiveRequest(idempotenceKey, noticeMassive, null))
      .thenReturn(expectedResult);

    // When
    NoticeGenerationMassiveResourceDTO result = printPaymentNoticeClient.generateNoticeMassive(brokerId, idempotenceKey, noticeMassive, VALID_ACCESS_TOKEN);

    // Then
    assertSame(expectedResult, result);
  }

  @Test
  void givenValidRequestWhenGetFolderStatusThenVerifyResponse() {
    // Given
    Long brokerId = 1L;
    String apiKey = "apiKey";
    GetGenerationRequestStatusResourceDTO expectedResult = new GetGenerationRequestStatusResourceDTO();
    String folderId = "f8c3de3d-1fea-4d7c-a8b0-29f63c4c3454";

    Mockito.when(brokerServiceMock.getBrokerApiKey(brokerId, BrokerApiKeyType.GENERATE_NOTICE, VALID_ACCESS_TOKEN))
      .thenReturn(apiKey);
    Mockito.when(apisHolder.getNoticeGenerationRequestApisApiMap(apiKey))
      .thenReturn(noticeGenerationRequestApisApiMock);
    Mockito.when(noticeGenerationRequestApisApiMock.getFolderStatus(folderId, null))
      .thenReturn(expectedResult);

    // When
    GetGenerationRequestStatusResourceDTO result = printPaymentNoticeClient.getFolderStatus(brokerId, folderId, VALID_ACCESS_TOKEN);

    // Then
    assertSame(expectedResult, result);
  }

  @Test
  void givenValidRequestWhenGetFolderSignedUrlResourceThenVerifyResponse() {
    // Given
    Long brokerId = 1L;
    String apiKey = "apiKey";
    GetSignedUrlResourceDTO expectedResult = new GetSignedUrlResourceDTO();
    String folderId = "f8c3de3d-1fea-4d7c-a8b0-29f63c4c3454";

    Mockito.when(brokerServiceMock.getBrokerApiKey(brokerId, BrokerApiKeyType.GENERATE_NOTICE, VALID_ACCESS_TOKEN))
      .thenReturn(apiKey);
    Mockito.when(apisHolder.getNoticeGenerationRequestApisApiMap(apiKey))
      .thenReturn(noticeGenerationRequestApisApiMock);
    Mockito.when(noticeGenerationRequestApisApiMock.getFolderSignedUrlResource(folderId, null))
      .thenReturn(expectedResult);

    // When
    GetSignedUrlResourceDTO result = printPaymentNoticeClient.getFolderSignedUrlResource(brokerId, folderId, VALID_ACCESS_TOKEN);

    // Then
    assertSame(expectedResult, result);
  }
}
