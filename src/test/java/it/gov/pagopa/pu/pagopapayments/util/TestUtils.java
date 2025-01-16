package it.gov.pagopa.pu.pagopapayments.util;

import jakarta.annotation.Nullable;
import org.junit.jupiter.api.Assertions;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.util.ReflectionUtils;
import uk.co.jemos.podam.api.AbstractExternalFactory;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;

import javax.xml.datatype.XMLGregorianCalendar;
import java.lang.reflect.Type;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class TestUtils {

  private static final String ACCESS_TOKEN = "TOKENHEADER.TOKENPAYLOAD.TOKENDIGEST";

  public static String getFakeAccessToken() {
    return ACCESS_TOKEN;
  }

  public static void setFakeAccessTokenInContext(){
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
        if(pojoClass.isAssignableFrom(XMLGregorianCalendar.class)) {
          return (T) ConversionUtils.toXMLGregorianCalendar(OffsetDateTime.now());
        }
        return null;
      }

      @Override
      public <T> T populatePojo(T pojo, Type... genericTypeArgs) {
        return null;
      }
    };
    return new PodamFactoryImpl(externalFactory);
  }

  /**
   * It will assert not null on all o's fields
   */
  public static void checkNotNullFields(Object o, String... excludedFields) {
    Set<String> excludedFieldsSet = new HashSet<>(Arrays.asList(excludedFields));
    ReflectionUtils.doWithFields(o.getClass(),
      f -> {
        f.setAccessible(true);
        Assertions.assertNotNull(f.get(o), "The field "+f.getName()+" of the input object of type "+o.getClass()+" is null!");
      },
      f -> !excludedFieldsSet.contains(f.getName()));
  }

  public static void checkNotNullFieldsUsingNullableAnnotation(Object o, String... excludedFields) {
    Set<String> excludedFieldsSet = new HashSet<>(Arrays.asList(excludedFields));
    ReflectionUtils.doWithFields(o.getClass(),
      f -> {
        f.setAccessible(true);
        Assertions.assertNotNull(f.get(o), "The field "+f.getName()+" of the input object of type "+o.getClass()+" is null!");
      },
      f -> !excludedFieldsSet.contains(f.getName()) && f.getAnnotation(Nullable.class)==null);
  }

}
