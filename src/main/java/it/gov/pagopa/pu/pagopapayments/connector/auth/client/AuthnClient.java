package it.gov.pagopa.pu.pagopapayments.connector.auth.client;


import it.gov.pagopa.pu.auth.dto.generated.AccessToken;
import it.gov.pagopa.pu.pagopapayments.connector.auth.config.AuthApisHolder;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Lazy
@Service
public class AuthnClient {

    private final AuthApisHolder authApisHolder;

    public AuthnClient(AuthApisHolder authApisHolder) {
        this.authApisHolder = authApisHolder;
    }

    public AccessToken postToken(String clientId, String grantType, String scope, String subjectToken, String subjectIssuer, String subjectTokenType, String clientSecret) {
        return authApisHolder.getAuthnApi(null)
                .postToken(clientId, grantType, scope, subjectToken, subjectIssuer, subjectTokenType, clientSecret);
    }

}
