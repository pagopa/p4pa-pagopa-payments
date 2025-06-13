package it.gov.pagopa.pu.pagopapayments.util;

import it.gov.pagopa.pu.pagopapayments.registry.RegistryLogger;

import java.util.Arrays;
import java.util.stream.Collectors;

public class IdentityUtils {
  private IdentityUtils() {
  }

  public static String iuv2Nav(String iuv) {
    if (iuv == null || iuv.isBlank()) {
      return null;
    } else if (iuv.contains(RegistryLogger.IUV_SEPARATOR)) {
      return Arrays.stream(iuv.split(RegistryLogger.IUV_SEPARATOR))
        .map(IdentityUtils::iuv2Nav)
        .collect(Collectors.joining(RegistryLogger.IUV_SEPARATOR));
    } else {
      return Constants.AUX_DIGIT + iuv;
    }
  }
}
