package it.gov.pagopa.pu.pagopapayments.connector.debtpositions;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionOrigin;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;

import java.util.List;

public interface DebtPositionService {
  DebtPositionTypeOrg getDebtPositionTypeOrgById(Long debtPositionTypeOrgId, String accessToken);
  List<InstallmentDTO> getInstallmentsByOrganizationIdAndNav(Long organizationId, String nav, List<DebtPositionOrigin> debtPositionOriginList, String accessToken);
  InstallmentDTO updateInstallmentNotificationFee(Long organizationId, String nav, Long newFeeCents, String accessToken);
  DebtPositionTypeOrg findDebtPositionTypeOrgByOrgIdAndNavAndOrigins(Long organizationId, String nav, List<DebtPositionOrigin> debtPositionOriginList, String accessToken);
}
