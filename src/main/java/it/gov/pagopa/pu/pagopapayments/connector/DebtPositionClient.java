package it.gov.pagopa.pu.pagopapayments.connector;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;

import java.util.List;

public interface DebtPositionClient {

  DebtPositionTypeOrg getDebtPositionTypeOrgById(Long debtPositionTypeOrgId, String accessToken);

  List<InstallmentDTO> getDebtPositionsByOrganizationIdAndNav(Long organizationId, String nav, String accessToken);
}
