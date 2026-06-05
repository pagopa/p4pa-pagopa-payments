package it.gov.pagopa.pu.pagopapayments.config.json;

import com.fasterxml.jackson.core.JsonParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;

import java.io.IOException;
import java.time.LocalDate;
import java.time.Month;

class OffsetDateTimeToLocalDateDeserializerTest {
  private final OffsetDateTimeToLocalDateDeserializer deserializer = new OffsetDateTimeToLocalDateDeserializer();

  @ParameterizedTest
  @ValueSource(strings = {
    "2025-12-25",
    "2025-12-25T10:30:00+01:00",
    "2025-12-25T10:30:00"
  })
  void givenValidDateStringWhenDeserializeThenExtractedLocalDate(String dateString) throws IOException {
    JsonParser parser = Mockito.mock(JsonParser.class);
    Mockito.when(parser.getValueAsString()).thenReturn(dateString);

    LocalDate result = deserializer.deserialize(parser, null);

    Assertions.assertEquals(LocalDate.of(2025, Month.DECEMBER, 25), result);
  }

  @ParameterizedTest
  @NullAndEmptySource
  void givenNullOrEmptyStringWhenDeserializeThenNull(String invalidString) throws IOException {
    JsonParser parser = Mockito.mock(JsonParser.class);
    Mockito.when(parser.getValueAsString()).thenReturn(invalidString);

    LocalDate result = deserializer.deserialize(parser, null);

    Assertions.assertNull(result);
  }
}
