package it.gov.pagopa.pu.pagopapayments.connector.pagopa.aca;

import it.gov.pagopa.nodo.pacreateposition.dto.generated.DebtPositionResponse;
import it.gov.pagopa.nodo.pacreateposition.dto.generated.NewDebtPositionRequest;
import it.gov.pagopa.pu.pagopapayments.connector.pagopa.aca.client.AcaClient;
import it.gov.pagopa.pu.pagopapayments.registry.RegistryContextData;
import it.gov.pagopa.pu.pagopapayments.registry.RegistryEventType;
import it.gov.pagopa.pu.pagopapayments.registry.RegistryLogger;
import it.gov.pagopa.pu.pagopapayments.util.Utilities;
import it.gov.pagopa.pu.registries.dto.generated.RegistryOutcome;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.stereotype.Service;

@Service
public class AcaServiceImpl implements AcaService {

  private final AcaClient client;
  private final RegistryLogger registryLogger;

  public AcaServiceImpl(AcaClient client, RegistryLogger registryLogger) {
    this.client = client;
    this.registryLogger = registryLogger;
  }

  @Override
  public DebtPositionResponse paCreatePosition(NewDebtPositionRequest request, String acaApiKey, String segregationCodes) {
    RegistryContextData contextData = RegistryContextData.builder()
      .orgFiscalCode(request.getEntityFiscalCode())
      .brokerStationId(null) // @TODO: not sure where to get this, possibly null
      .pspId(null) // @TODO: not sure where to get this, possibly null
      .pspChannelId(null) // @TODO: not sure where to get this, possibly null
      .paymentMethod(null) // @TODO: not sure where to get this, possibly null
      .ccp(null) // @TODO: not sure where to get this, possibly null
      .eventType(RegistryEventType.createPosition) // @TODO: should this be createPosition as the other one in GdpService?
      .iuv(request.getIuv())
      .build();

    return registryLogger.execute(
      contextData,
      request,
      () -> Triple.of(client.paCreatePosition(request, acaApiKey, segregationCodes), null, RegistryOutcome.OK),
      null,
      true
    );
  }
}
