package it.gov.pagopa.pu.pagopapayments.config.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import it.gov.pagopa.pu.pagopapayments.util.Constants;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;


@Configuration
public class OffsetDateTimeToLocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {

  @Override
  public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
    String dateString = p.getValueAsString();
    return parse(dateString);
  }

  public static LocalDateTime parse(String dateString) {
    if(dateString.contains("+") || dateString.endsWith("Z")){
      return OffsetDateTime.parse(dateString).atZoneSameInstant(Constants.ZONEID).toLocalDateTime();
    } else {
      return LocalDateTime.parse(dateString);
    }
  }
}

