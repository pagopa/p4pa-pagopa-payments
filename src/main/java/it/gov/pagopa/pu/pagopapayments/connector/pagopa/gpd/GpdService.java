package it.gov.pagopa.pu.pagopapayments.connector.pagopa.gpd;

import it.gov.pagopa.nodo.gpd.dto.generated.PaymentPositionModelV3;

public interface GpdService {
  void paCreatePosition(String apiKey, String orgFiscalCode, PaymentPositionModelV3 paymentPositionModel);

  void paUpdatePosition(String apiKey, String orgFiscalCode, String iupd, PaymentPositionModelV3 paymentPositionModel);

  void paDeletePosition(String apiKey, String orgFiscalCode, String iupd, PaymentPositionModelV3 paymentPositionModel);
}
