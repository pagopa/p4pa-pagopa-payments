package it.gov.pagopa.pu.pagopapayments.connector.pagopa.gpd;

import it.gov.pagopa.nodo.gpd.dto.generated.PaymentOptionModel;
import it.gov.pagopa.nodo.gpd.dto.generated.PaymentPositionModel;
import it.gov.pagopa.pu.pagopapayments.connector.pagopa.gpd.client.GpdClient;
import it.gov.pagopa.pu.pagopapayments.registry.RegistryContextData;
import it.gov.pagopa.pu.pagopapayments.registry.RegistryEventType;
import it.gov.pagopa.pu.pagopapayments.registry.RegistryLogger;
import it.gov.pagopa.pu.registries.dto.generated.RegistryOutcome;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

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
        paymentPositionModel
      ),
      paymentPositionModel,
      () -> {
        PaymentPositionModel response = client.createPosition(apiKey, organizationfiscalcode,paymentPositionModel);
        return Triple.of(response, null, RegistryOutcome.OK);
      },
      null,
      true
    );
  }

  @Override
  public void paUpdatePosition(String apiKey, String organizationfiscalcode, String iupd, PaymentPositionModel paymentPositionModel) {
    registryLogger.execute(
      getRegistryContextDataFromPaymentPositionModel(
        organizationfiscalcode,
        RegistryEventType.updatePosition,
        paymentPositionModel
      ),
      paymentPositionModel,
      () -> {
        PaymentPositionModel response = client.updatePosition(apiKey, organizationfiscalcode, iupd, paymentPositionModel);
        return Triple.of(response, null, RegistryOutcome.OK);
      },
      null,
      true
    );
  }

  @Override
  public void paDeletePosition(String apiKey, String organizationfiscalcode, String iupd) {
    RegistryContextData contextData = RegistryContextData.builder()
      .orgFiscalCode(organizationfiscalcode)
      .eventType(RegistryEventType.deletePosition)
      .build();

    registryLogger.execute(
      contextData,
      iupd,
      () -> {
        String response = client.deletePosition(apiKey, organizationfiscalcode, iupd);
        return Triple.of(response, null, RegistryOutcome.OK);
      },
      null,
      true
    );
  }

  private RegistryContextData getRegistryContextDataFromPaymentPositionModel(
    String organizationFiscalCode,
    RegistryEventType eventType,
    PaymentPositionModel paymentPositionModel
  ) {
    String iuvConcat = "";

    if (paymentPositionModel.getPaymentOption() != null) {
      iuvConcat = paymentPositionModel.getPaymentOption().stream()
        .map(PaymentOptionModel::getIuv)
        .collect(Collectors.joining(","));
    }

    return RegistryContextData.builder()
      .orgFiscalCode(organizationFiscalCode)
      .eventType(eventType)
      .iuv(iuvConcat)
      .build();
  }
}
