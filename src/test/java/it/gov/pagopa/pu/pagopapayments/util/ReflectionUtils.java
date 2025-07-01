package it.gov.pagopa.pu.pagopapayments.util;

import com.fasterxml.jackson.annotation.JsonValue;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * The Class ReflectionUtils.
 */
public class ReflectionUtils {

  /**
   * It will transform the input enum into a String, using the same logic of Jackson
   */
  public static String enum2String(Enum<?> o) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    final Object result = enum2Object(o);

    return result != null ? result.toString() : null;
  }

  /**
   * It will transform the input enum into an Object, using the same logic of Jackson
   */
  private static Object enum2Object(Enum<?> o) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
    Method jsonValueMethod = null;
    for (Method m : o.getClass().getMethods()) {
      if (m.getAnnotation(JsonValue.class) != null) {
        jsonValueMethod = m;
        break;
      }
    }

    if (jsonValueMethod == null) {
      jsonValueMethod = o.getClass().getMethod("toString");
    }

    return jsonValueMethod.invoke(o);
  }
}
