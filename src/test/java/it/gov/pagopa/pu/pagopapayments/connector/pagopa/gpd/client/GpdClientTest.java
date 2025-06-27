package it.gov.pagopa.pu.pagopapayments.connector.pagopa.gpd.client;

import it.gov.pagopa.nodo.gpd.controller.generated.DebtPositionsApiApi;
import it.gov.pagopa.nodo.gpd.dto.generated.PaymentPositionModel;
import it.gov.pagopa.pu.pagopapayments.connector.pagopa.gpd.config.GpdApisHolder;
import it.gov.pagopa.pu.pagopapayments.registry.RegistryContextData;
import it.gov.pagopa.pu.pagopapayments.registry.RegistryLogger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GpdClientTest {

  @Mock
  private GpdApisHolder gpdApisHolderMock;
  @Mock
  private RegistryLogger registryLoggerMock;

  @Mock
  private DebtPositionsApiApi debtPositionsApiMock;

  private GpdClient gpdClient;

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
      debtPositionsApiMock
    );
  }

  @Test
  void createPosition_ShouldCallGpdApiClient() {
    PaymentPositionModel paymentPositionModel = new PaymentPositionModel();

    when(registryLoggerMock.execute(any(RegistryContextData.class), any(PaymentPositionModel.class), any(), isNull()))
        .thenReturn(paymentPositionModel);
    when(gpdApisHolderMock.getGpdApiClientByApiKey(TEST_API_KEY)).thenReturn(debtPositionsApiMock);

    gpdClient.createPosition(TEST_API_KEY, ORGANIZATION_FISCAL_CODE, paymentPositionModel);

    verify(debtPositionsApiMock, times(1)).createPosition(ORGANIZATION_FISCAL_CODE, paymentPositionModel, null, TO_PUBLISH);
  }

  @Test
  void updatePosition_ShouldCallGpdApiClient() {
    PaymentPositionModel paymentPositionModel = new PaymentPositionModel();

    when(registryLoggerMock.execute(any(RegistryContextData.class), any(PaymentPositionModel.class), any(), isNull()))
      .thenReturn(paymentPositionModel);
    when(gpdApisHolderMock.getGpdApiClientByApiKey(TEST_API_KEY)).thenReturn(debtPositionsApiMock);

    gpdClient.updatePosition(TEST_API_KEY, ORGANIZATION_FISCAL_CODE, IUPD, paymentPositionModel);

    verify(debtPositionsApiMock, times(1)).updatePosition(ORGANIZATION_FISCAL_CODE, IUPD, paymentPositionModel, null, TO_PUBLISH);
  }

  @Test
  void deletePosition_ShouldCallGpdApiClient() {
    when(registryLoggerMock.execute(any(RegistryContextData.class), any(String.class), any(), isNull()))
      .thenReturn("success");
    when(gpdApisHolderMock.getGpdApiClientByApiKey(TEST_API_KEY)).thenReturn(debtPositionsApiMock);

    gpdClient.deletePosition(TEST_API_KEY, ORGANIZATION_FISCAL_CODE, IUPD);

    verify(debtPositionsApiMock, times(1)).deletePosition(ORGANIZATION_FISCAL_CODE, IUPD, null);
  }
}
