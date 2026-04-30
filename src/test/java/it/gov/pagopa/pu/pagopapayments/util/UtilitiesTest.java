package it.gov.pagopa.pu.pagopapayments.util;

import io.micrometer.common.util.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.MDC;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class UtilitiesTest {

  private static final String AUX_DIGIT = "3";

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
    String result = Utilities.iuv2Nav(iuv, AUX_DIGIT);

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
    String result = Utilities.nav2Iuv(nav, AUX_DIGIT);

    // Then
    Assertions.assertEquals(expectedResult, result);
  }

  @Test
  void testInvalidNav2Iuv(){
    // Given
    String nav = "INVALID_NAV";

    // When
    IllegalArgumentException result = Assertions.assertThrows(IllegalArgumentException.class,()->Utilities.nav2Iuv(nav, AUX_DIGIT));

    // Then
    Assertions.assertTrue(result.getMessage().startsWith("Invalid NAV format: "));
  }

  public static void setTraceId(String traceId) {
    MDC.put("traceId", traceId);
  }
  public static void clearTraceIdContext(){
    MDC.clear();
  }

  @Test
  void testTruncateFullName() {
    String under70Name = "fullName";
    String over70Name = "Alexandrius WetherfordSilvermanValencourtMontgomeryEllingsworthBrade70hireRavenbrookThornfieldTalren";
    String expectedTruncatedOver70Name = "Alexandrius WetherfordSilvermanValencourtMontgomeryEllingsworthBrade70";

    Assertions.assertNull(Utilities.truncateFullName(null));
    Assertions.assertEquals(under70Name,
      Utilities.truncateFullName(under70Name));
    Assertions.assertEquals(expectedTruncatedOver70Name,
      Utilities.truncateFullName(over70Name));
  }

  @Test
  void testTruncateRemittanceInformation() {
    String under140Description = "remittanceInformation";
    String over140Description = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad mi140 veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.";
    String expectedTruncatedOver140Description = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad mi140";

    Assertions.assertNull(Utilities.truncateRemittanceInformation(null));
    Assertions.assertEquals(under140Description,
      Utilities.truncateRemittanceInformation(under140Description));
    Assertions.assertEquals(expectedTruncatedOver140Description,
      Utilities.truncateRemittanceInformation(over140Description));
  }

  @Test
  void givenBigDecimalEuroAmountWhenBigDecimalEuroToLongCentsAmountThenReturnCorrectCents() {
    BigDecimal amount = BigDecimal.valueOf(123.45);

    long result = Utilities.bigDecimalEuroToLongCentsAmount(amount);

    assertEquals(12345, result);
  }

  @Test
  void givenNullAmountWhenBigDecimalEuroToLongCentsAmountThenReturnNull() {
    Long result = Utilities.bigDecimalEuroToLongCentsAmount(null);

    assertNull(result);
  }
}
