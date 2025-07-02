package it.gov.pagopa.pu.pagopapayments.connector.pagopa.aca.client;

import it.gov.pagopa.nodo.pacreateposition.dto.generated.DebtPositionResponse;
import it.gov.pagopa.nodo.pacreateposition.dto.generated.NewDebtPositionRequest;
import it.gov.pagopa.pu.pagopapayments.connector.pagopa.aca.config.AcaApisHolder;
import it.gov.pagopa.pu.pagopapayments.registry.RegistryContextData;
import it.gov.pagopa.pu.pagopapayments.registry.RegistryEventType;
import it.gov.pagopa.pu.pagopapayments.registry.RegistryLogger;
import it.gov.pagopa.pu.registries.dto.generated.RegistryOutcome;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AcaClient {

  private final AcaApisHolder acaApisHolder;
  private final RegistryLogger registryLogger;

  public AcaClient(AcaApisHolder acaApisHolder, RegistryLogger registryLogger){
    this.acaApisHolder = acaApisHolder;
    this.registryLogger = registryLogger;
  }

  public DebtPositionResponse paCreatePosition(NewDebtPositionRequest request, String apiKey, String segregationCodes) {
    RegistryContextData contextData = RegistryContextData.builder()
      .orgFiscalCode(request.getPaFiscalCode())
      .eventType(RegistryEventType.ACA_newDebtPosition)
      .iuv(request.getIuv())
      .build();

    return registryLogger.execute(
      contextData,
      request,
      () -> Triple.of(
        acaApisHolder.getAcaApiClientByApiKey(apiKey).newDebtPosition(request, segregationCodes),
        null,
        RegistryOutcome.OK
      ),
      null
    );
  }

}
