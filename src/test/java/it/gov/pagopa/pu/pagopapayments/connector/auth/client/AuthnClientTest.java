package it.gov.pagopa.pu.pagopapayments.connector.auth.client;

import it.gov.pagopa.pu.auth.client.generated.AuthnApi;
import it.gov.pagopa.pu.auth.dto.generated.AccessToken;
import it.gov.pagopa.pu.pagopapayments.connector.auth.config.AuthApisHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthnClientTest {
    @Mock
    private AuthApisHolder authApisHolderMock;
    @Mock
    private AuthnApi authnApiMock;

    private AuthnClient authnClient;

    @BeforeEach
    void setUp() {
        authnClient = new AuthnClient(authApisHolderMock);
    }

    @AfterEach
    void verifyNoMoreInteractions(){
        Mockito.verifyNoMoreInteractions(
                authApisHolderMock
        );
    }

    @Test
    void whenGetOperatorInfoThenInvokeWithAccessToken(){
        // Given
        AccessToken expectedResult = new AccessToken();

        String clientId = "clientId";
        String grantType = "grantType";
        String scope = "scope";
        String subjectToken = "subjectToken";
        String subjectIssuer = "subjectIssuer";
        String subjectTokenType = "subjectTokenType";
        String clientSecret = "clientSecret";

        when(authApisHolderMock.getAuthnApi(null))
                .thenReturn(authnApiMock);
        when(authnApiMock.postToken(clientId, grantType, scope, subjectToken, subjectIssuer, subjectTokenType, clientSecret, null))
                .thenReturn(expectedResult);

        // When
        AccessToken result = authnClient.postToken(clientId, grantType, scope, subjectToken, subjectIssuer, subjectTokenType, clientSecret);

        // Then
        Assertions.assertSame(expectedResult, result);
    }
}
