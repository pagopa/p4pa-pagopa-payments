package it.gov.pagopa.pu.pagopapayments.connector;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionTypeOrg;

public interface DebtPositionClient {

  DebtPositionTypeOrg getDebtPositionTypeOrgById(Long debtPositionTypeOrgId, String accessToken);
}
