package it.gov.pagopa.pu.pagopapayments.util;

import io.micrometer.common.util.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.MDC;

public class UtilitiesTest {

  @Test
  void testGetTraceId(){
    // Given
    String expectedResult = "TRACEID";
    setTraceId(expectedResult);

    // When
    String result = Utilities.getTraceId();

    // Then
    Assertions.assertSame(expectedResult, result);
    clearTraceIdContext();
  }

  @ParameterizedTest
  @ValueSource(strings = {"IUV","IUV1,IUV2,IUV3"})
  @NullAndEmptySource
  void testIuv2Nav(String iuv){
    // Given
    String expectedResult = StringUtils.isBlank(iuv) ? null : ("IUV".equals(iuv) ? "3IUV" : "3IUV1,3IUV2,3IUV3");

    // When
    String result = Utilities.iuv2Nav(iuv);

    // Then
    Assertions.assertEquals(expectedResult, result);
  }

  @ParameterizedTest
  @ValueSource(strings = {"3NAV","3NAV1,3NAV2,3NAV3"})
  @NullAndEmptySource
  void testNav2Iuv(String nav){
    // Given
    String expectedResult = StringUtils.isBlank(nav) ? null : ("3NAV".equals(nav) ? "NAV" : "NAV1,NAV2,NAV3");

    // When
    String result = Utilities.nav2Iuv(nav);

    // Then
    Assertions.assertEquals(expectedResult, result);
  }

  @Test
  void testInvalidNav2Iuv(){
    // Given
    String nav = "INVALID_NAV";

    // When
    IllegalArgumentException result = Assertions.assertThrows(IllegalArgumentException.class,()->Utilities.nav2Iuv(nav));

    // Then
    Assertions.assertTrue(result.getMessage().startsWith("Invalid NAV format: "));
  }

  public static void setTraceId(String traceId) {
    MDC.put("traceId", traceId);
  }
  public static void clearTraceIdContext(){
    MDC.clear();
  }
}
