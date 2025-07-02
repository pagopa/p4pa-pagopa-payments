package it.gov.pagopa.pu.pagopapayments.connector.pagopa.aca.client;

import it.gov.pagopa.nodo.pacreateposition.controller.generated.AcaApi;
import it.gov.pagopa.nodo.pacreateposition.dto.generated.DebtPositionResponse;
import it.gov.pagopa.nodo.pacreateposition.dto.generated.NewDebtPositionRequest;
import it.gov.pagopa.pu.pagopapayments.connector.pagopa.aca.config.AcaApisHolder;
import it.gov.pagopa.pu.pagopapayments.registry.RegistryContextData;
import it.gov.pagopa.pu.pagopapayments.registry.RegistryEventType;
import it.gov.pagopa.pu.pagopapayments.registry.RegistryLogger;
import it.gov.pagopa.pu.pagopapayments.registry.RegistryLoggerTest;
import it.gov.pagopa.pu.pagopapayments.util.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.jemos.podam.api.PodamFactory;

@ExtendWith(MockitoExtension.class)
class AcaClientTest {

  @Mock
  private AcaApisHolder acaApisHolderMock;
  @Mock
  private AcaApi acaApiMock;
  @Mock
  private RegistryLogger registryLoggerMock;

  private AcaClient acaClient;

  private final PodamFactory podamFactory = TestUtils.getPodamFactory();

  @BeforeEach
  void setUp() {
    acaClient = new AcaClient(acaApisHolderMock, registryLoggerMock);
  }

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(
      acaApisHolderMock,
      acaApiMock,
      registryLoggerMock
    );
  }

  @Test
  void givenValidDebtPositionWhenPaCreatePositionThenOk(){
    //given
    String apiKey = "apiKey";
    String segregationCode = "01";
    NewDebtPositionRequest request = podamFactory.manufacturePojo(NewDebtPositionRequest.class);
    DebtPositionResponse expectedResponse = new DebtPositionResponse();

    RegistryContextData contextData = RegistryContextData.builder()
      .orgFiscalCode(request.getPaFiscalCode())
      .eventType(RegistryEventType.newDebtPosition)
      .iuv(request.getIuv())
      .build();
    RegistryLoggerTest.configureRegistryLoggerMock(registryLoggerMock, contextData, request, false, false);

    Mockito.when(acaApisHolderMock.getAcaApiClientByApiKey(apiKey))
      .thenReturn(acaApiMock);
    Mockito.when(acaApiMock.newDebtPosition(Mockito.same(request), Mockito.same(segregationCode)))
      .thenReturn(expectedResponse);

    //when
    DebtPositionResponse response = acaClient.paCreatePosition(request, apiKey, segregationCode);

    //verify
    Assertions.assertSame(expectedResponse, response);
  }

}
