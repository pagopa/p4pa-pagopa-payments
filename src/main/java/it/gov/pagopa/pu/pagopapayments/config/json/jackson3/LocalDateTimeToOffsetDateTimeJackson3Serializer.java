package it.gov.pagopa.pu.pagopapayments.config.json.jackson3;

import it.gov.pagopa.pu.pagopapayments.config.json.LocalDateTimeToOffsetDateTimeSerializer;
import org.springframework.context.annotation.Configuration;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

import java.time.LocalDateTime;

@Configuration
public class LocalDateTimeToOffsetDateTimeJackson3Serializer extends ValueSerializer<LocalDateTime> {

  @Override
  public void serialize(LocalDateTime value, tools.jackson.core.JsonGenerator gen, SerializationContext ctxt) throws JacksonException {
    if(value!=null) {
      gen.writeString(LocalDateTimeToOffsetDateTimeSerializer.convertToOffsetDateTime(value).toString());
    }
  }
}

