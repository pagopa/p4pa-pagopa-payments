package it.gov.pagopa.pu.pagopapayments.util;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.net.URI;
import java.security.Principal;
import java.util.Optional;

public class SecurityUtils {
  private SecurityUtils() {
  }

  public static final String SYSTEM_USERID_PREFIX = "WS_USER-piattaforma-unitaria_";
  public static final String HEADER_USER_ID = "X-user-id";

  public static String getAccessToken() {
    return Optional.ofNullable(SecurityContextHolder.getContext())
      .flatMap(c -> Optional.ofNullable(c.getAuthentication()))
      .map(a -> ((Jwt) a.getCredentials()).getTokenValue())
      .orElse(null);
  }

  public static String getCurrentUserExternalId(){
    return resolvePuSystemUser(getAuthentication()
      .map(Principal::getName)
      .orElse(null));
  }

  private static String resolvePuSystemUser(String mappedExternalUserId) {
    if(mappedExternalUserId != null && mappedExternalUserId.startsWith(SYSTEM_USERID_PREFIX) && RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes servletRequestAttributes){
      HttpServletRequest requestAttributes = servletRequestAttributes.getRequest();
      mappedExternalUserId = ObjectUtils.firstNonNull(requestAttributes.getHeader(HEADER_USER_ID), mappedExternalUserId);
    }
    return mappedExternalUserId;
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
