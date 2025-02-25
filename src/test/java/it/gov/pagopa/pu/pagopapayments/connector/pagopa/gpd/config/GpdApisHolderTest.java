package it.gov.pagopa.pu.pagopapayments.connector.pagopa.gpd.config;

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
import org.springframework.web.util.DefaultUriBuilderFactory;

@ExtendWith(MockitoExtension.class)
class GpdApisHolderTest extends BaseApiHolderTest{
  @Mock
  private RestTemplateBuilder restTemplateBuilderMock;

  private GpdApisHolder gpdApisHolder;

  private static final String ORG_FISCAL_CODE = "1234567890";

  @BeforeEach
  void setUp() {
    Mockito.when(restTemplateBuilderMock.build()).thenReturn(restTemplateMock);
    Mockito.when(restTemplateMock.getUriTemplateHandler()).thenReturn(new DefaultUriBuilderFactory());
    GpdApiClientConfig apiClient = new GpdApiClientConfig();
    apiClient.setBaseUrl("http://example.com");
    gpdApisHolder = new GpdApisHolder(apiClient, restTemplateBuilderMock);
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
      apiKey -> gpdApisHolder.getGpdApiClientByApiKey(apiKey)
        .createPosition(ORG_FISCAL_CODE,new PaymentPositionModel(), null, true),
      Object.class,
      () -> {},
      BaseApiHolderTest.AUTH_TYPE.API_KEY,
      "Ocp-Apim-Subscription-Key");
  }

}
