package it.gov.pagopa.pu.pagopapayments.connector.pagopa.aca.client;

import it.gov.pagopa.pu.aca.gpd.v1.controller.generated.DebtPositionsApiApi;
import it.gov.pagopa.pu.aca.gpd.v1.dto.generated.PaymentOptionModel;
import it.gov.pagopa.pu.aca.gpd.v1.dto.generated.PaymentPositionModel;
import it.gov.pagopa.pu.pagopapayments.connector.pagopa.aca.config.AcaApisHolder;
import it.gov.pagopa.pu.pagopapayments.registry.RegistryContextData;
import it.gov.pagopa.pu.pagopapayments.registry.RegistryEventType;
import it.gov.pagopa.pu.pagopapayments.registry.RegistryLogger;
import it.gov.pagopa.pu.pagopapayments.registry.RegistryLoggerTest;
import it.gov.pagopa.pu.pagopapayments.util.TestUtils;
import it.gov.pagopa.pu.pagopapayments.util.Utilities;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.jemos.podam.api.PodamFactory;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AcaClientTest {

  @Mock
  private AcaApisHolder acaApisHolderMock;

  @Mock
  private DebtPositionsApiApi debtPositionsApiMock;

  @Mock
  private RegistryLogger registryLoggerMock;

  private AcaClient acaClient;

  private final PodamFactory podamFactory = TestUtils.getPodamFactory();

  private static final String TEST_API_KEY = "test-api-key";
  private static final String ORGANIZATION_FISCAL_CODE = "12345678901";
  private static final String IUPD = "test-iupd";

  @BeforeEach
  void setUp() {
    acaClient = new AcaClient(acaApisHolderMock, registryLoggerMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      acaApisHolderMock,
      debtPositionsApiMock,
      registryLoggerMock
    );
  }

  @Test
  void createPosition_ShouldCallAcaApiClient() {
    PaymentPositionModel paymentPositionModel =
      configureMocks3(RegistryEventType.ACA_createPosition, null, TEST_API_KEY, ORGANIZATION_FISCAL_CODE);

    acaClient.createPosition(TEST_API_KEY, ORGANIZATION_FISCAL_CODE, paymentPositionModel);

    verify(debtPositionsApiMock, times(1))
      .createPosition(ORGANIZATION_FISCAL_CODE, paymentPositionModel, null, true);
  }

  @Test
  void updatePosition_ShouldCallAcaApiClient() {
    PaymentPositionModel paymentPositionModel =
      configureMocks4(RegistryEventType.ACA_updatePosition, null, TEST_API_KEY, ORGANIZATION_FISCAL_CODE, IUPD);

    acaClient.updatePosition(TEST_API_KEY, ORGANIZATION_FISCAL_CODE, IUPD, paymentPositionModel);

    verify(debtPositionsApiMock, times(1))
      .updatePosition(ORGANIZATION_FISCAL_CODE, IUPD, paymentPositionModel, null, true);
  }

  @Test
  void deletePosition_ShouldCallAcaApiClient() {
    PaymentPositionModel paymentPositionModel =
      configureMocks4(RegistryEventType.ACA_deletePosition, IUPD, TEST_API_KEY, ORGANIZATION_FISCAL_CODE, IUPD);

    acaClient.deletePosition(TEST_API_KEY, ORGANIZATION_FISCAL_CODE, IUPD, paymentPositionModel);

    verify(debtPositionsApiMock, times(1))
      .deletePosition(ORGANIZATION_FISCAL_CODE, IUPD, null);
  }

  private PaymentPositionModel configureMocks3(RegistryEventType registryEventType, Object request, Object arg1, Object arg2) {
    return configureMocks(registryEventType, request,
      (contextData, paymentPositionModel) ->
        RegistryLoggerTest.configureRegistryLoggerMockExecute3(
          registryLoggerMock,
          contextData,
          Objects.requireNonNullElse(request, paymentPositionModel),
          false,
          false,
          arg1, arg2, paymentPositionModel
        )
      );
  }

  private PaymentPositionModel configureMocks4(RegistryEventType registryEventType, Object request, Object arg1, Object arg2, Object arg3) {
    return configureMocks(registryEventType, request,
      (contextData, paymentPositionModel) ->
        RegistryLoggerTest.configureRegistryLoggerMockExecute4(
          registryLoggerMock,
          contextData,
          Objects.requireNonNullElse(request, paymentPositionModel),
          false,
          false,
          arg1, arg2, arg3, paymentPositionModel
        )
    );
  }

  private PaymentPositionModel configureMocks(RegistryEventType registryEventType, Object request, BiConsumer<RegistryContextData, PaymentPositionModel> mockConfigurer) {
    PaymentPositionModel paymentPositionModel = podamFactory.manufacturePojo(PaymentPositionModel.class);

    when(acaApisHolderMock.getApiClientByApiKey(TEST_API_KEY))
      .thenReturn(debtPositionsApiMock);

    String expectedIuvConcat = "";
    if (paymentPositionModel.getPaymentOption() != null) {
      expectedIuvConcat = paymentPositionModel.getPaymentOption().stream()
        .filter(Objects::nonNull)
        .map(PaymentOptionModel::getIuv)
        .collect(Collectors.joining(Utilities.IUV_SEPARATOR));
    }

    RegistryContextData contextData = RegistryContextData.builder()
      .orgFiscalCode(ORGANIZATION_FISCAL_CODE)
      .eventType(registryEventType)
      .iuv(expectedIuvConcat)
      .build();

    mockConfigurer.accept(contextData, paymentPositionModel);

    return paymentPositionModel;
  }
}
