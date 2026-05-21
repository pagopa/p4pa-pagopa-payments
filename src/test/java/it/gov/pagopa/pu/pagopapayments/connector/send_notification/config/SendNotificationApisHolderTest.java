package it.gov.pagopa.pu.pagopapayments.connector.send_notification.config;

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
class SendNotificationApisHolderTest extends BaseApiHolderTest {

  @Mock
  private RestTemplateBuilder restTemplateBuilderMock;

  private SendNotificationApisHolder sendNotificationApisHolder;
  private SendNotificationApiClientConfig apiClientConfig;

  @BeforeEach
  void setUp() {
    Mockito.when(restTemplateBuilderMock.build()).thenReturn(restTemplateMock);
    Mockito.when(restTemplateMock.getUriTemplateHandler()).thenReturn(new DefaultUriBuilderFactory());

    apiClientConfig = SendNotificationApiClientConfig.builder()
      .baseUrl("http://example.com")
      .maxAttempts(3)
      .build();

    sendNotificationApisHolder = new SendNotificationApisHolder(apiClientConfig, restTemplateBuilderMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      restTemplateBuilderMock,
      restTemplateMock
    );
  }

  @Test
  void whenGetSendApiThenAuthenticationShouldBeSetInThreadSafeMode() throws InterruptedException {
    assertAuthenticationShouldBeSetInThreadSafeMode(
      accessToken -> sendNotificationApisHolder.getSendApi(accessToken)
        .retrieveNotificationPrice(1L, "NAV"),
      new ParameterizedTypeReference<>() {
      },
      sendNotificationApisHolder::unload
    );
  }

  @Test
  void testRetryConfiguration() {
    assertRetry(apiClientConfig,
      accessToken -> sendNotificationApisHolder.getSendApi(accessToken)
        .retrieveNotificationPrice(1L, "NAV"),
      new ParameterizedTypeReference<>() {
      }
    );
  }
}
