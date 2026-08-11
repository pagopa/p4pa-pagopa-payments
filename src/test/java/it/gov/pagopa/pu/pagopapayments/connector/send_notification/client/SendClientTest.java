package it.gov.pagopa.pu.pagopapayments.connector.send_notification.client;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import it.gov.pagopa.pu.pagopapayments.connector.send_notification.config.SendNotificationApisHolder;
import it.gov.pagopa.pu.sendnotification.client.generated.SendApi;
import it.gov.pagopa.pu.sendnotification.dto.generated.NotificationPriceResponseV23DTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SendClientTest {

  @Mock
  private SendNotificationApisHolder sendNotificationApisHolderMock;
  @Mock
  private SendApi sendApiMock;

  private SendClient sendClient;

  @BeforeEach
  void setUp() {
    sendClient = new SendClient(sendNotificationApisHolderMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(sendNotificationApisHolderMock);
  }

  @Test
  void whenRetrieveNotificationPriceThenInvokeWithAccessToken() {
    String accessToken = "ACCESSTOKEN";
    Long organizationId = 1L;
    String nav = "NAV";
    NotificationPriceResponseV23DTO expectedResult = new NotificationPriceResponseV23DTO();

    when(sendNotificationApisHolderMock.getSendApi(accessToken))
      .thenReturn(sendApiMock);
    when(sendApiMock.retrieveNotificationPrice(organizationId, nav))
      .thenReturn(expectedResult);

    NotificationPriceResponseV23DTO result = sendClient.retrieveNotificationPrice(
      organizationId, nav, accessToken);

    assertSame(expectedResult, result);
  }
}
