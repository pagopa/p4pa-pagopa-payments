package it.gov.pagopa.pu.pagopapayments.connector.pagopa.aca.client;

import io.vavr.Function4;
import it.gov.pagopa.pu.aca.gpd.v1.dto.generated.PaymentOptionModel;
import it.gov.pagopa.pu.aca.gpd.v1.dto.generated.PaymentPositionModel;
import it.gov.pagopa.pu.pagopapayments.connector.pagopa.aca.config.AcaApisHolder;
import it.gov.pagopa.pu.pagopapayments.registry.RegistryContextData;
import it.gov.pagopa.pu.pagopapayments.registry.RegistryEventType;
import it.gov.pagopa.pu.pagopapayments.registry.RegistryLogger;
import it.gov.pagopa.pu.pagopapayments.util.Utilities;
import it.gov.pagopa.pu.registries.dto.generated.RegistryOutcome;
import org.apache.commons.lang3.function.TriFunction;
import org.apache.commons.lang3.tuple.Triple;

import java.util.stream.Collectors;

public class AcaClient {

  private final RegistryLogger registryLogger;

  private final TriFunction<String, String, PaymentPositionModel, Triple<PaymentPositionModel, String, RegistryOutcome>> createPositionHandler;
  private final Function4<String, String, String, PaymentPositionModel, Triple<PaymentPositionModel, String, RegistryOutcome>> updatePositionHandler;
  private final Function4<String, String, String, PaymentPositionModel, Triple<String, String, RegistryOutcome>> deletePositionHandler;

  public AcaClient(AcaApisHolder apisHolder, RegistryLogger registryLogger) {
    this.registryLogger = registryLogger;

    this.createPositionHandler = (String apiKey, String orgFiscalCode, PaymentPositionModel paymentPositionModel) -> {
      PaymentPositionModel response = apisHolder.getApiClientByApiKey(apiKey)
        .createPosition(orgFiscalCode, paymentPositionModel, null, true);
      return Triple.of(response, null, RegistryOutcome.OK);
    };

    this.updatePositionHandler = (String apiKey, String orgFiscalCode, String iupd, PaymentPositionModel paymentPositionModel) -> {
      PaymentPositionModel response = apisHolder.getApiClientByApiKey(apiKey)
        .updatePosition(orgFiscalCode, iupd, paymentPositionModel, null, true);
      return Triple.of(response, null, RegistryOutcome.OK);
    };

    this.deletePositionHandler = (String apiKey, String orgFiscalCode, String iupd, PaymentPositionModel paymentPositionModel) -> {
      String response = apisHolder.getApiClientByApiKey(apiKey)
        .deletePosition(orgFiscalCode, iupd, null);
      return Triple.of(response, null, RegistryOutcome.OK);
    };
  }

  public void createPosition(String apiKey, String orgFiscalCode, PaymentPositionModel paymentPositionModel) {
    registryLogger.execute3(
      getRegistryContextDataFromPaymentPositionModel(
        orgFiscalCode,
        RegistryEventType.ACA_createPosition,
        paymentPositionModel
      ),
      paymentPositionModel,
      createPositionHandler,
      null,
      apiKey, orgFiscalCode, paymentPositionModel
    );
  }

  public void updatePosition(String apiKey, String orgFiscalCode, String iupd, PaymentPositionModel paymentPositionModel) {
    registryLogger.execute4(
      getRegistryContextDataFromPaymentPositionModel(
        orgFiscalCode,
        RegistryEventType.ACA_updatePosition,
        paymentPositionModel
      ),
      paymentPositionModel,
      updatePositionHandler,
      null,
      apiKey, orgFiscalCode, iupd, paymentPositionModel
    );
  }

  public void deletePosition(String apiKey, String orgFiscalCode, String iupd, PaymentPositionModel paymentPositionModel) {
    registryLogger.execute4(
      getRegistryContextDataFromPaymentPositionModel(
        orgFiscalCode,
        RegistryEventType.ACA_deletePosition,
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
