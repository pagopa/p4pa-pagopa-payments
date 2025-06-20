package it.gov.pagopa.pu.pagopapayments.util;

import it.gov.pagopa.pu.pagopapayments.exception.ValidatorException;
import it.gov.pagopa.pu.pagopapayments.registry.RegistryLogger;
import org.slf4j.MDC;

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

  public static String getTraceId() {
    return MDC.get("traceId");
  }

  public static String numeroAvvisoToIuvValidator(String numeroAvviso) {
    String iuv;
    if (numeroAvviso!= null && numeroAvviso.length() == 18) {
      if (numeroAvviso.startsWith(Constants.OLD_IUV_AUX_DIGIT)) {
        iuv = numeroAvviso.substring(3);
      } else if (numeroAvviso.startsWith(Constants.SMALL_IUV_AUX_DIGIT)) {
        iuv = numeroAvviso.substring(1);
      } else {
        throw new ValidatorException("Codice avviso / IUV non corretto.");
      }
    } else if (numeroAvviso!= null && (numeroAvviso.length() == 15 || numeroAvviso.length() == 17)) {
      iuv = numeroAvviso;
    } else {
      throw new ValidatorException("Codice avviso / IUV non corretto.");
    }
    return iuv;
  }
}
