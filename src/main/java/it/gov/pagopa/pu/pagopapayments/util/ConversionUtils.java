package it.gov.pagopa.pu.pagopapayments.util;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Optional;

public class ConversionUtils {
  private ConversionUtils() {
  }

  private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
  private static final DatatypeFactory DATATYPE_FACTORY_XML_GREGORIAN_CALENDAR;
  public static final ZoneId ZONE_ID_ROME = ZoneId.of("Europe/Rome");
  public static final OffsetDateTime MAX_EXPIRATION_DATE = LocalDateTime.of(2099, 12, 31, 23, 59, 59).atZone(ConversionUtils.ZONE_ID_ROME).toOffsetDateTime();

  static {
    try {
      DATATYPE_FACTORY_XML_GREGORIAN_CALENDAR = DatatypeFactory.newInstance();
    } catch (DatatypeConfigurationException e) {
      throw new UnsupportedOperationException(e);
    }
  }

  public static BigDecimal centsAmountToBigDecimalEuroAmount(Long centsAmount) {
    return centsAmount != null ? BigDecimal.valueOf(centsAmount).divide(HUNDRED, 2, RoundingMode.UNNECESSARY) : null;
  }

  public static XMLGregorianCalendar toXMLGregorianCalendar(OffsetDateTime offsetDateTime) {
    return offsetDateTime != null ? DATATYPE_FACTORY_XML_GREGORIAN_CALENDAR.newXMLGregorianCalendar(GregorianCalendar.from(offsetDateTime.toZonedDateTime())) : null;
  }


  public static OffsetDateTime toOffsetDateTime(XMLGregorianCalendar xmlGregorianCalendar) {
    if(xmlGregorianCalendar == null) {
      return null;
    }
    OffsetDateTime odt = OffsetDateTime.parse(xmlGregorianCalendar.toString());
    ZoneOffset zoneOffset = ZONE_ID_ROME.getRules().getOffset(odt.toInstant());
    return odt.withOffsetSameInstant(zoneOffset);
  }

  public static OffsetDateTime toOffsetDateTime(Date date) {
    if (date == null) {
      return null;
    }
    return OffsetDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
  }

  public static OffsetDateTime localDate2RomeMaxTime(LocalDate dueDate){
    return Optional.ofNullable(dueDate)
      .map(dt -> dt.atTime(LocalTime.MAX).atZone(ConversionUtils.ZONE_ID_ROME).toOffsetDateTime())
      .orElse(MAX_EXPIRATION_DATE);
  }

  public static LocalDateTime atEndOfDay(LocalDate localDate) {
    if(localDate == null){
      return null;
    }
    return LocalDateTime.of(localDate, LocalTime.MAX);
  }
}
