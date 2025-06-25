package it.gov.pagopa.pu.pagopapayments.connector.pagopa.gpd;

import it.gov.pagopa.nodo.gpd.dto.generated.PaymentPositionModel;
import it.gov.pagopa.pu.pagopapayments.connector.pagopa.gpd.client.GpdClient;
import it.gov.pagopa.pu.pagopapayments.registry.RegistryContextData;
import it.gov.pagopa.pu.pagopapayments.registry.RegistryEventType;
import it.gov.pagopa.pu.pagopapayments.registry.RegistryLogger;
import it.gov.pagopa.pu.pagopapayments.util.Utilities;
import it.gov.pagopa.pu.registries.dto.generated.RegistryOutcome;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class GpdServiceImpl implements GpdService {

  private final GpdClient client;
  private final RegistryLogger registryLogger;

  public GpdServiceImpl(GpdClient client, RegistryLogger logger) {
    this.client = client;
    this.registryLogger = logger;
  }

  @Override
  public void paCreatePosition(String apiKey, String organizationfiscalcode, PaymentPositionModel paymentPositionModel) {
    registryLogger.execute(
      getRegistryContextDataFromPaymentPositionModel(
        organizationfiscalcode,
        RegistryEventType.createPosition,
        paymentPositionModel.getIupd(),
        paymentPositionModel
      ),
      paymentPositionModel,
      () -> {
        client.createPosition(apiKey, organizationfiscalcode,paymentPositionModel);
        return Triple.of(null, null, RegistryOutcome.OK);
      },
      e -> {
        log.error(e.getMessage(), e);
        return null;
      }
    );
  }

  @Override
  public void paUpdatePosition(String apiKey, String organizationfiscalcode, String iupd, PaymentPositionModel paymentPositionModel) {
    registryLogger.execute(
      getRegistryContextDataFromPaymentPositionModel(
        organizationfiscalcode,
        RegistryEventType.updatePosition,
        iupd,
        paymentPositionModel
      ),
      paymentPositionModel,
      () -> {
        client.updatePosition(apiKey, organizationfiscalcode, iupd, paymentPositionModel);
        return Triple.of(null, null, RegistryOutcome.OK);
      },
      e -> {
        log.error(e.getMessage(), e);
        return null;
      }
    );
  }

  @Override
  public void paDeletePosition(String apiKey, String organizationfiscalcode, String iupd) {
    RegistryContextData contextData = RegistryContextData.builder()
      .orgFiscalCode(organizationfiscalcode)
      .brokerStationId(null) // @TODO: no broker station found on payment position model
      .pspId(null) // @TODO: no psp found on payment position model
      .pspChannelId(null) // @TODO: no psp channel found on payment position model
      .paymentMethod(null) // @TODO: being a position, does it have a payment method?
      .ccp(null) // @TODO: i guess the receipt is not available at this point
      .eventType(RegistryEventType.deletePosition)
      .iuv(Utilities.nav2Iuv(iupd))
      .build();

    registryLogger.execute(
      contextData,
      iupd,
      () -> {
        client.deletePosition(apiKey, organizationfiscalcode, iupd);
        return Triple.of(null, null, RegistryOutcome.OK);
      },
      e -> {
        log.error(e.getMessage(), e);
        return null;
      }
    );
  }

  private RegistryContextData getRegistryContextDataFromPaymentPositionModel(
    String organizationFiscalCode,
    RegistryEventType eventType,
    String iupd,
    PaymentPositionModel paymentPositionModel
  ) {
    return RegistryContextData.builder()
      .orgFiscalCode(organizationFiscalCode)
      .brokerStationId(null) // @TODO: no broker station found on payment position model
      .pspId(null) // @TODO: no psp found on payment position model
      .pspChannelId(null) // @TODO: no psp channel found on payment position model
      .paymentMethod(null) // @TODO: being a position, does it have a payment method?
      .ccp(null) // @TODO: i guess the receipt is not available at this point
      .eventType(eventType)
      .iuv(Utilities.nav2Iuv(iupd))
      .build();
  }
}
