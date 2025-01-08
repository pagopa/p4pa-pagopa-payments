package it.gov.pagopa.pu.pagopapayments.connector;

import it.gov.pagopa.pu.p4pa_debt_positions.dto.generated.DebtPositionTypeOrg;

public interface DebtPositionClient {

  DebtPositionTypeOrg getDebtPositionTypeOrgById(Long debtPositionTypeOrgId, String accessToken);
}
