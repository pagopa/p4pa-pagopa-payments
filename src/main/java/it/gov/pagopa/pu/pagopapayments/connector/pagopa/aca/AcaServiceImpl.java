package it.gov.pagopa.pu.pagopapayments.connector.pagopa.aca;

import it.gov.pagopa.pu.aca.dto.generated.PaymentPositionModel;
import it.gov.pagopa.pu.pagopapayments.connector.pagopa.aca.client.AcaClient;

public class AcaServiceImpl implements AcaService {

  private final AcaClient client;

  public AcaServiceImpl(AcaClient client) {
    this.client = client;
  }

  @Override
  public void paCreatePosition(String apiKey, String orgFiscalCode, PaymentPositionModel paymentPositionModel) {
    client.createPosition(apiKey, orgFiscalCode,paymentPositionModel);
  }

  @Override
  public void paUpdatePosition(String apiKey, String orgFiscalCode, String iupd, PaymentPositionModel paymentPositionModel) {
    client.updatePosition(apiKey, orgFiscalCode, iupd, paymentPositionModel);
  }

  @Override
  public void paDeletePosition(String apiKey, String orgFiscalCode, String iupd, PaymentPositionModel paymentPositionModel) {
    client.deletePosition(apiKey, orgFiscalCode, iupd, paymentPositionModel);
  }
}
