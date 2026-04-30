package it.gov.pagopa.pu.pagopapayments.connector.send_notification.client;

import it.gov.pagopa.pu.pagopapayments.connector.send_notification.config.SendNotificationApisHolder;
import it.gov.pagopa.pu.sendnotification.dto.generated.NotificationPriceResponseV23DTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SendClient {
  private final SendNotificationApisHolder sendNotificationApisHolder;

  public SendClient(SendNotificationApisHolder sendNotificationApisHolder) {
    this.sendNotificationApisHolder = sendNotificationApisHolder;
  }

  public NotificationPriceResponseV23DTO retrieveNotificationPrice(Long organizationId, String nav, String accessToken){
    return sendNotificationApisHolder.getSendApi(accessToken).retrieveNotificationPrice(organizationId, nav);
  }
}
