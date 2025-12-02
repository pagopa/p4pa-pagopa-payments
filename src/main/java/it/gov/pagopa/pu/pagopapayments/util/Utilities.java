package it.gov.pagopa.pu.pagopapayments.util;

import org.slf4j.MDC;

import java.util.Arrays;
import java.util.stream.Collectors;

public class Utilities {
  private Utilities() {
  }

  public static final String IUV_SEPARATOR = ",";

  public static String getTraceId() {
    return MDC.get("traceId");
  }

  public static String iuv2Nav(String iuv) {
    if (iuv == null || iuv.isBlank()) {
      return null;
    } else if (iuv.contains(IUV_SEPARATOR)) {
      return Arrays.stream(iuv.split(IUV_SEPARATOR))
        .map(Utilities::iuv2Nav)
        .collect(Collectors.joining(IUV_SEPARATOR));
    } else {
      return Constants.AUX_DIGIT + iuv;
    }
  }

  public static String nav2Iuv(String nav) {
    if (nav == null || nav.isBlank()) {
      return null;
    } else if (nav.contains(IUV_SEPARATOR)) {
      return Arrays.stream(nav.split(IUV_SEPARATOR))
              .map(Utilities::nav2Iuv)
              .collect(Collectors.joining(IUV_SEPARATOR));
    } else if (nav.length() < 2 || !nav.startsWith(Constants.AUX_DIGIT)) {
      throw new IllegalArgumentException("Invalid NAV format: " + nav);
    } else {
      return nav.substring(1);
    }
  }

  public static String truncateFullName(String fullName) {
    // maxLenght value is found @ resources/soap/wsdl/xsd/paForNode.xsd:174
    return safeTruncate(fullName, 70);
  }

  public static String truncateRemittanceInformation(String remittanceInformation) {
    // maxLenght value is found @ resources/soap/wsdl/xsd/paForNode.xsd:416
    return safeTruncate(remittanceInformation, 140);
  }

  private static String safeTruncate(String str, int maxLength) {
    if (str == null) {
      return null;
    }
    return str.length() <= maxLength ? str : str.substring(0, maxLength);
  }
}
