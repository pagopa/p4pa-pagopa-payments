package it.gov.pagopa.pu.pagopapayments.config.json;

import com.fasterxml.jackson.core.JsonParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.time.LocalDate;

class OffsetDateTimeToLocalDateDeserializerTest {
  private final OffsetDateTimeToLocalDateDeserializer deserializer = new OffsetDateTimeToLocalDateDeserializer();

  @Test
  void givenLocalDateWhenDeserializeThenOk() throws IOException {
    JsonParser parser = Mockito.mock(JsonParser.class);
    Mockito.when(parser.getValueAsString()).thenReturn("2025-12-25");

    LocalDate result = deserializer.deserialize(parser, null);

    Assertions.assertEquals(LocalDate.of(2025, 12, 25), result);
  }

  @Test
  void givenOffsetDateTimeWhenDeserializeThenExtractedLocalDate() throws IOException {
    JsonParser parser = Mockito.mock(JsonParser.class);
    Mockito.when(parser.getValueAsString()).thenReturn("2025-12-25T10:30:00+01:00");

    LocalDate result = deserializer.deserialize(parser, null);

    Assertions.assertEquals(LocalDate.of(2025, 12, 25), result);
  }

  @Test
  void givenLocalDateTimeWhenDeserializeThenExtractedLocalDate() throws IOException {
    JsonParser parser = Mockito.mock(JsonParser.class);
    Mockito.when(parser.getValueAsString()).thenReturn("2025-12-25T10:30:00");

    LocalDate result = deserializer.deserialize(parser, null);

    Assertions.assertEquals(LocalDate.of(2025, 12, 25), result);
  }

  @Test
  void givenEmptyStringWhenDeserializeThenNull() throws IOException {
    JsonParser parser = Mockito.mock(JsonParser.class);
    Mockito.when(parser.getValueAsString()).thenReturn("");

    LocalDate result = deserializer.deserialize(parser, null);

    Assertions.assertNull(result);
  }

  @Test
  void givenNullStringWhenDeserializeThenNull() throws IOException {
    JsonParser parser = Mockito.mock(JsonParser.class);
    Mockito.when(parser.getValueAsString()).thenReturn(null);

    LocalDate result = deserializer.deserialize(parser, null);

    Assertions.assertNull(result);
  }
}
