package it.gov.pagopa.pu.pagopapayments.connector.pagopa.gpd.client;

import it.gov.pagopa.nodo.gpd.controller.generated.DebtPositionsApiInstallmentsAndPaymentOptionsManagerApi;
import it.gov.pagopa.nodo.gpd.dto.generated.InstallmentModel;
import it.gov.pagopa.nodo.gpd.dto.generated.PaymentPositionModelV3;
import it.gov.pagopa.pu.pagopapayments.connector.pagopa.gpd.config.GpdApisHolder;
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
import java.util.stream.Collectors;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GpdClientTest {

  @Mock
  private GpdApisHolder gpdApisHolderMock;
  @Mock
  private DebtPositionsApiInstallmentsAndPaymentOptionsManagerApi debtPositionsApiMock;
  @Mock
  private RegistryLogger registryLoggerMock;

  private GpdClient gpdClient;

  private final PodamFactory podamFactory = TestUtils.getPodamFactory();

  private static final String TEST_API_KEY = "test-api-key";
  private static final String ORGANIZATION_FISCAL_CODE = "12345678901";
  private static final String IUPD = "test-iupd";
  private static final Boolean TO_PUBLISH = true;

  @BeforeEach
  void setUp() {
    gpdClient = new GpdClient(gpdApisHolderMock, registryLoggerMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      gpdApisHolderMock,
      debtPositionsApiMock,
      registryLoggerMock
    );
  }

  @Test
  void createPosition_ShouldCallGpdApiClient() {
    PaymentPositionModelV3 paymentPositionModel = configureMocks(RegistryEventType.GPD_createPosition, null);

    gpdClient.createPosition(TEST_API_KEY, ORGANIZATION_FISCAL_CODE, paymentPositionModel);

    verify(debtPositionsApiMock, times(1)).createPosition(ORGANIZATION_FISCAL_CODE, TO_PUBLISH, null,  paymentPositionModel);
  }

  @Test
  void updatePosition_ShouldCallGpdApiClient() {
    PaymentPositionModelV3 paymentPositionModel = configureMocks(RegistryEventType.GPD_updatePosition, null);

    gpdClient.updatePosition(TEST_API_KEY, ORGANIZATION_FISCAL_CODE, IUPD, paymentPositionModel);

    verify(debtPositionsApiMock, times(1)).updatePosition(ORGANIZATION_FISCAL_CODE, IUPD, TO_PUBLISH, null, paymentPositionModel);
  }

  @Test
  void deletePosition_ShouldCallGpdApiClient() {
    PaymentPositionModelV3 paymentPositionModel = configureMocks(RegistryEventType.GPD_deletePosition, IUPD);

    gpdClient.deletePosition(TEST_API_KEY, ORGANIZATION_FISCAL_CODE, IUPD, paymentPositionModel);

    verify(debtPositionsApiMock, times(1)).deletePosition(ORGANIZATION_FISCAL_CODE, IUPD, null);
  }

  private PaymentPositionModelV3 configureMocks(RegistryEventType registryEventType, Object request) {
    PaymentPositionModelV3 paymentPositionModel = podamFactory.manufacturePojo(PaymentPositionModelV3.class);

    when(gpdApisHolderMock.getDebtPositionsApiInstallmentsAndPaymentOptionsManagerApi(TEST_API_KEY)).thenReturn(debtPositionsApiMock);

    RegistryContextData contextData = RegistryContextData.builder()
      .orgFiscalCode(ORGANIZATION_FISCAL_CODE)
      .eventType(registryEventType)
      .iuv(
        paymentPositionModel.getPaymentOption().stream()
          .flatMap(po -> po.getInstallments().stream())
          .map(InstallmentModel::getIuv)
          .collect(Collectors.joining(Utilities.IUV_SEPARATOR)))
      .build();
    RegistryLoggerTest.configureRegistryLoggerMock(registryLoggerMock, contextData, Objects.requireNonNullElse(request, paymentPositionModel), false, false);

    return paymentPositionModel;
  }
}
