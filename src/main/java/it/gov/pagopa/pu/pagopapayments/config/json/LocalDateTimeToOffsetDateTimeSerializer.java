package it.gov.pagopa.pu.pagopapayments.config.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;

@Configuration
public class LocalDateTimeToOffsetDateTimeSerializer extends JsonSerializer<LocalDateTime> {

  @Override
  public void serialize(LocalDateTime value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
    if (value != null) {
      OffsetDateTime offsetDateTime = convertToOffsetDateTime(value);
      gen.writeString(offsetDateTime.toString());
    }
  }

  public static OffsetDateTime convertToOffsetDateTime(LocalDateTime value) {
    return value.atZone(ZoneId.systemDefault()).toOffsetDateTime();
  }
}

