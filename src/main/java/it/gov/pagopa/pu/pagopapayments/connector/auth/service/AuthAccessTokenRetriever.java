package it.gov.pagopa.pu.pagopapayments.connector.auth.service;

import it.gov.pagopa.pu.auth.dto.generated.AccessToken;
import it.gov.pagopa.pu.pagopapayments.connector.auth.client.AuthnClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Lazy
@Service
public class AuthAccessTokenRetriever {

  private static final String GRANT_TYPE = "client_credentials";
  private static final String SCOPE = "openid";
  private static final String CLIENT_ID_PREFIX = "piattaforma-unitaria_";

  private final AuthnClient authnClient;
  private final String clientSecret;

  private final Map<String, Pair<LocalDateTime, AccessToken>> clientId2accessTokensMap = new ConcurrentHashMap<>();

  public AuthAccessTokenRetriever(
    @Value("${rest.auth.post-token.client_secret}")
    String clientSecret,

    AuthnClient authnClient) {
    this.authnClient = authnClient;
    this.clientSecret = clientSecret;
  }

  public AccessToken getAccessToken(String orgIpaCode) {
    String clientId = CLIENT_ID_PREFIX + StringUtils.stripToEmpty(orgIpaCode);
    return clientId2accessTokensMap.compute(clientId, (k, v) -> {
      if (v == null || LocalDateTime.now().isAfter(v.getLeft())) {
        log.info("M2M AccessToken with clientId[{}] expired, refreshing", clientId);
        LocalDateTime tokenRequestDateTime = LocalDateTime.now();
        AccessToken accessToken = authnClient.postToken(clientId, GRANT_TYPE, SCOPE, null, null, null, clientSecret);
        LocalDateTime expiration = tokenRequestDateTime.plusSeconds(accessToken.getExpiresIn() - 5L); // setting some seconds to avoid too strict expiration
        return Pair.of(expiration, accessToken);
      } else {
        return v;
      }
    }).getRight();
  }

}
