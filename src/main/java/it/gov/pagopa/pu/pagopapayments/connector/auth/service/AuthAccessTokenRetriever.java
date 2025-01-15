package it.gov.pagopa.pu.pagopapayments.connector.auth.service;

import it.gov.pagopa.pu.auth.dto.generated.AccessToken;
import it.gov.pagopa.pu.pagopapayments.connector.auth.client.AuthnClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Lazy
@Service
public class AuthAccessTokenRetriever {

    private static final String GRANT_TYPE = "client_credentials";
    private static final String SCOPE = "openid";
    private static final String CLIENT_ID_PREFIX = "piattaforma-unitaria_";

    private final AuthnClient authnClient;
    private final String clientSecret;

    private final AtomicReference<Pair<LocalDateTime, AccessToken>> accessTokenRef = new AtomicReference<>();

    public AuthAccessTokenRetriever(
            @Value("${rest.auth.post-token.client_secret}")
            String clientSecret,

            AuthnClient authnClient) {
        this.authnClient = authnClient;
        this.clientSecret = clientSecret;
    }

    public AccessToken getAccessToken() {
        return accessTokenRef.updateAndGet(this::checkAndReturn).getValue();
    }

    private Pair<LocalDateTime, AccessToken> checkAndReturn(Pair<LocalDateTime, AccessToken> tokenPair) {
        if (tokenPair == null || LocalDateTime.now().isAfter(tokenPair.getLeft())) {
            log.info("M2M AccessToken expired, refreshing");
            LocalDateTime tokenRequestDateTime = LocalDateTime.now();
            AccessToken accessToken = authnClient.postToken(CLIENT_ID_PREFIX, GRANT_TYPE, SCOPE, null, null, null, clientSecret);
            LocalDateTime expiration = tokenRequestDateTime.plusSeconds(accessToken.getExpiresIn() - 5L); // setting some seconds to avoid too strict expiration
            return Pair.of(expiration, accessToken
            );
        } else {
            return tokenPair;
        }
    }
}
