package it.gov.pagopa.pu.pagopapayments.config.json;

import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public class LocalDateDeserializerWithFallbackOnDateTime extends ValueDeserializer<LocalDate> {
  @Override
  public LocalDate deserialize(JsonParser p, DeserializationContext ctx) {
    String dateStr = p.getValueAsString();

    if (dateStr.contains("T")) {
      return OffsetDateTime.parse(dateStr).toLocalDate();
    }

    return LocalDate.parse(dateStr);
  }
}
