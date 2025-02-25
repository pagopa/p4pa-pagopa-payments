package it.gov.pagopa.pu.pagopapayments.connector.pagopa.gpd.client;

import it.gov.pagopa.nodo.gpd.dto.generated.PaymentPositionModel;
import it.gov.pagopa.pu.pagopapayments.connector.pagopa.gpd.config.GpdApisHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class GpdClient {

  private final GpdApisHolder gpdApisHolder;

  public GpdClient(GpdApisHolder gpdApisHolder) {
    this.gpdApisHolder = gpdApisHolder;
  }

  public void createPosition(String apiKey, String organizationfiscalcode, PaymentPositionModel paymentPositionModel) {
    gpdApisHolder.getGpdApiClientByApiKey(apiKey)
      .createPosition(organizationfiscalcode, paymentPositionModel, null, true);
  }

  public void updatePosition(String apiKey, String organizationfiscalcode, String iupd, PaymentPositionModel paymentPositionModel){
    gpdApisHolder.getGpdApiClientByApiKey(apiKey)
      .updatePosition(organizationfiscalcode, iupd, paymentPositionModel, null,true);
  }

  public void deletePosition(String apiKey, String organizationfiscalcode, String iupd) {
    gpdApisHolder.getGpdApiClientByApiKey(apiKey)
      .deletePosition(organizationfiscalcode, iupd, null);
  }
}
