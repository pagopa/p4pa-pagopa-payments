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

  public PaymentPositionModel createPosition(String apiKey, String organizationfiscalcode, PaymentPositionModel paymentPositionModel) {
    return gpdApisHolder.getGpdApiClientByApiKey(apiKey)
      .createPosition(organizationfiscalcode, paymentPositionModel, null, true);
  }

  public PaymentPositionModel updatePosition(String apiKey, String organizationfiscalcode, String iupd, PaymentPositionModel paymentPositionModel){
    return gpdApisHolder.getGpdApiClientByApiKey(apiKey)
      .updatePosition(organizationfiscalcode, iupd, paymentPositionModel, null,true);
  }

  public String deletePosition(String apiKey, String organizationfiscalcode, String iupd) {
    return gpdApisHolder.getGpdApiClientByApiKey(apiKey)
      .deletePosition(organizationfiscalcode, iupd, null);
  }
}
