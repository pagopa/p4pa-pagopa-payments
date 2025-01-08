package it.gov.pagopa.pu.pagopapayments.util;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Optional;

public class SecurityUtils {
  private SecurityUtils() {
  }

  public static String getAccessToken() {
    return Optional.ofNullable(SecurityContextHolder.getContext())
      .flatMap(c -> Optional.ofNullable(c.getAuthentication()))
      .map(a -> ((Jwt) a.getCredentials()).getTokenValue())
      .orElse(null);
  }
}
