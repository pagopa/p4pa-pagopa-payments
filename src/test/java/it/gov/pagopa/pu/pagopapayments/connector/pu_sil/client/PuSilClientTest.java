package it.gov.pagopa.pu.pagopapayments.connector.pu_sil.client;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import it.gov.pagopa.pu.pagopapayments.connector.pu_sil.config.PuSilApisHolder;
import it.gov.pagopa.pu.pagopapayments.exception.NotPayableSilActualizedAmountException;
import it.gov.pagopa.pu.pusil.controller.generated.AmountUpdatesApi;
import it.gov.pagopa.pu.pusil.dto.generated.AmountUpdatesDTO;
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
class PuSilClientTest {

  @Mock
  private PuSilApisHolder puSilApisHolderMock;
  @Mock
  private AmountUpdatesApi amountUpdatesApiMock;

  private PuSilClient puSilClient;

  @BeforeEach
  void setUp() { puSilClient = new PuSilClient(puSilApisHolderMock); }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(puSilApisHolderMock);
  }

  @Test
  void whenGetAmountUpdatesThenInvokeWithAccessToken() {
    String accessToken = "ACCESSTOKEN";
    Long orgSilServiceId = 1L;
    String nav = "NAV";
    AmountUpdatesDTO expectedResult = new AmountUpdatesDTO();

    when(puSilApisHolderMock.getAmountUpdatesApi(accessToken)).thenReturn(amountUpdatesApiMock);
    when(amountUpdatesApiMock.getAmountUpdates(orgSilServiceId, nav)).thenReturn(expectedResult);

    AmountUpdatesDTO result = puSilClient.getAmountUpdates(
      orgSilServiceId, nav, accessToken);

    assertSame(expectedResult, result);
  }

  @Test
  void givenConflictExceptionWhenGetAmountUpdatesThenNull(){
    //Given
    String accessToken = "ACCESSTOKEN";
    Long orgSilServiceId = 1L;
    String nav = "NAV";

    when(puSilApisHolderMock.getAmountUpdatesApi(accessToken)).thenReturn(amountUpdatesApiMock);
    when(amountUpdatesApiMock.getAmountUpdates(orgSilServiceId, nav))
      .thenThrow(
        HttpClientErrorException.create(HttpStatus.CONFLICT, "Conflict", null, null, null));

    // When Then
    Assertions.assertThrows(NotPayableSilActualizedAmountException.class,
      () -> puSilClient.getAmountUpdates(orgSilServiceId, nav, accessToken));
  }

}
