package it.gov.pagopa.pu.pagopapayments.config.json.jackson3;

import it.gov.pagopa.pu.pagopapayments.config.json.JsonDeserializationProblemHandler;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.deser.DeserializationProblemHandler;
import tools.jackson.databind.exc.UnrecognizedPropertyException;

public class JsonDeserializationJackson3ProblemHandler extends DeserializationProblemHandler {
  @Override
  public boolean handleUnknownProperty(DeserializationContext ctxt, JsonParser p, ValueDeserializer<?> deserializer, Object beanOrClass, String propertyName) throws JacksonException {
    if (JsonDeserializationProblemHandler.isUnknownPropertyNotAllowed(beanOrClass)) {
      throw UnrecognizedPropertyException.from(p,
        beanOrClass, propertyName, (deserializer == null) ? null : deserializer.getKnownPropertyNames());
    }
    return false;
  }
}
