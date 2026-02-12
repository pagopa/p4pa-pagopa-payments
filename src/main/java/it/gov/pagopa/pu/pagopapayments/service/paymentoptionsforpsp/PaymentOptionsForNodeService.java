package it.gov.pagopa.pu.pagopapayments.service.paymentoptionsforpsp;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.orgfornode.dto.generated.PaymentOptionsResponse;
import it.gov.pagopa.pu.pagopapayments.connector.auth.AuthnService;
import it.gov.pagopa.pu.pagopapayments.connector.debtpositions.DebtPositionService;
import it.gov.pagopa.pu.pagopapayments.connector.organization.OrganizationService;
import it.gov.pagopa.pu.pagopapayments.exception.ConflictException;
import it.gov.pagopa.pu.pagopapayments.mapper.DebtPositions2PaymentOptionsNodeResponseMapper;
import org.springframework.stereotype.Service;

import java.util.List;

import static it.gov.pagopa.pu.pagopapayments.util.DebtPositionUtils.ORDINARY_DEBT_POSITION_ORIGINS;

@Service
public class PaymentOptionsForNodeService {

  private final AuthnService authnService;
  private final OrganizationService organizationService;
  private final DebtPositionService debtPositionService;
  private final DebtPositions2PaymentOptionsNodeResponseMapper mapper;

  public PaymentOptionsForNodeService(AuthnService authnService, OrganizationService organizationService, DebtPositionService debtPositionService, DebtPositions2PaymentOptionsNodeResponseMapper mapper) {
    this.authnService = authnService;
    this.organizationService = organizationService;
    this.debtPositionService = debtPositionService;
    this.mapper = mapper;
  }

  public PaymentOptionsResponse getPaymentOptions(String noticeNumber, String organizationFiscalCode) {
    String accessToken = authnService.getAccessToken();

    Organization organization = organizationService.getOrganizationByFiscalCode(organizationFiscalCode, accessToken);
    if (organization == null) {
      return null;
    }

    List<DebtPositionDTO> debtPositions = debtPositionService.getDebtPositionsByOrganizationIdAndNav(organization.getOrganizationId(), noticeNumber, ORDINARY_DEBT_POSITION_ORIGINS, accessToken);
    if (debtPositions == null || debtPositions.isEmpty()) {
      return null;
    }

    boolean paidInstallmentFound = debtPositions.stream()
      .flatMap(dp -> dp.getPaymentOptions().stream())
      .flatMap(po -> po.getInstallments().stream())
      .anyMatch(inst ->
        inst.getStatus() == InstallmentStatus.PAID || inst.getStatus() == InstallmentStatus.REPORTED);
    if (paidInstallmentFound) {
      throw new ConflictException("[NOTICE_ALREADY_PAID] Notice already paid");
    }

    return mapper.mapToResponse(debtPositions, organization);
  }
}
