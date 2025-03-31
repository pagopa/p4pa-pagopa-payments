package it.gov.pagopa.pu.pagopapayments.connector.pagopa.aca;

import it.gov.pagopa.nodo.pacreateposition.dto.generated.DebtPositionResponse;
import it.gov.pagopa.nodo.pacreateposition.dto.generated.NewDebtPositionRequest;
import it.gov.pagopa.pu.pagopapayments.connector.pagopa.aca.client.AcaClient;
import org.springframework.stereotype.Service;

@Service
public class AcaServiceImpl implements AcaService {

  private final AcaClient client;

  public AcaServiceImpl(AcaClient client) {
    this.client = client;
  }

  @Override
  public DebtPositionResponse paCreatePosition(NewDebtPositionRequest request, String acaApiKey, String segregationCodes) {
    return client.paCreatePosition(request, acaApiKey, segregationCodes);
  }
}
