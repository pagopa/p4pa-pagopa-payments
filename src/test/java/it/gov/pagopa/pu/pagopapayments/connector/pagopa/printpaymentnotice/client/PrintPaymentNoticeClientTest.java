package it.gov.pagopa.pu.pagopapayments.connector.pagopa.printpaymentnotice.client;

import it.gov.pagopa.pu.organization.dto.generated.BrokerApiKeyType;
import it.gov.pagopa.pu.pagopapayments.connector.organization.BrokerService;
import it.gov.pagopa.pu.pagopapayments.connector.pagopa.printpaymentnotice.config.PagopaPrintPaymentNoticeApisHolder;
import it.gov.pagopa.pu.printpaymentnotice.connector.printpaymentnotice.generated.api.NoticeGenerationRequestApisApi;
import it.gov.pagopa.pu.printpaymentnotice.connector.printpaymentnotice.generated.dto.NoticeGenerationRequestItemDTO;
import it.gov.pagopa.pu.printpaymentnotice.connector.printpaymentnotice.generated.dto.NoticeRequestDataDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;

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
    File response = new File("path");

    Mockito.when(brokerServiceMock.getBrokerApiKey(brokerId, BrokerApiKeyType.GENERATE_NOTICE, VALID_ACCESS_TOKEN))
      .thenReturn(apiKey);
    Mockito.when(apisHolder.getNoticeGenerationRequestApisApiMap(apiKey))
      .thenReturn(noticeGenerationRequestApisApiMock);
    Mockito.when(noticeGenerationRequestApisApiMock.generateNotice(requestDTO, null, null))
      .thenReturn(response);

    // When
    File result = printPaymentNoticeClient.generateNotice(brokerId, requestDTO, VALID_ACCESS_TOKEN);

    // Then
    assertSame(response, result);
  }
}
