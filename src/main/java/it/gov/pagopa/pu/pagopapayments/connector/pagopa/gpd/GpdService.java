package it.gov.pagopa.pu.pagopapayments.connector.pagopa.gpd;

import it.gov.pagopa.nodo.gpd.dto.generated.PaymentPositionModel;

public interface GpdService {
  void paCreatePosition(String apiKey, String orgFiscalCode, PaymentPositionModel paymentPositionModel);
  void paUpdatePosition(String apiKey, String orgFiscalCode, String iupd, PaymentPositionModel paymentPositionModel);
  void paDeletePosition(String apiKey, String orgFiscalCode, String iupd, PaymentPositionModel paymentPositionModel);
}
