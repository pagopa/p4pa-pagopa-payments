package it.gov.pagopa.pu.pagopapayments.connector.pagopa.paymentsreporting.config;

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

@ExtendWith(MockitoExtension.class)
class PaymentsReportingApisHolderTest extends BaseApiHolderTest {

  @Mock
  private RestTemplateBuilder restTemplateBuilderMock;

  private PaymentsReportingApisHolder paymentsReportingApisHolder;


  private static final String ORG_FISCAL_CODE = "1234567890";
  private static final String API_KEY_HEADER = "Ocp-Apim-Subscription-Key";

  @BeforeEach
  void setUp() {
    Mockito.when(restTemplateBuilderMock.build()).thenReturn(restTemplateMock);
    Mockito.when(restTemplateMock.getUriTemplateHandler()).thenReturn(new DefaultUriBuilderFactory());
    PaymentsReportingApiClientConfig clientConfig = PaymentsReportingApiClientConfig.builder()
      .baseUrl("http://example.com")
      .build();
    paymentsReportingApisHolder = new PaymentsReportingApisHolder(restTemplateBuilderMock, clientConfig);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      restTemplateBuilderMock,
      restTemplateMock
    );
  }

  @Test
  void whenGetOrganizationApiByApiKeyThenAuthenticationShouldBeSetInThreadSafeMode() throws InterruptedException {
    assertAuthenticationShouldBeSetInThreadSafeMode(
      apiKey -> {
        var api = paymentsReportingApisHolder.getOrganizationApiByApiKey(apiKey);
        api.getApiClient().addDefaultHeader(API_KEY_HEADER, apiKey);

        return api.iOrganizationsControllerGetAllPublishedFlows(ORG_FISCAL_CODE,  OffsetDateTime.now(), 1L, null, null, null);
      },
      new ParameterizedTypeReference<>() {
      },
      () -> {
      },
      BaseApiHolderTest.AUTH_TYPE.API_KEY,
      API_KEY_HEADER);
  }

}
