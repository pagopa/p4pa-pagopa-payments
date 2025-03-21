package it.gov.pagopa.pu.pagopapayments.connector.debtpositions;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionOrigin;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;

import java.util.List;

public interface DebtPositionService {
  DebtPositionTypeOrg getDebtPositionTypeOrgById(Long debtPositionTypeOrgId, String accessToken);
  List<InstallmentDTO> getDebtPositionsByOrganizationIdAndNav(Long organizationId, String nav, List<DebtPositionOrigin> debtPositionOriginList, String accessToken);
}
