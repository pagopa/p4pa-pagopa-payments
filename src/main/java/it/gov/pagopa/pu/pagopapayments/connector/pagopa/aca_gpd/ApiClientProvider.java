package it.gov.pagopa.pu.pagopapayments.connector.pagopa.aca_gpd;

import it.gov.pagopa.nodo.gpd.controller.generated.DebtPositionsApiApi;

public interface ApiClientProvider {
  DebtPositionsApiApi getApiClientByApiKey(String apiKey);
}
