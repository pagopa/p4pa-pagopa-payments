package it.gov.pagopa.pu.pagopapayments.config.json.jackson3;

import it.gov.pagopa.pu.pagopapayments.config.json.OffsetDateTimeToLocalDateDeserializer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import tools.jackson.core.JsonParser;

import java.time.LocalDate;
import java.time.Month;

class OffsetDateTimeToLocalDateJackson3DeserializerTest {
  private final OffsetDateTimeToLocalDateJackson3Deserializer deserializer = new OffsetDateTimeToLocalDateJackson3Deserializer();

  @Test
  void whenDeserializeThenCallHandler(){
    try (MockedStatic<OffsetDateTimeToLocalDateDeserializer> deserializerStatic = Mockito.mockStatic(OffsetDateTimeToLocalDateDeserializer.class)) {
      LocalDate expectedResult = LocalDate.of(2025, Month.DECEMBER, 1);
      JsonParser jsonParser = Mockito.mock(JsonParser.class);

      deserializerStatic.when(() -> OffsetDateTimeToLocalDateDeserializer.parse("dateString"))
        .thenReturn(expectedResult);

      Mockito.when(jsonParser.getValueAsString())
        .thenReturn("dateString");

      LocalDate result = deserializer.deserialize(jsonParser, null);

      Assertions.assertSame(expectedResult, result);
    }
  }
}
