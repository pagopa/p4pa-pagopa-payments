package it.gov.pagopa.pu.pagopapayments.connector.debtpositions.config;

import it.gov.pagopa.pu.debtpositions.dto.generated.UpdateInstallmentNotificationFeeRequest;
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
class DebtPositionsApisHolderTest extends BaseApiHolderTest {
    @Mock
    private RestTemplateBuilder restTemplateBuilderMock;

    private DebtPositionsApisHolder apisHolder;

    @BeforeEach
    void setUp() {
        Mockito.when(restTemplateBuilderMock.build()).thenReturn(restTemplateMock);
        Mockito.when(restTemplateMock.getUriTemplateHandler()).thenReturn(new DefaultUriBuilderFactory());
        DebtPositionsApiClientConfig clientConfig = DebtPositionsApiClientConfig.builder()
          .baseUrl("http://example.com")
          .build();
        apisHolder = new DebtPositionsApisHolder(clientConfig, restTemplateBuilderMock);
    }

    @AfterEach
    void verifyNoMoreInteractions() {
        Mockito.verifyNoMoreInteractions(
                restTemplateBuilderMock,
                restTemplateMock
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
        new ParameterizedTypeReference<>() {},
        apisHolder::unload);
    }

  @Test
  void whenGetDebtPositionApiThenAuthenticationShouldBeSetInThreadSafeMode() throws InterruptedException {
    assertAuthenticationShouldBeSetInThreadSafeMode(
      accessToken -> apisHolder.getDebtPositionApi(accessToken)
        .updateInstallmentNotificationFee(new UpdateInstallmentNotificationFeeRequest(1L, "NAV", 100L)),
      new ParameterizedTypeReference<>() {},
      apisHolder::unload);
  }

  @Test
  void whenGetDebtPositionTypeOrgEntitySearchApiThenAuthenticationShouldBeSetInThreadSafeMode() throws InterruptedException {
    assertAuthenticationShouldBeSetInThreadSafeMode(
      accessToken -> apisHolder.getDebtPositionTypeOrgSearchControllerApi(accessToken)
        .crudDebtPositionTypeOrgsGetDebtPositionTypeOrgByInstallmentId(1L),
      new ParameterizedTypeReference<>() {},
      apisHolder::unload);
  }

}
