package it.gov.pagopa.pu.pagopapayments.connector.pagopa.gpd;

import it.gov.pagopa.nodo.gpd.dto.generated.PaymentPositionModel;

public interface GpdService {
  void paCreatePosition(String apiKey, String organizationfiscalcode, PaymentPositionModel paymentPositionModel, Boolean toPublish);
  void paUpdatePosition(String apiKey, String organizationfiscalcode, String iupd, PaymentPositionModel paymentPositionModel, Boolean toPublish);
  void paDeletePosition(String apiKey, String organizationfiscalcode, String iupd);



}
