package it.gov.pagopa.pu.pagopapayments.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.net.URI;
import java.security.Principal;
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

  public static String getCurrentUserExternalId(){
    return getAuthentication()
      .map(Principal::getName)
      .orElse(null);
  }

  private static Optional<Authentication> getAuthentication() {
    return Optional.ofNullable(SecurityContextHolder.getContext())
      .flatMap(c -> Optional.ofNullable(c.getAuthentication()));
  }

  public static String removePiiFromURI(URI uri){
    return uri != null
      ? uri.toString().replaceAll("=[^&]*", "=***")
      : null;
  }
}
