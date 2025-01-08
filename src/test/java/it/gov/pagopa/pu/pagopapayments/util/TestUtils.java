package it.gov.pagopa.pu.pagopapayments.util;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

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


}
