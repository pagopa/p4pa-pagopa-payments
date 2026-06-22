package it.gov.pagopa.pu.pagopapayments.config.json;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Custom annotation used to throw an exception if a JSON contains unknown field */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface JsonUnknownPropertiesNotAllowed {
}
