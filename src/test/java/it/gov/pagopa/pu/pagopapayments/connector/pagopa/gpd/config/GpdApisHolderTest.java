package it.gov.pagopa.pu.pagopapayments.connector.pagopa.gpd.config;

import it.gov.pagopa.nodo.gpd.dto.generated.PaymentPositionModelV3;
import it.gov.pagopa.pu.pagopapayments.config.json.JsonConfig;
import it.gov.pagopa.pu.pagopapayments.config.rest.HttpClientErrorJsonBodyHandler;
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
class GpdApisHolderTest extends BaseApiHolderTest {
  @Mock
  private RestTemplateBuilder restTemplateBuilderMock;

  private GpdApisHolder gpdApisHolder;
  private GpdApiClientConfig apiClientConfig;

  private static final String ORG_FISCAL_CODE = "1234567890";
  private static final String API_KEY_HEADER = "Ocp-Apim-Subscription-Key";

  @BeforeEach
  void setUp() {
    Mockito.when(restTemplateBuilderMock.build()).thenReturn(restTemplateMock);
    Mockito.when(restTemplateMock.getUriTemplateHandler()).thenReturn(new DefaultUriBuilderFactory());

    apiClientConfig = new GpdApiClientConfig();
    apiClientConfig.setBaseUrl("http://example.com");
    apiClientConfig.setMaxAttempts(3);

    gpdApisHolder = new GpdApisHolder(apiClientConfig, restTemplateBuilderMock, new JsonConfig().objectMapperJackson3());

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
  void whenGetOrganizationEntityControllerApiThenAuthenticationShouldBeSetInThreadSafeMode() throws InterruptedException {
    assertAuthenticationShouldBeSetInThreadSafeMode(
      apiKey ->
        gpdApisHolder.getDebtPositionsApiInstallmentsAndPaymentOptionsManagerApi(apiKey)
          .createPosition(ORG_FISCAL_CODE, true, null, new PaymentPositionModelV3()),
      new ParameterizedTypeReference<>() {
      },
      () -> {
      },
      AUTH_TYPE.API_KEY,
      API_KEY_HEADER);
  }

  @Test
  void testRetryConfiguration() {
    assertRetry(apiClientConfig,
      apiKey ->
        gpdApisHolder.getDebtPositionsApiInstallmentsAndPaymentOptionsManagerApi(apiKey)
          .createPosition(ORG_FISCAL_CODE, true, null, new PaymentPositionModelV3()),
      new ParameterizedTypeReference<>() {
      }
    );
  }
}
