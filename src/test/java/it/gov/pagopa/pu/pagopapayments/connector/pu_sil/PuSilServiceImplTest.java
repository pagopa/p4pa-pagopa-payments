package it.gov.pagopa.pu.pagopapayments.connector.pu_sil;

import it.gov.pagopa.pu.pagopapayments.connector.pu_sil.client.PuSilClient;
import it.gov.pagopa.pu.pusil.dto.generated.ActualizationResultDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertSame;

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
  void whenActualizeThenInvokeClient(){
    Long orgSilServiceId = 1L;
    String nav = "NAV";
    String accessToken = "access_token";
    ActualizationResultDTO expectedResponse = new ActualizationResultDTO();

    Mockito.when(puSilClientMock.actualize(orgSilServiceId, nav, accessToken))
      .thenReturn(expectedResponse);

    ActualizationResultDTO response = puSilService
      .actualize(orgSilServiceId, nav, accessToken);

    assertSame(expectedResponse,response);
  }

}
