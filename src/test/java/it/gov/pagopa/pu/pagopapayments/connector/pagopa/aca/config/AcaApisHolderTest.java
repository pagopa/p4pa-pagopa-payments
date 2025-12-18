package it.gov.pagopa.pu.pagopapayments.connector.pagopa.aca.config;

import it.gov.pagopa.nodo.gpd.dto.generated.PaymentPositionModel;
import it.gov.pagopa.pu.pagopapayments.connector.BaseApiHolderTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.util.DefaultUriBuilderFactory;

@ExtendWith(MockitoExtension.class)
class AcaApisHolderTest extends BaseApiHolderTest {
  @Mock
  private RestTemplateBuilder restTemplateBuilderMock;

  private AcaApisHolder acaApisHolder;

  @BeforeEach
  void setUp() {
    Mockito.when(restTemplateBuilderMock.build()).thenReturn(restTemplateMock);
    Mockito.when(restTemplateMock.getUriTemplateHandler()).thenReturn(new DefaultUriBuilderFactory());
    AcaApiClientConfig apiClient = new AcaApiClientConfig();
    apiClient.setBaseUrl("http://example.com");
    acaApisHolder = new AcaApisHolder(apiClient, restTemplateBuilderMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      restTemplateBuilderMock,
      restTemplateMock
    );
  }

  @Test
  void whenGetOrganizationEntityControllerApiThenAuthenticationShouldBeSetInThreadSafeMode() throws InterruptedException {
    assertAuthenticationShouldBeSetInThreadSafeMode(
      apiKey -> acaApisHolder.getApiClientByApiKey(apiKey)
        .createPosition("12345678901", new PaymentPositionModel(), null, true),
      new ParameterizedTypeReference<>() {},
      () -> {},
      AUTH_TYPE.API_KEY,
      "Ocp-Apim-Subscription-Key");
  }
}
