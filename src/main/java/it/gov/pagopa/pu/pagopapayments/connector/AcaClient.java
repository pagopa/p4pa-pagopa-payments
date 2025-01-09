package it.gov.pagopa.pu.pagopapayments.connector;

import it.gov.pagopa.nodo.pacreateposition.dto.generated.DebtPositionResponse;
import it.gov.pagopa.nodo.pacreateposition.dto.generated.NewDebtPositionRequest;

public interface AcaClient {
  DebtPositionResponse paCreatePosition(NewDebtPositionRequest request, String acaApiKey, String segregationCodes);
}
