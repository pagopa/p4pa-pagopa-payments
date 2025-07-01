package it.gov.pagopa.pu.pagopapayments.connector.pu_sil;

import static org.junit.jupiter.api.Assertions.*;

import it.gov.pagopa.pu.pagopapayments.connector.pu_sil.client.PuSilClient;
import it.gov.pagopa.pu.pusil.dto.generated.AmountUpdatesDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PuSilServiceImplTest {

  @Mock
  private PuSilClient puSilClientMock;
  private PuSilService puSilService;

  @BeforeEach
  void setUp() {
    puSilService = new PuSilServiceImpl(puSilClientMock);
  }

  @Test
  void whenGetAmountUpdatesThenInvokeClient(){
    Long orgSilServiceId = 1L;
    String nav = "NAV";
    String accessToken = "access_token";
    AmountUpdatesDTO expectedResponse = new AmountUpdatesDTO();

    Mockito.when(puSilClientMock.getAmountUpdates(orgSilServiceId, nav, accessToken))
      .thenReturn(expectedResponse);

    AmountUpdatesDTO response = puSilService
      .getAmountUpdates(orgSilServiceId, nav, accessToken);

    assertSame(expectedResponse,response);
  }

}
