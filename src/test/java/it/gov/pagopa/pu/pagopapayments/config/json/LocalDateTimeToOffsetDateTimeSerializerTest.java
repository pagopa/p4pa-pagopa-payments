package it.gov.pagopa.pu.pagopapayments.config.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.TimeZone;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class LocalDateTimeToOffsetDateTimeSerializerTest {

  @Mock
  private JsonGenerator jsonGenerator;

  @Mock
  private SerializerProvider serializerProvider;

  private LocalDateTimeToOffsetDateTimeSerializer dateTimeSerializer;

  @BeforeEach
  void setUp() {
    dateTimeSerializer = new LocalDateTimeToOffsetDateTimeSerializer();
  }

  @Test
  void testDateSerializer() throws IOException {
    LocalDateTime localDateTime = LocalDateTime.of(2025, 1, 16, 9, 15, 20);

    TimeZone.setDefault(TimeZone.getTimeZone("Europe/Rome"));

    dateTimeSerializer.serialize(localDateTime, jsonGenerator, serializerProvider);

    verify(jsonGenerator).writeString("2025-01-16T09:15:20+01:00");
  }

  @Test
  void testNullDateSerializer() throws IOException {
    dateTimeSerializer.serialize(null, jsonGenerator, serializerProvider);

    verifyNoInteractions(jsonGenerator);
  }
}
