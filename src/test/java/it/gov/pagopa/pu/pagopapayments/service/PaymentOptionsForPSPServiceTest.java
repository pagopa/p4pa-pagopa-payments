package it.gov.pagopa.pu.pagopapayments.service;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.pagopapayments.connector.auth.AuthnService;
import it.gov.pagopa.pu.pagopapayments.connector.debtpositions.DebtPositionService;
import it.gov.pagopa.pu.pagopapayments.connector.organization.OrganizationService;
import it.gov.pagopa.pu.pagopapayments.dto.generated.PaymentOptionsResponse;
import it.gov.pagopa.pu.pagopapayments.mapper.DebtPositions2PaymentOptionsResponseMapper;
import it.gov.pagopa.pu.pagopapayments.service.paymentoptionsforpsp.PaymentOptionsForPSPService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static it.gov.pagopa.pu.pagopapayments.util.DebtPositionUtils.ORDINARY_DEBT_POSITION_ORIGINS;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentOptionsForPSPServiceTest {

  @Mock
  private AuthnService authnServiceMock;

  @Mock
  private OrganizationService organizationServiceMock;

  @Mock
  private DebtPositionService debtPositionServiceMock;

  @Mock
  private DebtPositions2PaymentOptionsResponseMapper mapperMock;

  @InjectMocks
  private PaymentOptionsForPSPService service;

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

    verify(authnServiceMock, times(1))
      .getAccessToken();
    verify(organizationServiceMock, times(1))
      .getOrganizationByFiscalCode(organizationFiscalCode, accessToken);
    verify(debtPositionServiceMock, times(1))
      .getDebtPositionsByOrganizationIdAndNav(organizationId, noticeNumber, ORDINARY_DEBT_POSITION_ORIGINS, accessToken);
    verify(mapperMock, times(1))
      .mapToResponse(debtPositions, organization);

    verifyNoMoreInteractions(authnServiceMock, organizationServiceMock, debtPositionServiceMock, mapperMock);
  }
}
