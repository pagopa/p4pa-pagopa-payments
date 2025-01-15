package it.gov.pagopa.pu.pagopapayments.util;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;

public class Constants {
  public static final OffsetDateTime MAX_EXPIRATION_DATE = LocalDateTime.of(2099, 12, 31, 23, 59, 59).atZone(ZoneId.of("Europe/Rome")).toOffsetDateTime();

}
