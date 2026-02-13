package it.gov.pagopa.pu.pagopapayments.service;

import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.orgfornode.dto.generated.PaymentOptionsResponse;
import it.gov.pagopa.pu.pagopapayments.connector.auth.AuthnService;
import it.gov.pagopa.pu.pagopapayments.connector.debtpositions.DebtPositionService;
import it.gov.pagopa.pu.pagopapayments.connector.organization.OrganizationService;
import it.gov.pagopa.pu.pagopapayments.exception.ConflictException;
import it.gov.pagopa.pu.pagopapayments.exception.NotFoundException;
import it.gov.pagopa.pu.pagopapayments.mapper.DebtPositions2PaymentOptionsNodeResponseMapper;
import it.gov.pagopa.pu.pagopapayments.service.paymentoptionsfornode.PaymentOptionsForNodeService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static it.gov.pagopa.pu.pagopapayments.util.DebtPositionUtils.ORDINARY_DEBT_POSITION_ORIGINS;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
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

    InstallmentDTO inst = new InstallmentDTO();
    inst.setNav(noticeNumber);
    inst.setStatus(InstallmentStatus.UNPAID);

    PaymentOptionDTO poUnpaid = new PaymentOptionDTO();
    poUnpaid.setStatus(PaymentOptionStatus.UNPAID);
    poUnpaid.setInstallments(List.of(inst));

    PaymentOptionDTO poCancelled = new PaymentOptionDTO();
    poCancelled.setStatus(PaymentOptionStatus.CANCELLED);
    poCancelled.setInstallments(List.of(inst));

    DebtPositionDTO dp = new DebtPositionDTO();
    dp.setDebtPositionOrigin(DebtPositionOrigin.ORDINARY);
    dp.setPaymentOptions(List.of(poUnpaid, poCancelled));

    List<DebtPositionDTO> debtPositions = List.of(dp);

    PaymentOptionsResponse expectedResponse = new PaymentOptionsResponse();

    when(authnServiceMock.getAccessToken()).thenReturn(accessToken);
    when(organizationServiceMock.getOrganizationByFiscalCode(organizationFiscalCode, accessToken))
      .thenReturn(organization);
    when(debtPositionServiceMock.getDebtPositionsByOrganizationIdAndNav(
      organizationId, noticeNumber, ORDINARY_DEBT_POSITION_ORIGINS, accessToken
    )).thenReturn(debtPositions);

    ArgumentCaptor<List<DebtPositionDTO>> captor = ArgumentCaptor.forClass(List.class);
    when(mapperMock.mapToResponse(captor.capture(), eq(organization)))
      .thenReturn(expectedResponse);

    // when
    PaymentOptionsResponse response = service.getPaymentOptions(noticeNumber, organizationFiscalCode);

    // then
    Assertions.assertSame(expectedResponse, response);

    List<DebtPositionDTO> passed = captor.getValue();
    Assertions.assertEquals(1, passed.size());

    DebtPositionDTO passedDp = passed.get(0);

    Assertions.assertEquals(1, passedDp.getPaymentOptions().size());
    Assertions.assertEquals(PaymentOptionStatus.UNPAID, passedDp.getPaymentOptions().get(0).getStatus());
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

    PaymentOptionsResponse response = service.getPaymentOptions(noticeNumber, organizationFiscalCode);

    Assertions.assertNull(response);
  }

  @Test
  void givenOnlySecondaryOrgDebtPositionWhenGetPaymentOptionsThenReturnNull() {
    String noticeNumber = "NAV123";
    String organizationFiscalCode = "ORG_FISCAL_CODE";
    String accessToken = "ACCESS_TOKEN";
    Long organizationId = 1L;

    Organization organization = new Organization();
    organization.setOrganizationId(organizationId);

    DebtPositionDTO secondary = new DebtPositionDTO();
    secondary.setDebtPositionOrigin(DebtPositionOrigin.SECONDARY_ORG);

    when(authnServiceMock.getAccessToken()).thenReturn(accessToken);
    when(organizationServiceMock.getOrganizationByFiscalCode(organizationFiscalCode, accessToken))
      .thenReturn(organization);
    when(debtPositionServiceMock.getDebtPositionsByOrganizationIdAndNav(
      organizationId, noticeNumber, ORDINARY_DEBT_POSITION_ORIGINS, accessToken
    )).thenReturn(List.of(secondary));

    PaymentOptionsResponse response = service.getPaymentOptions(noticeNumber, organizationFiscalCode);
    Assertions.assertNull(response);
  }

  @Test
  void givenMoreThanOneValidDebtPositionWhenGetPaymentOptionsThenThrowConflict() {
    String noticeNumber = "NAV123";
    String organizationFiscalCode = "ORG_FISCAL_CODE";
    String accessToken = "ACCESS_TOKEN";
    Long organizationId = 1L;

    Organization organization = new Organization();
    organization.setOrganizationId(organizationId);

    DebtPositionDTO dp1 = new DebtPositionDTO();
    dp1.setDebtPositionOrigin(DebtPositionOrigin.ORDINARY);

    DebtPositionDTO dp2 = new DebtPositionDTO();
    dp2.setDebtPositionOrigin(DebtPositionOrigin.ORDINARY);

    when(authnServiceMock.getAccessToken()).thenReturn(accessToken);
    when(organizationServiceMock.getOrganizationByFiscalCode(organizationFiscalCode, accessToken))
      .thenReturn(organization);
    when(debtPositionServiceMock.getDebtPositionsByOrganizationIdAndNav(
      organizationId, noticeNumber, ORDINARY_DEBT_POSITION_ORIGINS, accessToken
    )).thenReturn(List.of(dp1, dp2));

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
    po.setStatus(PaymentOptionStatus.UNPAID);
    po.setInstallments(List.of(inst));

    DebtPositionDTO dp = new DebtPositionDTO();
    dp.setDebtPositionOrigin(DebtPositionOrigin.ORDINARY);
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
    po.setStatus(PaymentOptionStatus.UNPAID);
    po.setInstallments(List.of(otherPaid, requestedUnpaid));

    DebtPositionDTO dp = new DebtPositionDTO();
    dp.setDebtPositionOrigin(DebtPositionOrigin.ORDINARY);
    dp.setPaymentOptions(List.of(po));

    PaymentOptionsResponse expected = new PaymentOptionsResponse();

    when(authnServiceMock.getAccessToken()).thenReturn(accessToken);
    when(organizationServiceMock.getOrganizationByFiscalCode(organizationFiscalCode, accessToken))
      .thenReturn(organization);
    when(debtPositionServiceMock.getDebtPositionsByOrganizationIdAndNav(
      organizationId, noticeNumber, ORDINARY_DEBT_POSITION_ORIGINS, accessToken
    )).thenReturn(List.of(dp));
    when(mapperMock.mapToResponse(anyList(), eq(organization))).thenReturn(expected);

    PaymentOptionsResponse res = service.getPaymentOptions(noticeNumber, organizationFiscalCode);

    Assertions.assertSame(expected, res);
  }
}
