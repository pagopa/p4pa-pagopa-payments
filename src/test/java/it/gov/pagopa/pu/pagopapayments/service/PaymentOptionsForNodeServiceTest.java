package it.gov.pagopa.pu.pagopapayments.service;

import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.orgfornode.dto.generated.PaymentOptionsResponseForNode;
import it.gov.pagopa.pu.pagopapayments.connector.auth.AuthnService;
import it.gov.pagopa.pu.pagopapayments.connector.debtpositions.DebtPositionService;
import it.gov.pagopa.pu.pagopapayments.connector.organization.OrganizationService;
import it.gov.pagopa.pu.pagopapayments.exception.ConflictException;
import it.gov.pagopa.pu.pagopapayments.exception.NotFoundException;
import it.gov.pagopa.pu.pagopapayments.mapper.DebtPositions2PaymentOptionsNodeResponseMapper;
import it.gov.pagopa.pu.pagopapayments.service.orgfornode.PaymentOptionsForNodeService;
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
    String noticeNumber = "NAV123";
    String organizationFiscalCode = "ORG_FISCAL_CODE";
    String accessToken = "ACCESS_TOKEN";
    Long organizationId = 1L;

    Organization organization = new Organization();
    organization.setOrganizationId(organizationId);
    organization.setOrgFiscalCode(organizationFiscalCode);

    InstallmentDTO inst = new InstallmentDTO();
    inst.setNav(noticeNumber);
    inst.setStatus(InstallmentStatus.UNPAID);

    PaymentOptionDTO po = new PaymentOptionDTO();
    po.setStatus(PaymentOptionStatus.UNPAID);
    po.setInstallments(List.of(inst));

    DebtPositionDTO dp = new DebtPositionDTO();
    dp.setPaymentOptions(List.of(po));

    List<DebtPositionDTO> debtPositions = List.of(dp);

    PaymentOptionsResponseForNode expectedResponse = new PaymentOptionsResponseForNode();

    when(authnServiceMock.getAccessToken()).thenReturn(accessToken);
    when(organizationServiceMock.getOrganizationByFiscalCode(organizationFiscalCode, accessToken))
      .thenReturn(organization);
    when(debtPositionServiceMock.getDebtPositionsByOrganizationIdAndNav(
      organizationId, noticeNumber, ORDINARY_DEBT_POSITION_ORIGINS, accessToken
    )).thenReturn(debtPositions);
    when(mapperMock.mapToResponse(dp, organization)).thenReturn(expectedResponse);

    PaymentOptionsResponseForNode response = service.getPaymentOptions(noticeNumber, organizationFiscalCode);

    Assertions.assertSame(expectedResponse, response);
  }

  @Test
  void givenOrganizationNullWhenGetPaymentOptionsThenThrowNotFound() {
    String noticeNumber = "NAV123";
    String organizationFiscalCode = "ORG_FISCAL_CODE";
    String accessToken = "ACCESS_TOKEN";

    when(authnServiceMock.getAccessToken()).thenReturn(accessToken);
    when(organizationServiceMock.getOrganizationByFiscalCode(organizationFiscalCode, accessToken))
      .thenReturn(null);

    Assertions.assertThrows(NotFoundException.class,
      () -> service.getPaymentOptions(noticeNumber, organizationFiscalCode));
  }

  @Test
  void givenDebtPositionsNullWhenGetPaymentOptionsThenReturnNull() {
    String noticeNumber = "NAV123";
    String organizationFiscalCode = "ORG_FISCAL_CODE";
    String accessToken = "ACCESS_TOKEN";
    Long organizationId = 1L;

    Organization organization = new Organization();
    organization.setOrganizationId(organizationId);

    when(authnServiceMock.getAccessToken()).thenReturn(accessToken);
    when(organizationServiceMock.getOrganizationByFiscalCode(organizationFiscalCode, accessToken))
      .thenReturn(organization);
    when(debtPositionServiceMock.getDebtPositionsByOrganizationIdAndNav(
      organizationId, noticeNumber, ORDINARY_DEBT_POSITION_ORIGINS, accessToken
    )).thenReturn(null);

    PaymentOptionsResponseForNode response = service.getPaymentOptions(noticeNumber, organizationFiscalCode);
    Assertions.assertNull(response);
  }

  @Test
  void givenDebtPositionsEmptyWhenGetPaymentOptionsThenReturnNull() {
    String noticeNumber = "NAV123";
    String organizationFiscalCode = "ORG_FISCAL_CODE";
    String accessToken = "ACCESS_TOKEN";
    Long organizationId = 1L;

    Organization organization = new Organization();
    organization.setOrganizationId(organizationId);

    when(authnServiceMock.getAccessToken()).thenReturn(accessToken);
    when(organizationServiceMock.getOrganizationByFiscalCode(organizationFiscalCode, accessToken))
      .thenReturn(organization);
    when(debtPositionServiceMock.getDebtPositionsByOrganizationIdAndNav(
      organizationId, noticeNumber, ORDINARY_DEBT_POSITION_ORIGINS, accessToken
    )).thenReturn(List.of());

    PaymentOptionsResponseForNode response = service.getPaymentOptions(noticeNumber, organizationFiscalCode);

    Assertions.assertNull(response);
  }

  @Test
  void givenMoreThanOneDebtPositionWhenGetPaymentOptionsThenThrowConflict() {
    String noticeNumber = "NAV123";
    String organizationFiscalCode = "ORG_FISCAL_CODE";
    String accessToken = "ACCESS_TOKEN";
    Long organizationId = 1L;

    Organization organization = new Organization();
    organization.setOrganizationId(organizationId);

    when(authnServiceMock.getAccessToken()).thenReturn(accessToken);
    when(organizationServiceMock.getOrganizationByFiscalCode(organizationFiscalCode, accessToken))
      .thenReturn(organization);
    when(debtPositionServiceMock.getDebtPositionsByOrganizationIdAndNav(
      organizationId, noticeNumber, ORDINARY_DEBT_POSITION_ORIGINS, accessToken
    )).thenReturn(List.of(new DebtPositionDTO(), new DebtPositionDTO()));

    Assertions.assertThrows(ConflictException.class,
      () -> service.getPaymentOptions(noticeNumber, organizationFiscalCode));
  }

  @Test
  void givenRequestedNavPaidWhenGetPaymentOptionsThenThrowConflict() {
    String noticeNumber = "NAV123";
    String organizationFiscalCode = "ORG_FISCAL_CODE";
    String accessToken = "ACCESS_TOKEN";
    Long organizationId = 1L;

    Organization organization = new Organization();
    organization.setOrganizationId(organizationId);

    InstallmentDTO inst = new InstallmentDTO();
    inst.setNav(noticeNumber);
    inst.setStatus(InstallmentStatus.PAID);

    PaymentOptionDTO po = new PaymentOptionDTO();
    po.setInstallments(List.of(inst));

    DebtPositionDTO dp = new DebtPositionDTO();
    dp.setPaymentOptions(List.of(po));

    when(authnServiceMock.getAccessToken()).thenReturn(accessToken);
    when(organizationServiceMock.getOrganizationByFiscalCode(organizationFiscalCode, accessToken))
      .thenReturn(organization);
    when(debtPositionServiceMock.getDebtPositionsByOrganizationIdAndNav(
      organizationId, noticeNumber, ORDINARY_DEBT_POSITION_ORIGINS, accessToken
    )).thenReturn(List.of(dp));

    Assertions.assertThrows(ConflictException.class,
      () -> service.getPaymentOptions(noticeNumber, organizationFiscalCode));
  }

  @Test
  void givenOtherNavPaidButRequestedNavUnpaidWhenGetPaymentOptionsThenOk() {
    String noticeNumber = "NAV123";
    String otherNav = "NAV999";
    String organizationFiscalCode = "ORG_FISCAL_CODE";
    String accessToken = "ACCESS_TOKEN";
    Long organizationId = 1L;

    Organization organization = new Organization();
    organization.setOrganizationId(organizationId);

    InstallmentDTO otherPaid = new InstallmentDTO();
    otherPaid.setNav(otherNav);
    otherPaid.setStatus(InstallmentStatus.PAID);

    InstallmentDTO requestedUnpaid = new InstallmentDTO();
    requestedUnpaid.setNav(noticeNumber);
    requestedUnpaid.setStatus(InstallmentStatus.UNPAID);

    PaymentOptionDTO po = new PaymentOptionDTO();
    po.setInstallments(List.of(otherPaid, requestedUnpaid));

    DebtPositionDTO dp = new DebtPositionDTO();
    dp.setPaymentOptions(List.of(po));

    PaymentOptionsResponseForNode expected = new PaymentOptionsResponseForNode();

    when(authnServiceMock.getAccessToken()).thenReturn(accessToken);
    when(organizationServiceMock.getOrganizationByFiscalCode(organizationFiscalCode, accessToken))
      .thenReturn(organization);
    when(debtPositionServiceMock.getDebtPositionsByOrganizationIdAndNav(
      organizationId, noticeNumber, ORDINARY_DEBT_POSITION_ORIGINS, accessToken
    )).thenReturn(List.of(dp));
    when(mapperMock.mapToResponse(dp, organization)).thenReturn(expected);

    PaymentOptionsResponseForNode res = service.getPaymentOptions(noticeNumber, organizationFiscalCode);

    Assertions.assertSame(expected, res);
  }
}
