package it.gov.pagopa.pu.pagopapayments.connector.pagopa.aca.client;

import it.gov.pagopa.pu.aca.gpd.v1.dto.generated.PaymentOptionModel;
import it.gov.pagopa.pu.aca.gpd.v1.dto.generated.PaymentPositionModel;
import it.gov.pagopa.pu.pagopapayments.connector.pagopa.aca.config.AcaApisHolder;
import it.gov.pagopa.pu.pagopapayments.registry.RegistryContextData;
import it.gov.pagopa.pu.pagopapayments.registry.RegistryEventType;
import it.gov.pagopa.pu.pagopapayments.registry.RegistryLogger;
import it.gov.pagopa.pu.pagopapayments.util.Utilities;
import it.gov.pagopa.pu.registries.dto.generated.RegistryOutcome;
import org.apache.commons.lang3.tuple.Triple;

import java.util.stream.Collectors;

public class AcaClient {

  private final AcaApisHolder apisHolder;
  private final RegistryLogger registryLogger;

  public AcaClient(AcaApisHolder apisHolder, RegistryLogger registryLogger) {
    this.apisHolder = apisHolder;
    this.registryLogger = registryLogger;
  }

  public void createPosition(String apiKey, String orgFiscalCode, PaymentPositionModel paymentPositionModel) {
    registryLogger.execute(
      getRegistryContextDataFromPaymentPositionModel(
        orgFiscalCode,
        RegistryEventType.ACA_createPosition,
        paymentPositionModel
      ),
      paymentPositionModel,
      () -> {
        PaymentPositionModel response = apisHolder.getDebtPositionsApi(apiKey)
          .createPosition(orgFiscalCode, paymentPositionModel, null, true);
        return Triple.of(response, null, RegistryOutcome.OK);
      },
      null
    );
  }

  public void updatePosition(String apiKey, String orgFiscalCode, String iupd, PaymentPositionModel paymentPositionModel) {
    registryLogger.execute(
      getRegistryContextDataFromPaymentPositionModel(
        orgFiscalCode,
        RegistryEventType.ACA_updatePosition,
        paymentPositionModel
      ),
      paymentPositionModel,
      () -> {
        PaymentPositionModel response = apisHolder.getDebtPositionsApi(apiKey)
          .updatePosition(orgFiscalCode, iupd, paymentPositionModel, null, true);
        return Triple.of(response, null, RegistryOutcome.OK);
      },
      null
    );
  }

  public void deletePosition(String apiKey, String orgFiscalCode, String iupd, PaymentPositionModel paymentPositionModel) {
    registryLogger.execute(
      getRegistryContextDataFromPaymentPositionModel(
        orgFiscalCode,
        RegistryEventType.ACA_deletePosition,
        paymentPositionModel
      ),
      iupd,
      () -> {
        String response = apisHolder.getDebtPositionsApi(apiKey)
          .deletePosition(orgFiscalCode, iupd, null);
        return Triple.of(response, null, RegistryOutcome.OK);
      },
      null
    );
  }

  private RegistryContextData getRegistryContextDataFromPaymentPositionModel(
    String orgFiscalCode,
    RegistryEventType eventType,
    PaymentPositionModel paymentPositionModel) {

    String iuvConcat = "";
    if (paymentPositionModel.getPaymentOption() != null) {
      iuvConcat = paymentPositionModel.getPaymentOption().stream()
        .map(PaymentOptionModel::getIuv)
        .collect(Collectors.joining(Utilities.IUV_SEPARATOR));
    }
    return RegistryContextData.builder()
      .orgFiscalCode(orgFiscalCode)
      .eventType(eventType)
      .iuv(iuvConcat)
      .build();
  }
}
