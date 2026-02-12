package it.gov.pagopa.pu.pagopapayments.service;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.debtpositions.dto.generated.PaymentOptionDTO;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.orgfornode.dto.generated.PaymentOptionsResponse;
import it.gov.pagopa.pu.pagopapayments.connector.auth.AuthnService;
import it.gov.pagopa.pu.pagopapayments.connector.debtpositions.DebtPositionService;
import it.gov.pagopa.pu.pagopapayments.connector.organization.OrganizationService;
import it.gov.pagopa.pu.pagopapayments.exception.ConflictException;
import it.gov.pagopa.pu.pagopapayments.mapper.DebtPositions2PaymentOptionsNodeResponseMapper;
import it.gov.pagopa.pu.pagopapayments.service.paymentoptionsforpsp.PaymentOptionsForNodeService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static it.gov.pagopa.pu.pagopapayments.util.DebtPositionUtils.ORDINARY_DEBT_POSITION_ORIGINS;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentOptionsForNodeServiceTest {

  @Mock
  private AuthnService authnServiceMock;

  @Mock
  private OrganizationService organizationServiceMock;

  @Mock
  private DebtPositionService debtPositionServiceMock;

  @Mock
  private DebtPositions2PaymentOptionsNodeResponseMapper mapperMock;

  @InjectMocks
  private PaymentOptionsForNodeService service;

  @AfterEach
  void verifyNoMore() {
    verifyNoMoreInteractions(authnServiceMock, organizationServiceMock, debtPositionServiceMock, mapperMock);
  }

  @Test
  void givenValidInputsWhenGetPaymentOptionsThenOk() {
    // given
    String noticeNumber = "NAV123";
    String organizationFiscalCode = "ORG_FISCAL_CODE";
    String accessToken = "ACCESS_TOKEN";
    Long organizationId = 1L;

    Organization organization = new Organization();
    organization.setOrganizationId(organizationId);
    organization.setOrgFiscalCode(organizationFiscalCode);

    List<DebtPositionDTO> debtPositions = List.of(new DebtPositionDTO());

    PaymentOptionsResponse expectedResponse = new PaymentOptionsResponse();

    when(authnServiceMock.getAccessToken())
      .thenReturn(accessToken);
    when(organizationServiceMock.getOrganizationByFiscalCode(organizationFiscalCode, accessToken))
      .thenReturn(organization);
    when(debtPositionServiceMock.getDebtPositionsByOrganizationIdAndNav(organizationId, noticeNumber, ORDINARY_DEBT_POSITION_ORIGINS, accessToken))
      .thenReturn(debtPositions);
    when(mapperMock.mapToResponse(debtPositions, organization))
      .thenReturn(expectedResponse);

    // when
    PaymentOptionsResponse response = service.getPaymentOptions(noticeNumber, organizationFiscalCode);

    // then
    Assertions.assertNotNull(response);
    Assertions.assertSame(expectedResponse, response);
  }

  @Test
  void givenOrganizationNullWhenGetPaymentOptionsThenReturnNull() {
    // given
    String noticeNumber = "NAV123";
    String organizationFiscalCode = "ORG_FISCAL_CODE";
    String accessToken = "ACCESS_TOKEN";

    when(authnServiceMock.getAccessToken()).thenReturn(accessToken);
    when(organizationServiceMock.getOrganizationByFiscalCode(organizationFiscalCode, accessToken))
      .thenReturn(null);

    // when
    PaymentOptionsResponse response = service.getPaymentOptions(noticeNumber, organizationFiscalCode);

    // then
    Assertions.assertNull(response);
  }

  @Test
  void givenDebtPositionsNullWhenGetPaymentOptionsThenReturnNull() {
    // given
    String noticeNumber = "NAV123";
    String organizationFiscalCode = "ORG_FISCAL_CODE";
    String accessToken = "ACCESS_TOKEN";
    Long organizationId = 1L;

    Organization organization = new Organization();
    organization.setOrganizationId(organizationId);
    organization.setOrgFiscalCode(organizationFiscalCode);

    when(authnServiceMock.getAccessToken()).thenReturn(accessToken);
    when(organizationServiceMock.getOrganizationByFiscalCode(organizationFiscalCode, accessToken))
      .thenReturn(organization);
    when(debtPositionServiceMock.getDebtPositionsByOrganizationIdAndNav(
      organizationId, noticeNumber, ORDINARY_DEBT_POSITION_ORIGINS, accessToken
    )).thenReturn(null);

    // when
    PaymentOptionsResponse response = service.getPaymentOptions(noticeNumber, organizationFiscalCode);

    // then
    Assertions.assertNull(response);
  }

  @Test
  void givenDebtPositionsEmptyWhenGetPaymentOptionsThenReturnNull() {
    // given
    String noticeNumber = "NAV123";
    String organizationFiscalCode = "ORG_FISCAL_CODE";
    String accessToken = "ACCESS_TOKEN";
    Long organizationId = 1L;

    Organization organization = new Organization();
    organization.setOrganizationId(organizationId);
    organization.setOrgFiscalCode(organizationFiscalCode);

    when(authnServiceMock.getAccessToken()).thenReturn(accessToken);
    when(organizationServiceMock.getOrganizationByFiscalCode(organizationFiscalCode, accessToken))
      .thenReturn(organization);
    when(debtPositionServiceMock.getDebtPositionsByOrganizationIdAndNav(
      organizationId, noticeNumber, ORDINARY_DEBT_POSITION_ORIGINS, accessToken
    )).thenReturn(List.of());

    // when
    PaymentOptionsResponse response = service.getPaymentOptions(noticeNumber, organizationFiscalCode);

    // then
    Assertions.assertNull(response);
  }

  @Test
  void givenPaidInstallmentWhenGetPaymentOptionsThenThrowConflict() {
    // given
    String noticeNumber = "NAV123";
    String organizationFiscalCode = "ORG_FISCAL_CODE";
    String accessToken = "ACCESS_TOKEN";
    Long organizationId = 1L;

    Organization organization = new Organization();
    organization.setOrganizationId(organizationId);
    organization.setOrgFiscalCode(organizationFiscalCode);

    InstallmentDTO installment = new InstallmentDTO();
    installment.setStatus(InstallmentStatus.PAID);

    PaymentOptionDTO paymentOption = new PaymentOptionDTO();
    paymentOption.setInstallments(List.of(installment));

    DebtPositionDTO dp = new DebtPositionDTO();
    dp.setPaymentOptions(List.of(paymentOption));

    List<DebtPositionDTO> debtPositions = List.of(dp);

    when(authnServiceMock.getAccessToken()).thenReturn(accessToken);
    when(organizationServiceMock.getOrganizationByFiscalCode(organizationFiscalCode, accessToken))
      .thenReturn(organization);
    when(debtPositionServiceMock.getDebtPositionsByOrganizationIdAndNav(
      organizationId, noticeNumber, ORDINARY_DEBT_POSITION_ORIGINS, accessToken
    )).thenReturn(debtPositions);

    // when + then
    Assertions.assertThrows(ConflictException.class,
      () -> service.getPaymentOptions(noticeNumber, organizationFiscalCode));
  }
}
