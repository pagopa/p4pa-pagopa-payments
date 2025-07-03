package it.gov.pagopa.pu.pagopapayments.connector.pu_sil.client;

import it.gov.pagopa.pu.pagopapayments.connector.pu_sil.config.PuSilApisHolder;
import it.gov.pagopa.pu.pagopapayments.exception.NotPayableSilActualizedAmountException;
import it.gov.pagopa.pu.pusil.controller.generated.ActualizationApi;
import it.gov.pagopa.pu.pusil.dto.generated.ActualizationResultDTO;
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

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PuSilClientTest {

  @Mock
  private PuSilApisHolder puSilApisHolderMock;
  @Mock
  private ActualizationApi actualizationApiMock;

  private PuSilClient puSilClient;

  @BeforeEach
  void setUp() { puSilClient = new PuSilClient(puSilApisHolderMock); }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(puSilApisHolderMock);
  }

  @Test
  void whenActualizeThenInvokeWithAccessToken() {
    String accessToken = "ACCESSTOKEN";
    Long orgSilServiceId = 1L;
    String nav = "NAV";
    ActualizationResultDTO expectedResult = new ActualizationResultDTO();

    when(puSilApisHolderMock.getActualizationApi(accessToken)).thenReturn(actualizationApiMock);
    when(actualizationApiMock.actualize(orgSilServiceId, nav)).thenReturn(expectedResult);

    ActualizationResultDTO result = puSilClient.actualize(
      orgSilServiceId, nav, accessToken);

    assertSame(expectedResult, result);
  }

  @Test
  void givenConflictExceptionWhenActualizeThenNull(){
    //Given
    String accessToken = "ACCESSTOKEN";
    Long orgSilServiceId = 1L;
    String nav = "NAV";

    when(puSilApisHolderMock.getActualizationApi(accessToken)).thenReturn(actualizationApiMock);
    when(actualizationApiMock.actualize(orgSilServiceId, nav))
      .thenThrow(
        HttpClientErrorException.create(HttpStatus.CONFLICT, "Conflict", null, null, null));

    // When Then
    Assertions.assertThrows(NotPayableSilActualizedAmountException.class,
      () -> puSilClient.actualize(orgSilServiceId, nav, accessToken));
  }

}
