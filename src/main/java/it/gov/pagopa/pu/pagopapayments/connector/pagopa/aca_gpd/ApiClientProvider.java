package it.gov.pagopa.pu.pagopapayments.connector.pagopa.aca_gpd;

import it.gov.pagopa.nodo.gpd.controller.generated.DebtPositionsApiInstallmentsAndPaymentOptionsManagerApi;

public interface ApiClientProvider {
  DebtPositionsApiInstallmentsAndPaymentOptionsManagerApi getApiClientByApiKey(String apiKey);
}
