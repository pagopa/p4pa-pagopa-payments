package it.gov.pagopa.pu.pagopapayments.connector.pagopa.printpaymentnotice.config;

import it.gov.pagopa.nodo.printpaymentnotice.dto.generated.NoticeGenerationRequestItemDTO;
import it.gov.pagopa.pu.pagopapayments.config.json.JsonConfig;
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

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PagopaPrintPaymentNoticeApisHolderTest extends BaseApiHolderTest {
  @Mock
  private RestTemplateBuilder restTemplateBuilderMock;

  private PagopaPrintPaymentNoticeApisHolder apisHolder;
  private PagopaPrintPaymentNoticeApiClientConfig apiClientConfig;

  @BeforeEach
  void setUp() {
    when(restTemplateBuilderMock.build()).thenReturn(restTemplateMock);
    when(restTemplateMock.getUriTemplateHandler()).thenReturn(new DefaultUriBuilderFactory());

    apiClientConfig = PagopaPrintPaymentNoticeApiClientConfig.builder()
      .baseUrl("http://example.com")
      .maxAttempts(3)
      .build();
    apisHolder = new PagopaPrintPaymentNoticeApisHolder(apiClientConfig, restTemplateBuilderMock, new JsonConfig().objectMapperJackson3());

    verifyHttpClientErrorJsonBodyHandlerConfiguration(apisHolder.getNoticeGenerationRequestApi("apiKey"));
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      restTemplateBuilderMock,
      restTemplateMock
    );
  }

  @Test
  void testRetryConfiguration() {
    assertRetry(apiClientConfig,
      apiKey -> apisHolder.getNoticeGenerationRequestApi(apiKey)
        .generateNotice(new NoticeGenerationRequestItemDTO(), null, null),
      new ParameterizedTypeReference<>() {}
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
}
