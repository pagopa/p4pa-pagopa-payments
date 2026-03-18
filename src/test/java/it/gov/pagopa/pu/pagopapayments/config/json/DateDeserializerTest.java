package it.gov.pagopa.pu.pagopapayments.config.json;

import com.fasterxml.jackson.core.JsonParser;
import it.gov.pagopa.pu.pagopapayments.exception.InvalidValueException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

class DateDeserializerTest {

  private final DateDeserializer deserializer = new DateDeserializer();

  @Test
  void givenValidDateWhenDeserializeThenOk() throws IOException {
    // Given
    Date expectedResult = Date.from(LocalDate.now()
      .atStartOfDay(ZoneId.systemDefault())
      .toInstant());
    JsonParser parser = Mockito.mock(JsonParser.class);
    Mockito.when(parser.getValueAsString())
      .thenReturn(expectedResult.toInstant()
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

    // When
    Date result = deserializer.deserialize(parser, null);

    // Then
    Assertions.assertEquals(expectedResult, result);
  }

  @Test
  void givenInvalidDateWhenDeserializeThenInvalidValueException() throws IOException {
    // Given
    Date date = Date.from(LocalDate.now()
      .atStartOfDay(ZoneId.systemDefault())
      .toInstant());
    JsonParser parser = Mockito.mock(JsonParser.class);
    Mockito.when(parser.getValueAsString())
      .thenReturn(date.toInstant()
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));

    // When
    Assertions.assertThrows(InvalidValueException.class, () -> deserializer.deserialize(parser, null));
  }

  @Test
  void givenEmptyStringWhenThenNull() throws IOException {
    // Given
    JsonParser parser = Mockito.mock(JsonParser.class);
    Mockito.when(parser.getValueAsString())
      .thenReturn("");

    // When
    Date result = deserializer.deserialize(parser, null);

    // Then
    Assertions.assertNull(result);
  }

  @Test
  void givenNullStringWhenThenNull() throws IOException {
    // Given
    JsonParser parser = Mockito.mock(JsonParser.class);
    Mockito.when(parser.getValueAsString())
      .thenReturn(null);

    // When
    Date result = deserializer.deserialize(parser, null);

    // Then
    Assertions.assertNull(result);
  }
}


