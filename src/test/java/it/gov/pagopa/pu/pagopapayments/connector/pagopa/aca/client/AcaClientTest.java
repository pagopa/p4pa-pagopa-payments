package it.gov.pagopa.pu.pagopapayments.connector.pagopa.aca.client;

import it.gov.pagopa.nodo.pacreateposition.controller.generated.AcaApi;
import it.gov.pagopa.nodo.pacreateposition.dto.generated.DebtPositionResponse;
import it.gov.pagopa.nodo.pacreateposition.dto.generated.NewDebtPositionRequest;
import it.gov.pagopa.pu.pagopapayments.connector.pagopa.aca.config.AcaApisHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AcaClientTest {

  @Mock
  private AcaApisHolder acaApisHolderMock;
  @Mock
  private AcaApi acaApiMock;

  private AcaClient acaClient;

  @BeforeEach
  void setUp() {
    acaClient = new AcaClient(acaApisHolderMock);
  }

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(
      acaApisHolderMock,
      acaApiMock
    );
  }

  @Test
  void givenValidDebtPositionWhenPaCreatePositionThenOk(){
    //given
    String apiKey = "apiKey";
    String segregatioCode = "01";
    NewDebtPositionRequest request = new NewDebtPositionRequest();
    DebtPositionResponse expectedResponse = new DebtPositionResponse();

    Mockito.when(acaApisHolderMock.getAcaApiClientByApiKey(apiKey))
      .thenReturn(acaApiMock);
    Mockito.when(acaApiMock.newDebtPosition(Mockito.same(request), Mockito.same(segregatioCode)))
      .thenReturn(expectedResponse);

    //when
    DebtPositionResponse response = acaClient.paCreatePosition(request, apiKey, segregatioCode);

    //verify
    Assertions.assertSame(expectedResponse, response);
  }

}
