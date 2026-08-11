package it.gov.pagopa.pu.pagopapayments.connector.pagopa.paymentsreporting.config;

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

import java.time.OffsetDateTime;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentsReportingApisHolderTest extends BaseApiHolderTest {

  @Mock
  private RestTemplateBuilder restTemplateBuilderMock;

  private PaymentsReportingApisHolder apisHolder;
  private PaymentsReportingApiClientConfig apiClientConfig;

  private static final String ORG_FISCAL_CODE = "1234567890";
  private static final String API_KEY_HEADER = "Ocp-Apim-Subscription-Key";

  @BeforeEach
  void setUp() {
    when(restTemplateBuilderMock.build()).thenReturn(restTemplateMock);
    when(restTemplateMock.getUriTemplateHandler()).thenReturn(new DefaultUriBuilderFactory());

    apiClientConfig = PaymentsReportingApiClientConfig.builder()
      .baseUrl("http://example.com")
      .maxAttempts(3)
      .build();
    apisHolder = new PaymentsReportingApisHolder(restTemplateBuilderMock, apiClientConfig, new JsonConfig().objectMapperJackson3());

    verifyHttpClientErrorJsonBodyHandlerConfiguration(apisHolder.getOrganizationApi("apiKey"));
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
      apiKey -> apisHolder.getOrganizationApi(apiKey)
        .iOrganizationsControllerGetAllPublishedFlows(ORG_FISCAL_CODE,  OffsetDateTime.now(), 1L, null, null, null),
      new ParameterizedTypeReference<>() {}
    );
  }

  @Test
  void whenGetOrganizationApiThenAuthenticationShouldBeSetInThreadSafeMode() throws InterruptedException {
    assertAuthenticationShouldBeSetInThreadSafeMode(
      apiKey -> apisHolder.getOrganizationApi(apiKey)
        .iOrganizationsControllerGetAllPublishedFlows(ORG_FISCAL_CODE,  OffsetDateTime.now(), 1L, null, null, null),
      new ParameterizedTypeReference<>() {},
      () -> {},
      BaseApiHolderTest.AUTH_TYPE.API_KEY,
      API_KEY_HEADER);
  }

}
