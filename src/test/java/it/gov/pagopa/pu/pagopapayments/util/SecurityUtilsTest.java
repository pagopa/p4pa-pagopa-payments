package it.gov.pagopa.pu.pagopapayments.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.net.URI;

public class SecurityUtilsTest {

  @AfterEach
  void clear(){
    SecurityContextHolder.clearContext();
    RequestContextHolder.resetRequestAttributes();
  }

//region getAccessToken
  @Test
  void givenNoSecurityContextWhenGetAccessTokenThenNull(){
    Assertions.assertNull(SecurityUtils.getAccessToken());
  }

  @Test
  void givenNoAuthenticationWhenGetAccessTokenThenNull(){
    SecurityContextHolder.setContext(new SecurityContextImpl());
    Assertions.assertNull(SecurityUtils.getAccessToken());
  }

  @Test
  void givenJwtWhenGetAccessTokenThenReturnToken(){
    // Given
    String jwt = "TOKENHEADER.TOKENPAYLOAD.TOKENDIGEST";
    SecurityContextHolder.setContext(new SecurityContextImpl(new JwtAuthenticationToken(Jwt
      .withTokenValue(jwt)
      .header("", "")
      .claim("", "")
      .build())));

    // When
    String result = SecurityUtils.getAccessToken();

    // Then
    Assertions.assertSame(jwt, result);
  }
//endregion

//region test getCurrentUserExternalId
  @Test
  void givenJwtWhenGetCurrentUserExternalIdThenReturnPrincipalName(){
    // Given
    String principalName = "PRINCIPALNAME";
    SecurityContextHolder.setContext(new SecurityContextImpl(new JwtAuthenticationToken(Mockito.mock(Jwt.class), null, principalName)));

    // When
    String result = SecurityUtils.getCurrentUserExternalId();

    // Then
    Assertions.assertSame(principalName, result);
  }

  @Test
  void givenPuSystemUserAndUserIdProvidedWhenGetCurrentUserExternalIdThenReturnUserId(){
    // Given
    String expectedUserId = "USERID";
    String principalName = SecurityUtils.SYSTEM_USERID_PREFIX + "ORGIPACODE";
    SecurityContextHolder.setContext(new SecurityContextImpl(new JwtAuthenticationToken(Mockito.mock(Jwt.class), null, principalName)));
    configureXUserIdHeader(expectedUserId);

    // When
    String result = SecurityUtils.getCurrentUserExternalId();

    // Then
    Assertions.assertSame(expectedUserId, result);
  }

  public static void configureXUserIdHeader(String expectedUserId) {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader(SecurityUtils.HEADER_USER_ID, expectedUserId);
    RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
  }

  @Test
  void givenPuSystemUserAndNotUserIdProvidedWhenGetCurrentUserExternalIdThenReturnUserId(){
    // Given
    String principalName = SecurityUtils.SYSTEM_USERID_PREFIX + "ORGIPACODE";
    SecurityContextHolder.setContext(new SecurityContextImpl(new JwtAuthenticationToken(Mockito.mock(Jwt.class), null, principalName)));
    RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest()));

    // When
    String result = SecurityUtils.getCurrentUserExternalId();

    // Then
    Assertions.assertSame(principalName, result);
  }

  @Test
  void givenPuSystemUserAndNotHttpContextWhenGetCurrentUserExternalIdThenReturnUserId(){
    // Given
    String principalName = SecurityUtils.SYSTEM_USERID_PREFIX + "ORGIPACODE";
    SecurityContextHolder.setContext(new SecurityContextImpl(new JwtAuthenticationToken(Mockito.mock(Jwt.class), null, principalName)));

    // When
    String result = SecurityUtils.getCurrentUserExternalId();

    // Then
    Assertions.assertSame(principalName, result);
  }
//endregion

  @Test
  void givenUriWhenRemovePiiFromURIThenOk(){
    String result = SecurityUtils.removePiiFromURI(URI.create("https://host/path?param1=PII&param2=noPII"));
    Assertions.assertEquals("https://host/path?param1=***&param2=***", result);
  }

  @Test
  void givenNullUriWhenRemovePiiFromURIThenOk(){
    Assertions.assertNull(SecurityUtils.removePiiFromURI(null));
  }
}
