package it.gov.pagopa.pu.pagopapayments.config.json.jackson3;

import it.gov.pagopa.pu.pagopapayments.config.json.OffsetDateTimeToLocalDateDeserializer;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.ValueDeserializer;

import java.time.LocalDate;

@Configuration
public class OffsetDateTimeToLocalDateJackson3Deserializer extends ValueDeserializer<LocalDate> {
  @Override
  public LocalDate deserialize(tools.jackson.core.JsonParser p, tools.jackson.databind.DeserializationContext ctxt) {
    return OffsetDateTimeToLocalDateDeserializer.parse(p.getValueAsString());
  }
}
