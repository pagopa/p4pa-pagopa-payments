package it.gov.pagopa.pu.pagopapayments.connector.debtpositions.client;

import it.gov.pagopa.pu.debtpositions.controller.generated.SpontaneousFormEntityControllerApi;
import it.gov.pagopa.pu.debtpositions.dto.generated.SpontaneousForm;
import it.gov.pagopa.pu.pagopapayments.connector.debtpositions.config.DebtPositionsApisHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

@ExtendWith(MockitoExtension.class)
class SpontaneousFormClientTest {

  @Mock
  private DebtPositionsApisHolder apisHolderMock;
  @Mock
  private SpontaneousFormEntityControllerApi spontaneousFormEntityControllerApiMock;


  private SpontaneousFormClient spontaneousFormClient;

  @BeforeEach
  void setUp() {
    spontaneousFormClient = new SpontaneousFormClient(apisHolderMock);
  }

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(
      apisHolderMock,
      spontaneousFormEntityControllerApiMock
      );
  }

  @Test
  void whenGetSpontaneousFormThenInvokeApi(){
    //Given
    String accessToken = "ACCESSTOKEN";
    Long spontaneousFormId = 1L;
    SpontaneousForm expectedResult = new SpontaneousForm();

    Mockito.when(apisHolderMock.getSpontaneousFormEntityControllerApi(accessToken))
      .thenReturn(spontaneousFormEntityControllerApiMock);
    Mockito.when(spontaneousFormEntityControllerApiMock.crudGetSpontaneousform(spontaneousFormId+""))
      .thenReturn(expectedResult);

    // When
    SpontaneousForm result = spontaneousFormClient.getSpontaneousForm(spontaneousFormId, accessToken);

    // Then
    Assertions.assertSame(expectedResult, result);
  }

  @Test
  void givenNotExistentSpontaneousFormWhenGetSpontaneousFormThenNull(){
    //Given
    String accessToken = "ACCESSTOKEN";
    Long spontaneousFormId = 1L;

    Mockito.when(apisHolderMock.getSpontaneousFormEntityControllerApi(accessToken))
      .thenReturn(spontaneousFormEntityControllerApiMock);
    Mockito.when(spontaneousFormEntityControllerApiMock.crudGetSpontaneousform(spontaneousFormId+""))
      .thenThrow(HttpClientErrorException.create(HttpStatus.NOT_FOUND, "NotFound", null, null, null));

    // When
    SpontaneousForm result = spontaneousFormClient.getSpontaneousForm(spontaneousFormId, accessToken);

    // Then
    Assertions.assertNull(result);
  }
}
