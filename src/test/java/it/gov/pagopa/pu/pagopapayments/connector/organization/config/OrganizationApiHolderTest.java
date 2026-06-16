package it.gov.pagopa.pu.pagopapayments.connector.organization.config;

import it.gov.pagopa.pu.organization.dto.generated.OrganizationApiKeyType;
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
class OrganizationApiHolderTest extends BaseApiHolderTest {
  @Mock
  private RestTemplateBuilder restTemplateBuilderMock;

  private OrganizationApisHolder organizationApisHolder;
  private OrganizationApiClientConfig apiClientConfig;

  @BeforeEach
  void setUp() {
    Mockito.when(restTemplateBuilderMock.build()).thenReturn(restTemplateMock);
    Mockito.when(restTemplateMock.getUriTemplateHandler()).thenReturn(new DefaultUriBuilderFactory());

    apiClientConfig = new OrganizationApiClientConfig();
    apiClientConfig.setBaseUrl("http://example.com");
    apiClientConfig.setMaxAttempts(3);

    organizationApisHolder = new OrganizationApisHolder(apiClientConfig, restTemplateBuilderMock);
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
      accessToken -> organizationApisHolder.getOrganizationEntityControllerApi(accessToken)
        .crudGetOrganization("ORGID"),
      new ParameterizedTypeReference<>() {},
      organizationApisHolder::unload);
  }

  @Test
  void whenGetOrganizationSearchControllerApiThenAuthenticationShouldBeSetInThreadSafeMode() throws InterruptedException {
    assertAuthenticationShouldBeSetInThreadSafeMode(
      accessToken -> organizationApisHolder.getOrganizationSearchControllerApi(accessToken)
        .crudOrganizationsFindByIpaCode("IPACODE"),
      new ParameterizedTypeReference<>() {},
      organizationApisHolder::unload);
  }

  @Test
  void whenGetAuthnApiThenAuthenticationShouldBeSetInThreadSafeMode() throws InterruptedException {
    assertAuthenticationShouldBeSetInThreadSafeMode(
      accessToken -> organizationApisHolder.getBrokerEntityControllerApi(accessToken)
        .crudGetBroker("BROKERID"),
      new ParameterizedTypeReference<>() {},
      organizationApisHolder::unload);
  }

  @Test
  void whenGetBrokerApiThenAuthenticationShouldBeSetInThreadSafeMode() throws InterruptedException {
    assertAuthenticationShouldBeSetInThreadSafeMode(
      accessToken -> organizationApisHolder.getBrokerApi(accessToken)
        .getBrokerApiKeys(1L),
      new ParameterizedTypeReference<>() {},
      organizationApisHolder::unload);
  }

  @Test
  void whenGetOrganizationApiThenAuthenticationShouldBeSetInThreadSafeMode() throws InterruptedException {
    assertAuthenticationShouldBeSetInThreadSafeMode(
      token -> organizationApisHolder.getOrganizationApi(token)
        .getOrganizationApiKey(1L, OrganizationApiKeyType.SEND, "CODE"),
      new ParameterizedTypeReference<>() {},
      organizationApisHolder::unload);
  }

  @Test
  void whenGetBrokerSearchControllerApiThenAuthenticationShouldBeSetInThreadSafeMode() throws InterruptedException {
    assertAuthenticationShouldBeSetInThreadSafeMode(
      accessToken -> organizationApisHolder.getBrokerSearchControllerApi(accessToken)
        .crudBrokersFindByBrokerFiscalCode("FISCALCODE"),
      new ParameterizedTypeReference<>() {},
      organizationApisHolder::unload);
  }

  @Test
  void whenGetStationSearchControllerApiThenAuthenticationShouldBeSetInThreadSafeMode() throws InterruptedException {
    assertAuthenticationShouldBeSetInThreadSafeMode(
      accessToken -> organizationApisHolder.getStationSearchControllerApi(accessToken)
        .crudStationsFindByBrokerIdAndStationId(1L, "STATIONID"),
      new ParameterizedTypeReference<>() {},
      organizationApisHolder::unload);
  }

  @Test
  void testRetryConfiguration() {
    assertRetry(apiClientConfig,
      accessToken -> organizationApisHolder.getStationSearchControllerApi(accessToken)
        .crudStationsFindByBrokerIdAndStationId(1L, "STATIONID"),
      new ParameterizedTypeReference<>() {
      }
    );
  }
}
