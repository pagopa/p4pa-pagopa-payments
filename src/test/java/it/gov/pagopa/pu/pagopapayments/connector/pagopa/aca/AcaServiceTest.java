package it.gov.pagopa.pu.pagopapayments.connector.pagopa.aca;

import it.gov.pagopa.nodo.pacreateposition.dto.generated.DebtPositionResponse;
import it.gov.pagopa.nodo.pacreateposition.dto.generated.NewDebtPositionRequest;
import it.gov.pagopa.pu.pagopapayments.connector.pagopa.aca.client.AcaClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AcaServiceTest {

  @Mock
  private AcaClient clientMock;

  private AcaService service;

  @BeforeEach
  void init(){
    service = new AcaServiceImpl(clientMock);
  }

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(clientMock);
  }

  @Test
  void whenThenInvokeClient(){
    // Given
    NewDebtPositionRequest request = new NewDebtPositionRequest();
    String acaApiKey = "ACAAPIKEY";
    String segregationCode = "SEGREGATIONCODE";
    DebtPositionResponse expectedResult = new DebtPositionResponse();

    Mockito.when(clientMock.paCreatePosition(Mockito.same(request), Mockito.same(acaApiKey), Mockito.same(segregationCode)))
      .thenReturn(expectedResult);

    //When
    DebtPositionResponse result = service.paCreatePosition(request, acaApiKey, segregationCode);

    // Then
    Assertions.assertSame(expectedResult, result);
  }
}
