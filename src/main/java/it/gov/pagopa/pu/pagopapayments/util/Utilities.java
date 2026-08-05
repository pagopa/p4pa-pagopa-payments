package it.gov.pagopa.pu.pagopapayments.util;

import org.slf4j.MDC;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.stream.Collectors;

public class Utilities {
  private Utilities() {
  }

  public static final String IUV_SEPARATOR = ",";
  public static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

  public static String getTraceId() {
    return MDC.get("traceId");
  }

  public static String getSpanId(){
    return MDC.get("spanId");
  }

  public static String iuv2Nav(String iuv, String auxDigit) {
    if (iuv == null || iuv.isBlank()) {
      return null;
    } else if (iuv.contains(IUV_SEPARATOR)) {
      return Arrays.stream(iuv.split(IUV_SEPARATOR))
        .map(iuvToMap -> iuv2Nav(iuvToMap, auxDigit))
        .collect(Collectors.joining(IUV_SEPARATOR));
    } else {
      return auxDigit + iuv;
    }
  }

  public static String nav2Iuv(String nav, String auxDigit) {
    if (nav == null || nav.isBlank()) {
      return null;
    } else if (nav.contains(IUV_SEPARATOR)) {
      return Arrays.stream(nav.split(IUV_SEPARATOR))
              .map(navToMap -> nav2Iuv(navToMap, auxDigit))
              .collect(Collectors.joining(IUV_SEPARATOR));
    } else if (nav.length() < 2 || !nav.startsWith(auxDigit)) {
      throw new IllegalArgumentException("Invalid NAV format: " + nav);
    } else {
      return nav.substring(1);
    }
  }

  public static String truncateFullName(String fullName) {
    // maxLength value is found @ resources/soap/wsdl/xsd/paForNode.xsd:174
    return safeTruncate(fullName, 70);
  }

  public static String truncateRemittanceInformation(String remittanceInformation) {
    // maxLength value is found @ resources/soap/wsdl/xsd/paForNode.xsd:416
    return safeTruncate(remittanceInformation, 140);
  }

  private static String safeTruncate(String str, int maxLength) {
    if (str == null) {
      return null;
    }
    return str.length() <= maxLength ? str : str.substring(0, maxLength);
  }

  public static Long bigDecimalEuroToLongCentsAmount(BigDecimal euroAmount) {
    return euroAmount != null ? euroAmount.multiply(HUNDRED).longValue() : null;
  }
}
