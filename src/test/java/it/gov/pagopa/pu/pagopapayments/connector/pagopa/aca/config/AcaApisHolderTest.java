package it.gov.pagopa.pu.pagopapayments.connector.pagopa.aca.config;

import it.gov.pagopa.pu.aca.gpd.v1.dto.generated.PaymentPositionModel;
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
class AcaApisHolderTest extends BaseApiHolderTest {

  @Mock
  private RestTemplateBuilder restTemplateBuilderMock;

  private AcaApisHolder acaApisHolder;
  private AcaApiClientConfig apiClientConfig;

  private static final String ORG_FISCAL_CODE = "12345678901";
  private static final String API_KEY_HEADER = "Ocp-Apim-Subscription-Key";

  @BeforeEach
  void setUp() {
    Mockito.when(restTemplateBuilderMock.build()).thenReturn(restTemplateMock);
    Mockito.when(restTemplateMock.getUriTemplateHandler()).thenReturn(new DefaultUriBuilderFactory());

    apiClientConfig = new AcaApiClientConfig();
    apiClientConfig.setMaxAttempts(3);
    apiClientConfig.setBaseUrl("http://example.com");

    acaApisHolder = new AcaApisHolder(apiClientConfig, restTemplateBuilderMock, new JsonConfig().objectMapperJackson3());

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
      apiKey -> acaApisHolder.getDebtPositionsApi(apiKey)
        .createPosition(ORG_FISCAL_CODE, new PaymentPositionModel(), null, true),
      new ParameterizedTypeReference<>() {},
      () -> {},
      AUTH_TYPE.API_KEY,
      API_KEY_HEADER
    );
  }

  @Test
  void testRetryConfiguration() {
    assertRetry(apiClientConfig,
      apiKey -> acaApisHolder.getDebtPositionsApi(apiKey)
        .createPosition(ORG_FISCAL_CODE, new PaymentPositionModel(), null, true),
      new ParameterizedTypeReference<>() {}
      );
  }
}

