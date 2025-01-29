package it.gov.pagopa.pu.pagopapayments.util;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

public class ConversionUtils {
  private ConversionUtils() {
  }

  private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
  private static final DatatypeFactory DATATYPE_FACTORY_XML_GREGORIAN_CALENDAR;
  private static final ZoneId ZONE_ID_ROME = ZoneId.of("Europe/Rome");

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
    return offsetDateTime != null ? DATATYPE_FACTORY_XML_GREGORIAN_CALENDAR.newXMLGregorianCalendar(offsetDateTime.toString()) : null;
  }


  public static OffsetDateTime toOffsetDateTime(XMLGregorianCalendar xmlGregorianCalendar) {
    if(xmlGregorianCalendar == null) {
      return null;
    }
    OffsetDateTime odt = OffsetDateTime.parse(xmlGregorianCalendar.toString());
    ZoneOffset zoneOffset = ZONE_ID_ROME.getRules().getOffset(odt.toInstant());
    return odt.withOffsetSameInstant(zoneOffset);
  }

}
