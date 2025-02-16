package it.gov.pagopa.pu.pagopapayments.connector.pagopa.aca;

import it.gov.pagopa.nodo.pacreateposition.dto.generated.DebtPositionResponse;
import it.gov.pagopa.nodo.pacreateposition.dto.generated.NewDebtPositionRequest;

public interface AcaService {
  DebtPositionResponse paCreatePosition(NewDebtPositionRequest request, String acaApiKey, String segregationCodes);
}
