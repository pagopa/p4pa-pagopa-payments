package it.gov.pagopa.pu.pagopapayments.config.json;

import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

public class JsonDeserializationProblemHandler extends com.fasterxml.jackson.databind.deser.DeserializationProblemHandler {

  @Override
  public boolean handleUnknownProperty(com.fasterxml.jackson.databind.DeserializationContext ctxt, com.fasterxml.jackson.core.JsonParser p, JsonDeserializer<?> deserializer, Object beanOrClass, String propertyName) throws IOException {
    if (isUnknownPropertyNotAllowed(beanOrClass)) {
      throw com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException.from(p,
        beanOrClass, propertyName, (deserializer == null) ? null : deserializer.getKnownPropertyNames());
    }
    return false;
  }


  public static boolean isUnknownPropertyNotAllowed(Object beanOrClass) {
    return beanOrClass.getClass().getAnnotation(JsonUnknownPropertiesNotAllowed.class) != null;
  }
}
