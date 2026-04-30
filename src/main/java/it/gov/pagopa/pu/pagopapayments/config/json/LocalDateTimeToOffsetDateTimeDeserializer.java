package it.gov.pagopa.pu.pagopapayments.config.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import it.gov.pagopa.pu.pagopapayments.util.Constants;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;

@Configuration
public class LocalDateTimeToOffsetDateTimeDeserializer extends JsonDeserializer<OffsetDateTime> {

  @Override
  public OffsetDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
    String dateString = p.getValueAsString();
    return parseString(dateString);
  }

  public static OffsetDateTime parseString(String dateString) {
    if (dateString.contains("+") || dateString.endsWith("Z")) {
      return OffsetDateTime.parse(dateString);
    } else {
      return ZonedDateTime.of(LocalDateTime.parse(dateString), Constants.ZONEID).toOffsetDateTime();
    }
  }
}
