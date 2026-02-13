package it.gov.pagopa.pu.pagopapayments.service.paymentoptionsfornode;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionOrigin;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.debtpositions.dto.generated.PaymentOptionStatus;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.orgfornode.dto.generated.PaymentOptionsResponse;
import it.gov.pagopa.pu.pagopapayments.connector.auth.AuthnService;
import it.gov.pagopa.pu.pagopapayments.connector.debtpositions.DebtPositionService;
import it.gov.pagopa.pu.pagopapayments.connector.organization.OrganizationService;
import it.gov.pagopa.pu.pagopapayments.exception.ConflictException;
import it.gov.pagopa.pu.pagopapayments.exception.NotFoundException;
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
      throw new NotFoundException("[ORGANIZATION_NOT_FOUND] Organization not found");
    }

    List<DebtPositionDTO> debtPositions = debtPositionService.getDebtPositionsByOrganizationIdAndNav(organization.getOrganizationId(), noticeNumber, ORDINARY_DEBT_POSITION_ORIGINS, accessToken);
    if (debtPositions == null) {
      return null;
    }

    List<DebtPositionDTO> validDebtPositions = debtPositions.stream()
      .filter(dp -> dp.getDebtPositionOrigin() != DebtPositionOrigin.SECONDARY_ORG)
      .toList();

    if (validDebtPositions.isEmpty()) {
      return null;
    }

    if (validDebtPositions.size() > 1) {
      throw new ConflictException("[MULTIPLE_DEBT_POSITIONS_FOUND] More than one valid Debt Position found");
    }

    DebtPositionDTO dp = validDebtPositions.get(0);

    boolean navPaidOrReported = dp.getPaymentOptions().stream()
      .flatMap(po -> po.getInstallments().stream())
      .filter(inst -> noticeNumber.equals(inst.getNav()))
      .anyMatch(inst -> inst.getStatus() == InstallmentStatus.PAID || inst.getStatus() == InstallmentStatus.REPORTED);

    if (navPaidOrReported) {
      throw new ConflictException("[INSTALLMENT_ALREADY_PAID] Installment already paid");
    }

    List<DebtPositionDTO> dpForResponse = List.of(
      dp.paymentOptions(dp.getPaymentOptions().stream()
        .filter(po -> po.getStatus() == PaymentOptionStatus.UNPAID || po.getStatus() == PaymentOptionStatus.PARTIALLY_PAID)
        .toList()
      ));

    return mapper.mapToResponse(dpForResponse, organization);
  }
}
