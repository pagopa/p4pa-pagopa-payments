package it.gov.pagopa.pu.pagopapayments.connector;

import it.gov.pagopa.nodo.paCreatePosition.dto.generated.DebtPositionResponse;
import it.gov.pagopa.nodo.paCreatePosition.dto.generated.NewDebtPositionRequest;

public interface AcaClient {
  DebtPositionResponse paCreatePosition(Long organizationId, NewDebtPositionRequest request);
}
