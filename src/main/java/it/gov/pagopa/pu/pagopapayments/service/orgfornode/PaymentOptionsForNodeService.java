package it.gov.pagopa.pu.pagopapayments.service.orgfornode;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.orgfornode.dto.generated.PaymentOptionsResponseForNode;
import it.gov.pagopa.pu.pagopapayments.connector.auth.AuthnService;
import it.gov.pagopa.pu.pagopapayments.connector.debtpositions.DebtPositionService;
import it.gov.pagopa.pu.pagopapayments.connector.organization.OrganizationService;
import it.gov.pagopa.pu.pagopapayments.exception.ConflictException;
import it.gov.pagopa.pu.pagopapayments.exception.NotFoundException;
import it.gov.pagopa.pu.pagopapayments.mapper.DebtPositions2PaymentOptionsNodeResponseMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

import static it.gov.pagopa.pu.pagopapayments.util.DebtPositionUtils.ORDINARY_DEBT_POSITION_ORIGINS;
import static it.gov.pagopa.pu.pagopapayments.util.DebtPositionUtils.PAID_INSTALLMENT_STATUSES;

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

  public PaymentOptionsResponseForNode getPaymentOptions(String noticeNumber, String organizationFiscalCode) {
    String accessToken = authnService.getAccessToken();

    Organization organization = organizationService.getOrganizationByFiscalCode(organizationFiscalCode, accessToken);
    if (organization == null) {
      throw new NotFoundException("[ORGANIZATION_NOT_FOUND] Organization having fiscal code " + organizationFiscalCode + " not found");
    }

    List<DebtPositionDTO> debtPositions = debtPositionService.getDebtPositionsByOrganizationIdAndNav(organization.getOrganizationId(), noticeNumber, ORDINARY_DEBT_POSITION_ORIGINS, accessToken);
    if (CollectionUtils.isEmpty(debtPositions)) {
      return null;
    }

    if (debtPositions.size() > 1) {
      throw new ConflictException("[MULTIPLE_DEBT_POSITIONS_FOUND] More than one Debt Position having notice number " + noticeNumber + " and organization fiscal code " + organizationFiscalCode + " found");
    }

    DebtPositionDTO dp = debtPositions.getFirst();

    boolean navPaidOrReported = dp.getPaymentOptions().stream()
      .flatMap(po -> po.getInstallments().stream())
      .filter(inst -> noticeNumber.equals(inst.getNav()))
      .anyMatch(inst -> PAID_INSTALLMENT_STATUSES.contains(inst.getStatus()));

    if (navPaidOrReported) {
      throw new ConflictException("[INSTALLMENT_ALREADY_PAID] Installment having notice number " + noticeNumber + " already paid");
    }

    return mapper.mapToResponse(dp, organization);
  }
}
