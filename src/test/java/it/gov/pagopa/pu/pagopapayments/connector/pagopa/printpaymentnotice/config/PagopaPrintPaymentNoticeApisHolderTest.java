package it.gov.pagopa.pu.pagopapayments.connector.pagopa.printpaymentnotice.config;

import it.gov.pagopa.pu.pagopapayments.config.json.JsonConfig;
import it.gov.pagopa.pu.pagopapayments.config.rest.HttpClientErrorJsonBodyHandler;
import it.gov.pagopa.pu.pagopapayments.connector.BaseApiHolderTest;
import it.gov.pagopa.pu.printpaymentnotice.connector.printpaymentnotice.generated.dto.NoticeGenerationRequestItemDTO;
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
class PagopaPrintPaymentNoticeApisHolderTest extends BaseApiHolderTest {
  @Mock
  private RestTemplateBuilder restTemplateBuilderMock;

  private PagopaPrintPaymentNoticeApisHolder apisHolder;
  private PagopaPrintPaymentNoticeApiClientConfig apiClientConfig;

  @BeforeEach
  void setUp() {
    Mockito.when(restTemplateBuilderMock.build()).thenReturn(restTemplateMock);
    Mockito.when(restTemplateMock.getUriTemplateHandler()).thenReturn(new DefaultUriBuilderFactory());

    apiClientConfig = new PagopaPrintPaymentNoticeApiClientConfig();
    apiClientConfig.setBaseUrl("http://example.com");
    apiClientConfig.setMaxAttempts(3);

    apisHolder = new PagopaPrintPaymentNoticeApisHolder(apiClientConfig, restTemplateBuilderMock, new JsonConfig().objectMapperJackson3());

    Mockito.verify(restTemplateMock)
      .setErrorHandler(Mockito.any(HttpClientErrorJsonBodyHandler.class));
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      restTemplateBuilderMock,
      restTemplateMock
    );
  }

  @Test
  void whenGetNoticeGenerationRequestApiThenAuthenticationShouldBeSetInThreadSafeMode() throws InterruptedException {
    assertAuthenticationShouldBeSetInThreadSafeMode(
      apiKey -> apisHolder.getNoticeGenerationRequestApi(apiKey)
        .generateNotice(new NoticeGenerationRequestItemDTO(), null, null),
      new ParameterizedTypeReference<>() {
      },
      () -> {
      },
      AUTH_TYPE.API_KEY,
      "Ocp-Apim-Subscription-Key"
    );
  }

  @Test
  void testRetryConfiguration() {
    assertRetry(apiClientConfig,
      apiKey -> apisHolder.getNoticeGenerationRequestApi(apiKey)
        .generateNotice(new NoticeGenerationRequestItemDTO(), null, null),
      new ParameterizedTypeReference<>() {
      }
    );
  }
}
