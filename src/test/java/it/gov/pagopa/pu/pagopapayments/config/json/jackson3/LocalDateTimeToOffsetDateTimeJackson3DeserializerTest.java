package it.gov.pagopa.pu.pagopapayments.config.json.jackson3;

import it.gov.pagopa.pu.pagopapayments.config.json.LocalDateTimeToOffsetDateTimeDeserializer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import tools.jackson.core.JsonParser;

import java.time.OffsetDateTime;

class LocalDateTimeToOffsetDateTimeJackson3DeserializerTest {

  private final LocalDateTimeToOffsetDateTimeJackson3Deserializer deserializer = new LocalDateTimeToOffsetDateTimeJackson3Deserializer();

  @Test
  void whenDeserializeThenCallHandler(){
    try(MockedStatic<LocalDateTimeToOffsetDateTimeDeserializer> deserializerStatic = Mockito.mockStatic(LocalDateTimeToOffsetDateTimeDeserializer.class)){
      OffsetDateTime expectedResult = OffsetDateTime.now();
      JsonParser jsonParser = Mockito.mock(JsonParser.class);

      deserializerStatic.when(()-> LocalDateTimeToOffsetDateTimeDeserializer.parseString("dateString"))
          .thenReturn(expectedResult);
      Mockito.when(jsonParser.getValueAsString())
        .thenReturn("dateString");

      OffsetDateTime result = deserializer.deserialize(jsonParser, null);

      Assertions.assertSame(expectedResult, result);
    }
  }
}
