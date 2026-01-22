package it.gov.pagopa.pu.pagopapayments.connector.pagopa.aca;

import it.gov.pagopa.pu.aca.gpd.v1.dto.generated.PaymentPositionModel;

public interface AcaService {
  void paCreatePosition(String apiKey, String orgFiscalCode, PaymentPositionModel paymentPositionModel);

  void paUpdatePosition(String apiKey, String orgFiscalCode, String iupd, PaymentPositionModel paymentPositionModel);

  void paDeletePosition(String apiKey, String orgFiscalCode, String iupd, PaymentPositionModel paymentPositionModel);
}
