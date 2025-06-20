package it.gov.pagopa.pu.pagopapayments.util;

import it.gov.pagopa.pu.pagopapayments.registry.RegistryLogger;
import org.slf4j.MDC;

import java.util.Arrays;
import java.util.stream.Collectors;

public class Utilities {
  private Utilities() {
  }

  public static String getTraceId() {
    return MDC.get("traceId");
  }

  public static String iuv2Nav(String iuv) {
    if (iuv == null || iuv.isBlank()) {
      return null;
    } else if (iuv.contains(RegistryLogger.IUV_SEPARATOR)) {
      return Arrays.stream(iuv.split(RegistryLogger.IUV_SEPARATOR))
        .map(Utilities::iuv2Nav)
        .collect(Collectors.joining(RegistryLogger.IUV_SEPARATOR));
    } else {
      return Constants.AUX_DIGIT + iuv;
    }
  }

  public static String nav2Iuv(String nav) {
    if (nav == null || nav.isBlank()) {
      return null;
    } else if (nav.contains(RegistryLogger.IUV_SEPARATOR)) {
      return Arrays.stream(nav.split(RegistryLogger.IUV_SEPARATOR))
              .map(Utilities::nav2Iuv)
              .collect(Collectors.joining(RegistryLogger.IUV_SEPARATOR));
    } else if (nav.length() < 2 || !nav.startsWith(Constants.AUX_DIGIT)) {
      throw new IllegalArgumentException("Invalid NAV format: " + nav);
    } else {
      return nav.substring(1);
    }
  }
}
