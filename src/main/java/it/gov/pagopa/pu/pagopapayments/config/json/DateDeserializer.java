package it.gov.pagopa.pu.pagopapayments.config.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import io.micrometer.common.util.StringUtils;
import it.gov.pagopa.pu.pagopapayments.exception.common.InvalidValueException;
import it.gov.pagopa.pu.pagopapayments.util.ErrorCodeConstants;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateDeserializer extends JsonDeserializer<Date> {

  private final SimpleDateFormat italianDateFormat = new SimpleDateFormat("dd/MM/yyyy");

  @Override
  public Date deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
    String dateStr = p.getValueAsString();

    if (StringUtils.isBlank(dateStr) || dateStr.equals("-")) {
      return null;
    }

    try {
      return italianDateFormat.parse(dateStr);
    } catch (ParseException e) {
      throw new InvalidValueException(ErrorCodeConstants.ERROR_CODE_INVALID_DATE_FORMAT, "Unknown date format: " + dateStr);
    }
  }
}
