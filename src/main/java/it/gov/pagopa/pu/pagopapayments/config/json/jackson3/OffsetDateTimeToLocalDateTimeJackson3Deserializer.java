package it.gov.pagopa.pu.pagopapayments.config.json.jackson3;

import it.gov.pagopa.pu.pagopapayments.config.json.OffsetDateTimeToLocalDateTimeDeserializer;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.ValueDeserializer;

import java.time.LocalDateTime;


@Configuration
public class OffsetDateTimeToLocalDateTimeJackson3Deserializer extends ValueDeserializer<LocalDateTime> {

  @Override
  public LocalDateTime deserialize(tools.jackson.core.JsonParser p, tools.jackson.databind.DeserializationContext ctxt) {
    return OffsetDateTimeToLocalDateTimeDeserializer.parse(p.getValueAsString());
  }
}

