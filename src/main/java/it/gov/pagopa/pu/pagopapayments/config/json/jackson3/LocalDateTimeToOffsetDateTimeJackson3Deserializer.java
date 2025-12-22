package it.gov.pagopa.pu.pagopapayments.config.json.jackson3;

import it.gov.pagopa.pu.pagopapayments.config.json.LocalDateTimeToOffsetDateTimeDeserializer;
import org.springframework.context.annotation.Configuration;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

import java.time.OffsetDateTime;

@Configuration
public class LocalDateTimeToOffsetDateTimeJackson3Deserializer extends ValueDeserializer<OffsetDateTime> {

  @Override
  public OffsetDateTime deserialize(JsonParser p, DeserializationContext ctxt) {
    String dateString = p.getValueAsString();
    return LocalDateTimeToOffsetDateTimeDeserializer.parseString(dateString);
  }
}
