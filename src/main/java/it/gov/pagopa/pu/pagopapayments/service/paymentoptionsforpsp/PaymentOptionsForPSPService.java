package it.gov.pagopa.pu.pagopapayments.service.paymentoptionsforpsp;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.fororgs.dto.generated.PaymentOptionsResponse;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.pagopapayments.connector.auth.AuthnService;
import it.gov.pagopa.pu.pagopapayments.connector.debtpositions.DebtPositionService;
import it.gov.pagopa.pu.pagopapayments.connector.organization.OrganizationService;
import it.gov.pagopa.pu.pagopapayments.mapper.DebtPositions2PaymentOptionsResponseMapper;
import org.springframework.stereotype.Service;

import java.util.List;

import static it.gov.pagopa.pu.pagopapayments.util.DebtPositionUtils.ORDINARY_DEBT_POSITION_ORIGINS;

@Service
public class PaymentOptionsForPSPService {

  private final AuthnService authnService;
  private final OrganizationService organizationService;
  private final DebtPositionService debtPositionService;
  private final DebtPositions2PaymentOptionsResponseMapper mapper;

  public PaymentOptionsForPSPService(AuthnService authnService, OrganizationService organizationService, DebtPositionService debtPositionService, DebtPositions2PaymentOptionsResponseMapper mapper) {
    this.authnService = authnService;
    this.organizationService = organizationService;
    this.debtPositionService = debtPositionService;
    this.mapper = mapper;
  }

  public PaymentOptionsResponse getPaymentOptions(String noticeNumber, String organizationFiscalCode) {
    String accessToken = authnService.getAccessToken();
    Organization organization = organizationService.getOrganizationByFiscalCode(organizationFiscalCode, accessToken);
    List<DebtPositionDTO> debtPositions = debtPositionService.getDebtPositionsByOrganizationIdAndNav(organization.getOrganizationId(), noticeNumber, ORDINARY_DEBT_POSITION_ORIGINS, accessToken);

    return mapper.mapToResponse(debtPositions, organization);
  }
}
