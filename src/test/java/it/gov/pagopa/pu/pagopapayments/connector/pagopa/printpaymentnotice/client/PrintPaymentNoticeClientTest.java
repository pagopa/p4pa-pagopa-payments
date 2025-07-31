package it.gov.pagopa.pu.pagopapayments.connector.pagopa.printpaymentnotice.client;

import it.gov.pagopa.pu.organization.dto.generated.OrganizationApiKeyType;
import it.gov.pagopa.pu.pagopapayments.connector.organization.OrganizationService;
import it.gov.pagopa.pu.pagopapayments.connector.pagopa.printpaymentnotice.config.PagopaPrintPaymentNoticeApisHolder;
import it.gov.pagopa.pu.printpaymentnotice.connector.printpaymentnotice.generated.api.NoticeGenerationRequestApisApi;
import it.gov.pagopa.pu.printpaymentnotice.connector.printpaymentnotice.generated.dto.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
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
  private OrganizationService organizationServiceMock;

  private PrintPaymentNoticeClient printPaymentNoticeClient;
  private static final String VALID_ACCESS_TOKEN = "VALID_ACCESS_TOKEN";

  @BeforeEach
  void setUp() {
    printPaymentNoticeClient = new PrintPaymentNoticeClient(apisHolder, organizationServiceMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      apisHolder,
      organizationServiceMock
    );
  }

  @Test
  void givenValidRequestWhenGenerateNoticeThenVerifyResponse() {
    // Given
    NoticeGenerationRequestItemDTO requestDTO = new NoticeGenerationRequestItemDTO();
    requestDTO.setData(new NoticeRequestDataDTO());
    requestDTO.setTemplateId("TemplateSingleInstalment");

    Long organizationId = 1L;
    String apiKey = "apiKey";
    byte[] response = "PDF-DATA".getBytes();

    Mockito.when(organizationServiceMock.getOrganizationApiKey(organizationId, OrganizationApiKeyType.GENERATE_NOTICE, VALID_ACCESS_TOKEN))
      .thenReturn(apiKey);
    Mockito.when(apisHolder.getNoticeGenerationRequestApisApiMap(apiKey))
      .thenReturn(noticeGenerationRequestApisApiMock);
    Mockito.when(noticeGenerationRequestApisApiMock.generateNotice(requestDTO, null, null))
      .thenReturn(response);

    // When
    byte[] result = printPaymentNoticeClient.generateNotice(organizationId, requestDTO, VALID_ACCESS_TOKEN);

    // Then
    assertSame(response, result);
  }

  @Test
  void givenValidRequestWhenGenerateNoticeMassiveThenVerifyResponse() {
    // Given
    Long organizationId = 1L;
    String apiKey = "apiKey";
    NoticeGenerationMassiveRequestDTO noticeMassive = new NoticeGenerationMassiveRequestDTO();
    NoticeGenerationMassiveResourceDTO expectedResult = new NoticeGenerationMassiveResourceDTO();
    expectedResult.setFolderId("123");
    String idempotenceKey = "f8c3de3d-1fea-4d7c-a8b0-29f63c4c3454";

    Mockito.when(organizationServiceMock.getOrganizationApiKey(organizationId, OrganizationApiKeyType.GENERATE_NOTICE, VALID_ACCESS_TOKEN))
      .thenReturn(apiKey);
    Mockito.when(apisHolder.getNoticeGenerationRequestApisApiMap(apiKey))
      .thenReturn(noticeGenerationRequestApisApiMock);
    Mockito.when(noticeGenerationRequestApisApiMock.generateNoticeMassiveRequest(idempotenceKey, noticeMassive, null))
      .thenReturn(expectedResult);

    // When
    NoticeGenerationMassiveResourceDTO result = printPaymentNoticeClient.generateNoticeMassive(organizationId, idempotenceKey, noticeMassive, VALID_ACCESS_TOKEN);

    // Then
    assertSame(expectedResult, result);
  }

  @Test
  void givenValidRequestWhenGetFolderStatusThenVerifyResponse() {
    // Given
    Long organizationId = 1L;
    String apiKey = "apiKey";
    GetGenerationRequestStatusResourceDTO expectedResult = new GetGenerationRequestStatusResourceDTO();
    String folderId = "f8c3de3d-1fea-4d7c-a8b0-29f63c4c3454";

    Mockito.when(organizationServiceMock.getOrganizationApiKey(organizationId, OrganizationApiKeyType.GENERATE_NOTICE, VALID_ACCESS_TOKEN))
      .thenReturn(apiKey);
    Mockito.when(apisHolder.getNoticeGenerationRequestApisApiMap(apiKey))
      .thenReturn(noticeGenerationRequestApisApiMock);
    Mockito.when(noticeGenerationRequestApisApiMock.getFolderStatus(folderId, null))
      .thenReturn(expectedResult);

    // When
    GetGenerationRequestStatusResourceDTO result = printPaymentNoticeClient.getFolderStatus(organizationId, folderId, VALID_ACCESS_TOKEN);

    // Then
    assertSame(expectedResult, result);
  }

  @Test
  void givenValidRequestWhenGetFolderSignedUrlResourceThenVerifyResponse() {
    // Given
    Long organizationId = 1L;
    String apiKey = "apiKey";
    GetSignedUrlResourceDTO expectedResult = new GetSignedUrlResourceDTO();
    String folderId = "f8c3de3d-1fea-4d7c-a8b0-29f63c4c3454";

    Mockito.when(organizationServiceMock.getOrganizationApiKey(organizationId, OrganizationApiKeyType.GENERATE_NOTICE, VALID_ACCESS_TOKEN))
      .thenReturn(apiKey);
    Mockito.when(apisHolder.getNoticeGenerationRequestApisApiMap(apiKey))
      .thenReturn(noticeGenerationRequestApisApiMock);
    Mockito.when(noticeGenerationRequestApisApiMock.getFolderSignedUrlResource(folderId, null))
      .thenReturn(expectedResult);

    // When
    GetSignedUrlResourceDTO result = printPaymentNoticeClient.getFolderSignedUrlResource(organizationId, folderId, VALID_ACCESS_TOKEN);

    // Then
    assertSame(expectedResult, result);
  }

  @Test
  void givenNoApiKeyWhenGetFolderSignedUrlResourceThenThrowInvalidStateException() {
    // Given
    Long organizationId = 1L;
    String folderId = "f8c3de3d-1fea-4d7c-a8b0-29f63c4c3454";

    Mockito.when(organizationServiceMock.getOrganizationApiKey(organizationId, OrganizationApiKeyType.GENERATE_NOTICE, VALID_ACCESS_TOKEN))
      .thenReturn(null);

    // When, Then
    Assertions.assertThrows(IllegalStateException.class, () -> printPaymentNoticeClient.getFolderSignedUrlResource(organizationId, folderId, VALID_ACCESS_TOKEN));
  }
}
