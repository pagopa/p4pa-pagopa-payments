package it.gov.pagopa.pu.pagopapayments.connector.send_notification;

import it.gov.pagopa.pu.pagopapayments.config.CacheConfig;
import it.gov.pagopa.pu.pagopapayments.connector.send_notification.client.SendClient;
import it.gov.pagopa.pu.sendnotification.dto.generated.NotificationPriceResponseV23DTO;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class SendNotificationServiceImpl implements SendNotificationService{
  private final SendClient sendClient;

  public SendNotificationServiceImpl(SendClient sendClient) {
    this.sendClient = sendClient;
  }

  @Override
  @Cacheable(cacheNames = CacheConfig.Fields.notificationFee, key = "#organizationId + '-' + #nav", unless="#result == null")
  public NotificationPriceResponseV23DTO retrieveNotificationPrice(Long organizationId, String nav, String accessToken) {
    return sendClient.retrieveNotificationPrice(organizationId, nav, accessToken);
  }
}
