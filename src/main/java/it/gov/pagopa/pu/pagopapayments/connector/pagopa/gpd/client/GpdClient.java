package it.gov.pagopa.pu.pagopapayments.connector.pagopa.gpd.client;

import io.vavr.Function4;
import it.gov.pagopa.nodo.gpd.dto.generated.InstallmentModel;
import it.gov.pagopa.nodo.gpd.dto.generated.PaymentPositionModelV3;
import it.gov.pagopa.pu.pagopapayments.connector.pagopa.gpd.config.GpdApisHolder;
import it.gov.pagopa.pu.pagopapayments.registry.RegistryContextData;
import it.gov.pagopa.pu.pagopapayments.registry.RegistryEventType;
import it.gov.pagopa.pu.pagopapayments.registry.RegistryLogger;
import it.gov.pagopa.pu.pagopapayments.util.Utilities;
import it.gov.pagopa.pu.registries.dto.generated.RegistryOutcome;
import org.apache.commons.lang3.function.TriFunction;
import org.apache.commons.lang3.tuple.Triple;

import java.util.Objects;
import java.util.stream.Collectors;

public class GpdClient {

  private final RegistryLogger registryLogger;

  private final TriFunction<String, String, PaymentPositionModelV3, Triple<PaymentPositionModelV3, String, RegistryOutcome>> createPositionHandler;
  private final Function4<String, String, String, PaymentPositionModelV3, Triple<PaymentPositionModelV3, String, RegistryOutcome>> updatePositionHandler;
  private final Function4<String, String, String, PaymentPositionModelV3, Triple<String, String, RegistryOutcome>> deletePositionHandler;

  public GpdClient(GpdApisHolder apisHolder, RegistryLogger registryLogger) {
    this.registryLogger = registryLogger;

    this.createPositionHandler = (apiKey, orgFiscalCode, paymentPositionModel) -> {
      PaymentPositionModelV3 response = apisHolder.getApiClientByApiKey(apiKey)
        .createPosition(orgFiscalCode, true, null, paymentPositionModel);
      return Triple.of(response, null, RegistryOutcome.OK);
    };

    this.updatePositionHandler = (apiKey, orgFiscalCode, iupd, paymentPositionModel) -> {
      PaymentPositionModelV3 response = apisHolder.getApiClientByApiKey(apiKey)
        .updatePosition(orgFiscalCode, iupd, true, null, paymentPositionModel);
      return Triple.of(response, null, RegistryOutcome.OK);
    };

    this.deletePositionHandler = (String apiKey, String orgFiscalCode, String iupd, PaymentPositionModelV3 paymentPositionModel) -> {
      String response = apisHolder.getApiClientByApiKey(apiKey)
        .deletePosition(orgFiscalCode, iupd, null);
      return Triple.of(response, null, RegistryOutcome.OK);
    };
  }

  public void createPosition(String apiKey, String orgFiscalCode, PaymentPositionModelV3 paymentPositionModel) {
    registryLogger.execute3(
      getRegistryContextDataFromPaymentPositionModel(
        orgFiscalCode,
        RegistryEventType.GPD_createPosition,
        paymentPositionModel
      ),
      paymentPositionModel,
      createPositionHandler,
      null,
      apiKey, orgFiscalCode, paymentPositionModel
    );
  }

  public void updatePosition(String apiKey, String orgFiscalCode, String iupd, PaymentPositionModelV3 paymentPositionModel){
    registryLogger.execute4(
      getRegistryContextDataFromPaymentPositionModel(
        orgFiscalCode,
        RegistryEventType.GPD_updatePosition,
        paymentPositionModel
      ),
      paymentPositionModel,
      updatePositionHandler,
      null,
      apiKey, orgFiscalCode, iupd, paymentPositionModel
    );
  }

  public void deletePosition(String apiKey, String orgFiscalCode, String iupd, PaymentPositionModelV3 paymentPositionModel) {
    registryLogger.execute4(
      getRegistryContextDataFromPaymentPositionModel(
        orgFiscalCode,
        RegistryEventType.GPD_deletePosition,
        paymentPositionModel
      ),
      iupd,
      deletePositionHandler,
      null,
      apiKey, orgFiscalCode, iupd, paymentPositionModel
    );
  }

  private RegistryContextData getRegistryContextDataFromPaymentPositionModel(
    String orgFiscalCode,
    RegistryEventType eventType,
    PaymentPositionModelV3 paymentPositionModel) {

    String iuvConcat = paymentPositionModel.getPaymentOption().stream()
              .filter(Objects::nonNull)
              .flatMap(po -> po.getInstallments().stream())
              .filter(Objects::nonNull)
              .map(InstallmentModel::getIuv)
              .collect(Collectors.joining(Utilities.IUV_SEPARATOR));

      return RegistryContextData.builder()
      .orgFiscalCode(orgFiscalCode)
      .eventType(eventType)
      .iuv(iuvConcat)
      .build();
  }
}
