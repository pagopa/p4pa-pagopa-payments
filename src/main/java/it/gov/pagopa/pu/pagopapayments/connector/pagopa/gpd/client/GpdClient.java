package it.gov.pagopa.pu.pagopapayments.connector.pagopa.gpd.client;

import it.gov.pagopa.nodo.gpd.dto.generated.PaymentOptionModel;
import it.gov.pagopa.nodo.gpd.dto.generated.PaymentPositionModel;
import it.gov.pagopa.pu.pagopapayments.connector.pagopa.gpd.config.GpdApisHolder;
import it.gov.pagopa.pu.pagopapayments.registry.RegistryContextData;
import it.gov.pagopa.pu.pagopapayments.registry.RegistryEventType;
import it.gov.pagopa.pu.pagopapayments.registry.RegistryLogger;
import it.gov.pagopa.pu.pagopapayments.util.Utilities;
import it.gov.pagopa.pu.registries.dto.generated.RegistryOutcome;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@Slf4j
public class GpdClient {

  private final GpdApisHolder gpdApisHolder;
  private final RegistryLogger registryLogger;

  public GpdClient(GpdApisHolder gpdApisHolder, RegistryLogger registryLogger) {
    this.gpdApisHolder = gpdApisHolder;
    this.registryLogger = registryLogger;
  }

  public void createPosition(String apiKey, String organizationfiscalcode, PaymentPositionModel paymentPositionModel) {
    registryLogger.execute(
      getRegistryContextDataFromPaymentPositionModel(
        organizationfiscalcode,
        RegistryEventType.createPosition,
        paymentPositionModel
      ),
      paymentPositionModel,
      () -> {
        PaymentPositionModel response = gpdApisHolder.getGpdApiClientByApiKey(apiKey)
          .createPosition(organizationfiscalcode, paymentPositionModel, null, true);
        return Triple.of(response, null, RegistryOutcome.OK);
      },
      null
    );
  }

  public void updatePosition(String apiKey, String organizationfiscalcode, String iupd, PaymentPositionModel paymentPositionModel){
    registryLogger.execute(
      getRegistryContextDataFromPaymentPositionModel(
        organizationfiscalcode,
        RegistryEventType.updatePosition,
        paymentPositionModel
      ),
      paymentPositionModel,
      () -> {
        PaymentPositionModel response = gpdApisHolder.getGpdApiClientByApiKey(apiKey)
          .updatePosition(organizationfiscalcode, iupd, paymentPositionModel, null,true);
        return Triple.of(response, null, RegistryOutcome.OK);
      },
      null
    );
  }

  public void deletePosition(String apiKey, String organizationfiscalcode, String iupd, PaymentPositionModel paymentPositionModel) {
    registryLogger.execute(
      getRegistryContextDataFromPaymentPositionModel(
        organizationfiscalcode,
        RegistryEventType.deletePosition,
        paymentPositionModel
      ),
      iupd,
      () -> {
        String response = gpdApisHolder.getGpdApiClientByApiKey(apiKey)
          .deletePosition(organizationfiscalcode, iupd, null);
        return Triple.of(response, null, RegistryOutcome.OK);
      },
      null
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
        .collect(Collectors.joining(Utilities.IUV_SEPARATOR));
    }

    return RegistryContextData.builder()
      .orgFiscalCode(organizationFiscalCode)
      .eventType(eventType)
      .iuv(iuvConcat)
      .build();
  }
}
