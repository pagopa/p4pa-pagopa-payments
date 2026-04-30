package it.gov.pagopa.pu.pagopapayments.connector.send_notification;

import it.gov.pagopa.pu.sendnotification.dto.generated.NotificationPriceResponseV23DTO;

public interface SendNotificationService {
  NotificationPriceResponseV23DTO retrieveNotificationPrice(Long organizationId, String nav, String accessToken);
}
