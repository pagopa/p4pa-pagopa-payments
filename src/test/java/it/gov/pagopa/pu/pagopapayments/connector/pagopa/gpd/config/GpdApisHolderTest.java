package it.gov.pagopa.pu.pagopapayments.connector.pagopa.gpd.config;

import it.gov.pagopa.nodo.gpd.dto.generated.PaymentPositionModelV3;
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
class GpdApisHolderTest extends BaseApiHolderTest {
  @Mock
  private RestTemplateBuilder restTemplateBuilderMock;

  private GpdApisHolder apisHolder;
  private GpdApiClientConfig apiClientConfig;

  private static final String ORG_FISCAL_CODE = "1234567890";
  private static final String API_KEY_HEADER = "Ocp-Apim-Subscription-Key";

  @BeforeEach
  void setUp() {
    when(restTemplateBuilderMock.build()).thenReturn(restTemplateMock);
    when(restTemplateMock.getUriTemplateHandler()).thenReturn(new DefaultUriBuilderFactory());

    apiClientConfig = GpdApiClientConfig.builder()
      .baseUrl("http://example.com")
      .maxAttempts(3)
      .build();
    apisHolder = new GpdApisHolder(apiClientConfig, restTemplateBuilderMock, new JsonConfig().objectMapperJackson3());

    verifyHttpClientErrorJsonBodyHandlerConfiguration(apisHolder.getDebtPositionsApiInstallmentsAndPaymentOptionsManagerApi("APIKEY"));
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
      apiKey ->
        apisHolder.getDebtPositionsApiInstallmentsAndPaymentOptionsManagerApi(apiKey)
          .createPosition(ORG_FISCAL_CODE, true, null, new PaymentPositionModelV3()),
      new ParameterizedTypeReference<>() {}
    );
  }

  @Test
  void whenGetOrganizationEntityControllerApiThenAuthenticationShouldBeSetInThreadSafeMode() throws InterruptedException {
    assertAuthenticationShouldBeSetInThreadSafeMode(
      apiKey ->
        apisHolder.getDebtPositionsApiInstallmentsAndPaymentOptionsManagerApi(apiKey)
          .createPosition(ORG_FISCAL_CODE, true, null, new PaymentPositionModelV3()),
      new ParameterizedTypeReference<>() {
      },
      () -> {
      },
      AUTH_TYPE.API_KEY,
      API_KEY_HEADER);
  }
}
