package it.gov.pagopa.pu.pagopapayments.config.json.jackson3;

import it.gov.pagopa.pu.pagopapayments.config.json.OffsetDateTimeToLocalDateTimeDeserializer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import tools.jackson.core.JsonParser;

import java.time.LocalDateTime;

class OffsetDateTimeToLocalDateTimeJackson3DeserializerTest {

  private final OffsetDateTimeToLocalDateTimeJackson3Deserializer deserializer = new OffsetDateTimeToLocalDateTimeJackson3Deserializer();

  @Test
  void whenDeserializeThenCallHandler(){
    try(MockedStatic<OffsetDateTimeToLocalDateTimeDeserializer> deserializerStatic = Mockito.mockStatic(OffsetDateTimeToLocalDateTimeDeserializer.class)){
      LocalDateTime expectedResult = LocalDateTime.now();
      JsonParser jsonParser = Mockito.mock(JsonParser.class);

      deserializerStatic.when(()-> OffsetDateTimeToLocalDateTimeDeserializer.parse("dateString"))
          .thenReturn(expectedResult);
      Mockito.when(jsonParser.getValueAsString())
        .thenReturn("dateString");

      LocalDateTime result = deserializer.deserialize(jsonParser, null);

      Assertions.assertSame(expectedResult, result);
    }
  }
}
