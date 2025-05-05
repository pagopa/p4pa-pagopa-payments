package it.gov.pagopa.pu.pagopapayments.connector.send_notification;

import it.gov.pagopa.pu.pagopapayments.connector.send_notification.client.SendClient;
import it.gov.pagopa.pu.sendnotification.dto.generated.NotificationPriceResponseV23DTO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SendNotificationServiceImplTest {

  @Mock
  private SendClient sendClientMock;
  private SendNotificationService sendNotificationService;

  @BeforeEach
  void setUp() {
    sendNotificationService = new SendNotificationServiceImpl(sendClientMock);
  }

  @Test
  void whenRetrieveNotificationPriceThenInvokeClient(){
    Long organizationId = 1L;
    String nav = "NAV";
    String accessToken = "access_token";
    NotificationPriceResponseV23DTO expectedResponse = new NotificationPriceResponseV23DTO();

    Mockito.when(sendClientMock.retrieveNotificationPrice(organizationId, nav, accessToken))
      .thenReturn(expectedResponse);

    NotificationPriceResponseV23DTO response = sendNotificationService
      .retrieveNotificationPrice(organizationId, nav, accessToken);

    Assertions.assertSame(expectedResponse,response);
  }
}
