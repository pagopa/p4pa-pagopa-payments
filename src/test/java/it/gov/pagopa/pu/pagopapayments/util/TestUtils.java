package it.gov.pagopa.pu.pagopapayments.util;

import jakarta.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import uk.co.jemos.podam.api.*;
import uk.co.jemos.podam.common.ManufacturingContext;
import uk.co.jemos.podam.typeManufacturers.AbstractTypeManufacturer;

import javax.xml.datatype.XMLGregorianCalendar;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.OffsetDateTime;
import java.time.chrono.ChronoZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.*;

@Slf4j
public class TestUtils {

  static {
    clearDefaultTimezone();
  }

  public static void clearDefaultTimezone() {
    TimeZone.setDefault(Constants.DEFAULT_TIMEZONE);
  }

  private static final String ACCESS_TOKEN = "TOKENHEADER.TOKENPAYLOAD.TOKENDIGEST";

  public static String getFakeAccessToken() {
    return ACCESS_TOKEN;
  }

  public static void setFakeAccessTokenInContext() {
    SecurityContextHolder.setContext(new SecurityContextImpl(new JwtAuthenticationToken(Jwt
      .withTokenValue(ACCESS_TOKEN)
      .header("", "")
      .claim("", "")
      .build())));
  }

  public static PodamFactory getPodamFactory() {
    PodamFactory externalFactory = new AbstractExternalFactory() {
      @Override
      public <T> T manufacturePojo(Class<T> pojoClass, Type... genericTypeArgs) {
        if (pojoClass.isAssignableFrom(XMLGregorianCalendar.class)) {
          //noinspection unchecked: guarantee from assignable condition
          return (T) ConversionUtils.toXMLGregorianCalendar(OffsetDateTime.now());
        }
        return null;
      }

      @Override
      public <T> T populatePojo(T pojo, Type... genericTypeArgs) {
        return null;
      }
    };
    PodamFactoryImpl podamFactory = new PodamFactoryImpl(externalFactory);
    podamFactory.getStrategy().addOrReplaceTypeManufacturer(SortedSet.class, new AbstractTypeManufacturer<>() {
      @Override
      public SortedSet<?> getType(DataProviderStrategy strategy, AttributeMetadata attributeMetadata, ManufacturingContext manufacturingCtx) {
        return new TreeSet<>();
      }
    });
    return podamFactory;
  }

  /**
   * It will assert not null on all o's fields
   */
  public static void checkNotNullFields(Object o, String... excludedFields) {
    Set<String> excludedFieldsSet = new HashSet<>(Arrays.asList(excludedFields));
    org.springframework.util.ReflectionUtils.doWithFields(o.getClass(),
      f -> {
        f.setAccessible(true);
        Assertions.assertNotNull(f.get(o), "The field " + f.getName() + " of the input object of type " + o.getClass() + " is null!");
      },
      f -> !excludedFieldsSet.contains(f.getName()));
  }

  public static void checkNotNullFieldsUsingNullableAnnotation(Object o, String... excludedFields) {
    Set<String> excludedFieldsSet = new HashSet<>(Arrays.asList(excludedFields));
    org.springframework.util.ReflectionUtils.doWithFields(o.getClass(),
      f -> {
        f.setAccessible(true);
        Assertions.assertNotNull(f.get(o), "The field " + f.getName() + " of the input object of type " + o.getClass() + " is null!");
      },
      f -> !excludedFieldsSet.contains(f.getName()) && f.getAnnotation(Nullable.class) == null);
  }


  public static List<Method> reflectionEqualsByName(Object o1, Object o2, String... ignoredFields) {
    Set<String> ignoredFieldSet = new HashSet<>(Arrays.asList(ignoredFields));
    Assertions.assertFalse(o1 == null ^ o2 == null, String.format("Both objects have to be null or not null:%n%s%n%s", o1 == null ? "null" : o1.getClass().getName(), o2 == null ? "null" : o2.getClass().getName()));

    List<Method> checked = new ArrayList<>();

    if (o1 != null) {
      for (Method m1 : o1.getClass().getMethods()) {
        if ((m1.getName().startsWith("get") || m1.getName().startsWith("is")) && m1.getParameterCount() == 0 && !"getClass".equals(m1.getName())) {
          String fieldName = StringUtils.uncapitalize(m1.getName().replaceFirst("^(?:get|is)", ""));
          if (ignoredFieldSet.contains(fieldName)) {
            continue;
          }
          Method m2 = null;
          try {
            m2 = Arrays.stream(o2.getClass().getMethods()).filter(m -> m.getName().equalsIgnoreCase(m1.getName()) && m.getParameterCount() == m1.getParameterCount()).findFirst().orElse(null);

            if (m2 == null) {
              throw new NoSuchMethodException();
            }

            Object v1 = m1.invoke(o1);
            Object v2 = m2.invoke(o2);

            boolean result = true;

            Assertions.assertFalse(v1 == null ^ v2 == null, String.format("Both objects have to be null or not null:%n%s = %s%n%s = %s", m1, v1 == null ? "null" : v1.getClass().getName(), m2, v2 == null ? "null" : v2.getClass().getName()));
            if (v1 != null) {
              if (v1.equals(v2)) {
                //Do Nothing
              } else if (v1 instanceof Comparable v1Comparable && v2 instanceof Comparable v2Comparable && compareEquals(v1Comparable, v2Comparable)) {
                //Do Nothing
              } else if (OffsetDateTime.class.isAssignableFrom(v1.getClass()) && OffsetDateTime.class.isAssignableFrom(v2.getClass())) {
                result = ((OffsetDateTime) v1).isEqual((OffsetDateTime) v2);
              } else if (ChronoZonedDateTime.class.isAssignableFrom(v1.getClass()) && ChronoZonedDateTime.class.isAssignableFrom(v2.getClass())) {
                result = ((ChronoZonedDateTime<?>) v1).isEqual((ChronoZonedDateTime<?>) v2);
              } else if (v1.getClass().isAssignableFrom(v2.getClass()) && ((v1.getClass().isPrimitive() && v2.getClass().isPrimitive()) || (hasStandardEquals(v1.getClass()) && hasStandardEquals(v2.getClass())))) {
                result = false;
              } else if (BigInteger.class.isAssignableFrom(v1.getClass()) && Integer.class.isAssignableFrom(v2.getClass())) {
                result = ((BigInteger) v1).intValue() == ((int) v2);
              } else if (BigInteger.class.isAssignableFrom(v2.getClass()) && Integer.class.isAssignableFrom(v1.getClass())) {
                result = ((BigInteger) v2).intValue() == ((int) v1);
              } else if (BigInteger.class.isAssignableFrom(v1.getClass()) && Long.class.isAssignableFrom(v2.getClass())) {
                result = ((BigInteger) v1).longValue() == ((long) v2);
              } else if (BigInteger.class.isAssignableFrom(v2.getClass()) && Long.class.isAssignableFrom(v1.getClass())) {
                result = ((BigInteger) v2).longValue() == ((long) v1);
              } else if (String.class.isAssignableFrom(v1.getClass()) && Enum.class.isAssignableFrom(v2.getClass())) {
                v2 = ReflectionUtils.enum2String((Enum<?>) v2);
                result = v1.equals(v2);
              } else if (String.class.isAssignableFrom(v2.getClass()) && Enum.class.isAssignableFrom(v1.getClass())) {
                v1 = ReflectionUtils.enum2String((Enum<?>) v1);
                result = v2.equals(v1);
              } else if (v1 instanceof Collection<?> collV1 && v2 instanceof Collection<?> collV2 && collV1.size() == collV2.size()) {
                Iterator<?> it1 = collV1.iterator();
                Iterator<?> it2 = collV2.iterator();
                while (it1.hasNext() && it2.hasNext()) {
                  checked.addAll(reflectionEqualsByName(it1.next(), it2.next()));
                }
              } else if (v1.getClass().isArray() && v2.getClass().isArray() && Array.getLength(v1) == Array.getLength(v2)) {
                for (int i = 0; i < Array.getLength(v1); i++) {
                  checked.addAll(reflectionEqualsByName(Array.get(v1, i), Array.get(v2, i)));
                }
              } else {
                boolean equals = v1.toString().equals(v2.toString());
                if (Enum.class.isAssignableFrom(v2.getClass()) && Enum.class.isAssignableFrom(v1.getClass())) {
                  result = equals;
                } else if (String.class.isAssignableFrom(v1.getClass()) || String.class.isAssignableFrom(v2.getClass())) {
                  result = equals;
                } else {
                  List<Method> addedMethods = reflectionEqualsByName(v1, v2);
                  Assertions.assertFalse(addedMethods.isEmpty(), String.format("Invalid compare between methods%n%s = %s%n%s = %s", m1, v1, m2, v2));
                  checked.addAll(addedMethods);
                }
              }

              checked.add(m1);
            }

            Assertions.assertTrue(result, String.format("Invalid compare between methods%n%s = %s%n%s = %s", m1, v1, m2, v2));
          } catch (NoSuchMethodException e) {
            log.warn("Method {} is not defined in {}{}", m1, o2.getClass().getName(), e.getMessage());
          } catch (Exception e) {
            throw new IllegalStateException(String.format("[ERROR] Something gone wrong comparing %s with %s%n%s", m1, m2, e.getMessage()), e);
          }
        }
      }
    }
    return checked;
  }

  private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.nnnnnnnnn");

  private static <T> boolean compareEquals(Comparable<T> v1, T v2) {
    try {
      //specific equality tests for TemporalAccessor classes
      if (v1 instanceof TemporalAccessor v1Time && v2 instanceof TemporalAccessor v2Time) {
        //ignore timezone (for localDate/Time objects)
        return StringUtils.equals(formatter.format(v1Time), formatter.format(v2Time));
      } else {
        //generic fallback
        return v1.compareTo(v2) == 0;
      }
    } catch (ClassCastException cce) {
      log.warn("cannot compare {} with {}", ClassUtils.getName(v1), ClassUtils.getName(v2));
      return false;
    }
  }

  private static boolean hasStandardEquals(Class<?> clazz) {
    try {
      return !clazz.getMethod("equals", Object.class).equals(Object.class.getMethod("equals", Object.class));
    } catch (NoSuchMethodException e) {
      // This exception cannot be thrown
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  public static boolean checkDecimalPlaces(BigDecimal number, int decimalPlaces) {
    try {
      BigDecimal scaledBigDecimal = number.movePointRight(decimalPlaces); //this scaled BigDecimal should have zero decimal places
      return scaledBigDecimal.toBigInteger().equals(scaledBigDecimal.toBigIntegerExact());
    } catch (ArithmeticException e) {
      return false;
    }
  }
}
