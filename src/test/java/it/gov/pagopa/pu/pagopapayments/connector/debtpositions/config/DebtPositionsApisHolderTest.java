package it.gov.pagopa.pu.pagopapayments.connector.debtpositions.config;

import it.gov.pagopa.pu.debtpositions.dto.generated.ActualizeAmountRequestDTO;
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
class DebtPositionsApisHolderTest extends BaseApiHolderTest {
  @Mock
  private RestTemplateBuilder restTemplateBuilderMock;

  private DebtPositionsApisHolder apisHolder;
  private DebtPositionsApiClientConfig apiClientConfig;

  @BeforeEach
  void setUp() {
    when(restTemplateBuilderMock.build()).thenReturn(restTemplateMock);
    when(restTemplateMock.getUriTemplateHandler()).thenReturn(new DefaultUriBuilderFactory());

    apiClientConfig = DebtPositionsApiClientConfig.builder()
      .baseUrl("http://example.com")
      .maxAttempts(3)
      .build();
    apisHolder = new DebtPositionsApisHolder(apiClientConfig, restTemplateBuilderMock, new JsonConfig().objectMapperJackson3());

    verifyHttpClientErrorJsonBodyHandlerConfiguration(apisHolder.getDebtPositionTypeOrgEntityControllerApi(null));
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
      accessToken -> apisHolder.getDebtPositionTypeOrgEntityControllerApi(accessToken)
        .crudGetDebtpositiontypeorg("debtPositionId"),
      new ParameterizedTypeReference<>() {}
    );
  }

  @Test
  void whenGetDebtPositionTypeOrgEntityControllerApiThenAuthenticationShouldBeSetInThreadSafeMode() throws InterruptedException {
    assertAuthenticationShouldBeSetInThreadSafeMode(
      accessToken -> apisHolder.getDebtPositionTypeOrgEntityControllerApi(accessToken)
        .crudGetDebtpositiontypeorg("debtPositionId"),
      new ParameterizedTypeReference<>() {},
      apisHolder::unload);
  }

  @Test
  void whenGetInstallmentApiThenAuthenticationShouldBeSetInThreadSafeMode() throws InterruptedException {
    assertAuthenticationShouldBeSetInThreadSafeMode(
      accessToken -> apisHolder.getInstallmentApi(accessToken)
        .getInstallmentsByOrganizationIdAndNav(1L, "nav", null),
      new ParameterizedTypeReference<>() {
      },
      apisHolder::unload);
  }

  @Test
  void whenGetDebtPositionApiThenAuthenticationShouldBeSetInThreadSafeMode() throws InterruptedException {
    ActualizeAmountRequestDTO request = ActualizeAmountRequestDTO.builder()
      .organizationId(1L)
      .nav("NAV")
      .newFeeCents(100L)
      .actualizedFromPuSil(false)
      .build();

    assertAuthenticationShouldBeSetInThreadSafeMode(
      accessToken -> apisHolder.getDebtPositionApi(accessToken)
        .updateInstallmentNotificationFee(request),
      new ParameterizedTypeReference<>() {
      },
      apisHolder::unload);
  }

  @Test
  void whenGetDebtPositionTypeOrgEntitySearchApiThenAuthenticationShouldBeSetInThreadSafeMode() throws InterruptedException {
    assertAuthenticationShouldBeSetInThreadSafeMode(
      accessToken -> apisHolder.getDebtPositionTypeOrgSearchControllerApi(accessToken)
        .crudDebtPositionTypeOrgsGetDebtPositionTypeOrgByInstallmentId(1L),
      new ParameterizedTypeReference<>() {
      },
      apisHolder::unload);
  }

  @Test
  void whenGetSpontaneousFormEntityControllerApiThenAuthenticationShouldBeSetInThreadSafeMode() throws InterruptedException {
    assertAuthenticationShouldBeSetInThreadSafeMode(
      accessToken -> apisHolder.getSpontaneousFormEntityControllerApi(accessToken)
        .crudGetSpontaneousform("1L"),
      new ParameterizedTypeReference<>() {
      },
      apisHolder::unload);
  }

}
