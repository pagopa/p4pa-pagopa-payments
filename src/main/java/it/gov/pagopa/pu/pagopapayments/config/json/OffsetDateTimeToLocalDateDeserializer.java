package it.gov.pagopa.pu.pagopapayments.config.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class OffsetDateTimeToLocalDateDeserializer extends JsonDeserializer<LocalDate> {
  @Override
  public LocalDate deserialize(JsonParser p, DeserializationContext ctxt) throws IOException  {
    String dateStr = p.getValueAsString();
    return parse(dateStr);
  }

  public static LocalDate parse(String dateString) {
    if (StringUtils.isBlank(dateString)) {
      return null;
    }

    if (dateString.contains("T")) {
      LocalDateTime localDateTime = OffsetDateTimeToLocalDateTimeDeserializer.parse(dateString);

      return localDateTime.toLocalDate();
    }

    return LocalDate.parse(dateString);
  }
}
