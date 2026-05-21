package it.gov.pagopa.pu.pagopapayments.connector.auth.config;

import it.gov.pagopa.pu.pagopapayments.connector.BaseApiHolderTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.util.DefaultUriBuilderFactory;

@ExtendWith(MockitoExtension.class)
class AuthApisHolderTest extends BaseApiHolderTest {
  @Mock
  private RestTemplateBuilder restTemplateBuilderMock;

  private AuthApisHolder authApisHolder;
  private AuthApiClientConfig apiClientConfig;

  @BeforeEach
  void setUp() {
    Mockito.when(restTemplateBuilderMock.build()).thenReturn(restTemplateMock);
    Mockito.when(restTemplateMock.getUriTemplateHandler()).thenReturn(new DefaultUriBuilderFactory());
    apiClientConfig = AuthApiClientConfig.builder()
      .baseUrl("http://example.com")
      .maxAttempts(3)
      .build();
    authApisHolder = new AuthApisHolder(apiClientConfig, restTemplateBuilderMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      restTemplateBuilderMock,
      restTemplateMock
    );
  }

  @Test
  void whenGetAuthzApiThenAuthenticationShouldBeSetInThreadSafeMode() throws InterruptedException {
    assertAuthenticationShouldBeSetInThreadSafeMode(
      accessToken -> authApisHolder.getAuthzApi(accessToken)
        .getUserInfoFromMappedExternaUserId("externalUserId"),
      new ParameterizedTypeReference<>() {
      },
      authApisHolder::unload);
  }

  @Test
  void whenGetAuthnApiThenAuthenticationShouldBeSetInThreadSafeMode() throws InterruptedException {
    assertAuthenticationShouldBeSetInThreadSafeMode(
      accessToken -> authApisHolder.getAuthnApi(accessToken)
        .getUserInfo(),
      new ParameterizedTypeReference<>() {
      },
      authApisHolder::unload);
  }

  @Test
  void testRetryConfiguration() {
    assertRetry(apiClientConfig,
      apiKey -> authApisHolder.getAuthnApi(apiKey)
        .getUserInfo(),
      new ParameterizedTypeReference<>() {
      }
    );
  }

}
