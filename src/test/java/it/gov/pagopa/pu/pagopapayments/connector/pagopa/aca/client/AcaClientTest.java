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
      configureMocks(RegistryEventType.ACA_createPosition, null);

    acaClient.createPosition(TEST_API_KEY, ORGANIZATION_FISCAL_CODE, paymentPositionModel);

    verify(debtPositionsApiMock, times(1))
      .createPosition(ORGANIZATION_FISCAL_CODE, paymentPositionModel, null, true);
  }

  @Test
  void updatePosition_ShouldCallAcaApiClient() {
    PaymentPositionModel paymentPositionModel =
      configureMocks(RegistryEventType.ACA_updatePosition, null);

    acaClient.updatePosition(TEST_API_KEY, ORGANIZATION_FISCAL_CODE, IUPD, paymentPositionModel);

    verify(debtPositionsApiMock, times(1))
      .updatePosition(ORGANIZATION_FISCAL_CODE, IUPD, paymentPositionModel, null, true);
  }

  @Test
  void deletePosition_ShouldCallAcaApiClient() {
    PaymentPositionModel paymentPositionModel =
      configureMocks(RegistryEventType.ACA_deletePosition, IUPD);

    acaClient.deletePosition(TEST_API_KEY, ORGANIZATION_FISCAL_CODE, IUPD, paymentPositionModel);

    verify(debtPositionsApiMock, times(1))
      .deletePosition(ORGANIZATION_FISCAL_CODE, IUPD, null);
  }

  private PaymentPositionModel configureMocks(RegistryEventType registryEventType, Object request) {
    PaymentPositionModel paymentPositionModel = podamFactory.manufacturePojo(PaymentPositionModel.class);

    when(acaApisHolderMock.getDebtPositionsApi(TEST_API_KEY))
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

    RegistryLoggerTest.configureRegistryLoggerMock(
      registryLoggerMock,
      contextData,
      Objects.requireNonNullElse(request, paymentPositionModel),
      false,
      false
    );

    return paymentPositionModel;
  }
}
