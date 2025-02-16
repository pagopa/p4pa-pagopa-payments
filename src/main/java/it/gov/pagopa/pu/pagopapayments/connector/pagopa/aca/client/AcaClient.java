package it.gov.pagopa.pu.pagopapayments.connector.pagopa.aca.client;

import it.gov.pagopa.nodo.pacreateposition.dto.generated.DebtPositionResponse;
import it.gov.pagopa.nodo.pacreateposition.dto.generated.NewDebtPositionRequest;
import it.gov.pagopa.pu.pagopapayments.connector.pagopa.aca.config.AcaApisHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AcaClient {

  private final AcaApisHolder acaApisHolder;

  public AcaClient(AcaApisHolder acaApisHolder){
    this.acaApisHolder = acaApisHolder;
  }

  public DebtPositionResponse paCreatePosition(NewDebtPositionRequest request, String apiKey, String segregationCodes) {
    return acaApisHolder.getAcaApiClientByApiKey(apiKey)
      .newDebtPosition(request, segregationCodes);
  }

}
