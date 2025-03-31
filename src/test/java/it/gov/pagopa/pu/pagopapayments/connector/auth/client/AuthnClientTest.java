package it.gov.pagopa.pu.pagopapayments.connector.auth.client;

import it.gov.pagopa.pu.auth.controller.generated.AuthnApi;
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

        Mockito.when(authApisHolderMock.getAuthnApi(null))
                .thenReturn(authnApiMock);
        Mockito.when(authnApiMock.postToken(clientId, grantType, scope, subjectToken, subjectIssuer, subjectTokenType, clientSecret))
                .thenReturn(expectedResult);

        // When
        AccessToken result = authnClient.postToken(clientId, grantType, scope, subjectToken, subjectIssuer, subjectTokenType, clientSecret);

        // Then
        Assertions.assertSame(expectedResult, result);
    }
}
