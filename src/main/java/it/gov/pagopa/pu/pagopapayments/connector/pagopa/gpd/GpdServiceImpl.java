package it.gov.pagopa.pu.pagopapayments.connector.pagopa.gpd;

import it.gov.pagopa.nodo.gpd.dto.generated.PaymentPositionModel;
import it.gov.pagopa.pu.pagopapayments.connector.pagopa.gpd.client.GpdClient;
import org.springframework.stereotype.Service;

@Service
public class GpdServiceImpl implements GpdService {

  private final GpdClient client;

  public GpdServiceImpl(GpdClient client) {
    this.client = client;
  }

  @Override
  public void paCreatePosition(String apiKey, String organizationfiscalcode, PaymentPositionModel paymentPositionModel) {
    client.createPosition(apiKey, organizationfiscalcode,paymentPositionModel);
  }

  @Override
  public void paUpdatePosition(String apiKey, String organizationfiscalcode, String iupd, PaymentPositionModel paymentPositionModel) {
    client.updatePosition(apiKey, organizationfiscalcode,iupd, paymentPositionModel);
  }

  @Override
  public void paDeletePosition(String apiKey, String organizationfiscalcode, String iupd) {
    client.deletePosition(apiKey, organizationfiscalcode,iupd);
  }
}
