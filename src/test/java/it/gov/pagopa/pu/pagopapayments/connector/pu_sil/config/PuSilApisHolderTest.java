package it.gov.pagopa.pu.pagopapayments.connector.pu_sil.config;

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
class PuSilApisHolderTest extends BaseApiHolderTest {

  @Mock
  private RestTemplateBuilder restTemplateBuilderMock;

  private PuSilApisHolder puSilApisHolder;
  private PuSilApiClientConfig apiClientConfig;

  @BeforeEach
  void setUp() {
    Mockito.when(restTemplateBuilderMock.build()).thenReturn(restTemplateMock);
    Mockito.when(restTemplateMock.getUriTemplateHandler()).thenReturn(new DefaultUriBuilderFactory());

    apiClientConfig = PuSilApiClientConfig.builder()
      .baseUrl("http://example.com")
      .maxAttempts(3)
      .build();

    puSilApisHolder = new PuSilApisHolder(apiClientConfig, restTemplateBuilderMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      restTemplateBuilderMock,
      restTemplateMock
    );
  }

  @Test
  void whenGetActualizationApiThenAuthenticationShouldBeSetInThreadSafeMode() throws InterruptedException {
    assertAuthenticationShouldBeSetInThreadSafeMode(
      accessToken -> puSilApisHolder.getActualizationApi(accessToken)
        .actualize(1L, "NAV"),
      new ParameterizedTypeReference<>() {
      },
      puSilApisHolder::unload
    );
  }

  @Test
  void testRetryConfiguration() {
    assertRetry(apiClientConfig,
      accessToken -> puSilApisHolder.getActualizationApi(accessToken)
        .actualize(1L, "NAV"),
      new ParameterizedTypeReference<>() {
      }
    );
  }
}
