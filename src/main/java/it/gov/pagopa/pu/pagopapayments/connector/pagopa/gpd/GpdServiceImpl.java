package it.gov.pagopa.pu.pagopapayments.connector.pagopa.gpd;

import it.gov.pagopa.nodo.gpd.dto.generated.PaymentPositionModelV3;
import it.gov.pagopa.pu.pagopapayments.connector.pagopa.gpd.client.GpdClient;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GpdServiceImpl implements GpdService {

  private final GpdClient client;

  public GpdServiceImpl(GpdClient client) {
    this.client = client;
  }

  @Override
  public void paCreatePosition(String apiKey, String orgFiscalCode, PaymentPositionModelV3 paymentPositionModel) {
    client.createPosition(apiKey, orgFiscalCode,paymentPositionModel);
  }

  @Override
  public void paUpdatePosition(String apiKey, String orgFiscalCode, String iupd, PaymentPositionModelV3 paymentPositionModel) {
    client.updatePosition(apiKey, orgFiscalCode, iupd, paymentPositionModel);
  }

  @Override
  public void paDeletePosition(String apiKey, String orgFiscalCode, String iupd, PaymentPositionModelV3 paymentPositionModel) {
    client.deletePosition(apiKey, orgFiscalCode, iupd, paymentPositionModel);
  }
}
