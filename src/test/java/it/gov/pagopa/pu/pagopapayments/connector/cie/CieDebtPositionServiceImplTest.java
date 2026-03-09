package it.gov.pagopa.pu.pagopapayments.connector.cie;

import it.gov.pagopa.pu.cie.dto.generated.DebtPositionCieRequestDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.pagopapayments.connector.auth.AuthnService;
import it.gov.pagopa.pu.pagopapayments.connector.cie.client.CieDebtPositionClient;
import it.gov.pagopa.pu.pagopapayments.util.TestUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import uk.co.jemos.podam.api.PodamFactory;

@ExtendWith(MockitoExtension.class)
class CieDebtPositionServiceImplTest {

  @Mock
  private CieDebtPositionClient cieDebtPositionClientMock;
  @Mock
  private AuthnService authnServiceMock;

  private final PodamFactory podamFactory = TestUtils.getPodamFactory();

  CieDebtPositionService cieDebtPositionService;

  @BeforeEach
  void setUp() {
    cieDebtPositionService = new CieDebtPositionServiceImpl(cieDebtPositionClientMock,authnServiceMock);
  }

  @AfterEach
  void mockitoVerify(){
    Mockito.verifyNoMoreInteractions(cieDebtPositionClientMock);
  }

  @Test
  void whenCreateDebtPositionCieThenInvokeClient() {
    String accessToken = "ACCESS_TOKEN";
    String cieOrgIpaCode = "cieOrgIpaCode";
    DebtPositionCieRequestDTO debtPositionCieRequestDTO = podamFactory.manufacturePojo(DebtPositionCieRequestDTO.class);
    DebtPositionDTO debtPositionDTO = podamFactory.manufacturePojo(DebtPositionDTO.class);
    ResponseEntity<DebtPositionDTO> expectedResult = ResponseEntity.ok().header("X-Workflow-Id", "workflow-id").body(debtPositionDTO);

    Mockito.when(authnServiceMock.getAccessToken(cieOrgIpaCode)).thenReturn(accessToken);
    Mockito.when(cieDebtPositionClientMock.createDebtPositionCie(debtPositionCieRequestDTO,accessToken)).thenReturn(expectedResult);

    Pair<DebtPositionDTO, String> result = cieDebtPositionService.createDebtPositionCie(debtPositionCieRequestDTO, cieOrgIpaCode);

    Assertions.assertNotNull(result);
    Assertions.assertSame(expectedResult.getBody(), result.getLeft());
    Assertions.assertSame("workflow-id", result.getRight());
  }
}
