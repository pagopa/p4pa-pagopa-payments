package it.gov.pagopa.pu.pagopapayments.connector.pagopa.gpd;

import it.gov.pagopa.nodo.gpd.dto.generated.PaymentPositionModel;

public interface GpdService {
  void paCreatePosition(String apiKey, String organizationfiscalcode, PaymentPositionModel paymentPositionModel);
  void paUpdatePosition(String apiKey, String organizationfiscalcode, String iupd, PaymentPositionModel paymentPositionModel);
  void paDeletePosition(String apiKey, String organizationfiscalcode, String iupd, PaymentPositionModel paymentPositionModel);



}
