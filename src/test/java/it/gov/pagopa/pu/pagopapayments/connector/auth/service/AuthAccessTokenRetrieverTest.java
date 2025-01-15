package it.gov.pagopa.pu.pagopapayments.connector.auth.service;

import it.gov.pagopa.pu.auth.dto.generated.AccessToken;
import it.gov.pagopa.pu.pagopapayments.connector.auth.client.AuthnClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthAccessTokenRetrieverTest {

    private static final String CLIENTSECRET = "clientsecret";

    @Mock
    private AuthnClient authnClientMock;

    private AuthAccessTokenRetriever accessTokenRetriever;

    @BeforeEach
    void init(){
        accessTokenRetriever = new AuthAccessTokenRetriever(CLIENTSECRET, authnClientMock);
    }

    @AfterEach
    void verifyNoMoreInteractions(){
        Mockito.verifyNoMoreInteractions(
                authnClientMock
        );
    }

    @Test
    void givenEmptyCacheWhenGetAccessTokenThenInvokeAndCache(){
        // Given
        AccessToken expectedResult = AccessToken.builder()
                .expiresIn(10)
                .accessToken("ACCESSTOKEN")
                .tokenType("TOKENTYPE")
                .build();

        // When
        configureAndInvoke(expectedResult);

        // Then
        Mockito.verify(authnClientMock, Mockito.times(1))
                .postToken(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    void givenExpiredCacheWhenGetAccessTokenThenInvokeAndCache(){
        // Given
        AccessToken expectedResult = AccessToken.builder()
                .expiresIn(5)
                .accessToken("ACCESSTOKEN")
                .tokenType("TOKENTYPE")
                .build();

        // When
        configureAndInvoke(expectedResult);

        // Then
        Mockito.verify(authnClientMock, Mockito.times(2))
                .postToken(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    }

    private void configureAndInvoke(AccessToken expectedResult) {
        // Given
        Mockito.when(authnClientMock.postToken("piattaforma-unitaria_", "client_credentials", "openid", null, null, null, CLIENTSECRET))
                .thenReturn(expectedResult);

        // When
        AccessToken result1 = accessTokenRetriever.getAccessToken();
        AccessToken result2 = accessTokenRetriever.getAccessToken();

        // Then
        Assertions.assertSame(expectedResult, result1);
        Assertions.assertSame(expectedResult, result2);
    }
}
