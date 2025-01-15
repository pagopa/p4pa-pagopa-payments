package it.gov.pagopa.pu.pagopapayments.util;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import uk.co.jemos.podam.api.AbstractExternalFactory;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;

import javax.xml.datatype.XMLGregorianCalendar;
import java.lang.reflect.Type;
import java.time.OffsetDateTime;

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


}
