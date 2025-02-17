package it.gov.pagopa.pu.pagopapayments.service;

import it.gov.pagopa.nodo.pacreateposition.dto.generated.NewDebtPositionRequest;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.organization.dto.generated.BrokerApiKeys;
import it.gov.pagopa.pu.pagopapayments.connector.pagopa.aca.AcaService;
import it.gov.pagopa.pu.pagopapayments.mapper.AcaDebtPositionMapper;
import it.gov.pagopa.pu.pagopapayments.service.aca.AcaFacadeService;
import it.gov.pagopa.pu.pagopapayments.service.broker.BrokerRetrieverService;
import it.gov.pagopa.pu.pagopapayments.util.TestUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.jemos.podam.api.PodamFactory;

@ExtendWith(MockitoExtension.class)
class AcaFacadeServiceTest {
  @Mock
  private AcaService acaServiceMock;
  @Mock
  private AcaDebtPositionMapper acaDebtPositionMapperMock;
  @Mock
  private BrokerRetrieverService brokerRetrieverServiceMock;

  @InjectMocks
  private AcaFacadeService acaFacadeService;

  private static final String VALID_ACA_KEY = "validAcaKey";
  private static final String VALID_SEGREGATION_CODE = "01";
  private static final BrokerApiKeys VALID_API_KEYS = new BrokerApiKeys()
    .acaKey(VALID_ACA_KEY)
    .syncKey("validSyncKey");

  private final PodamFactory podamFactory = TestUtils.getPodamFactory();

  @Test
  void givenValidDebtPositionWhenSyncThenOk() {
    //given
    DebtPositionDTO debtPosition = podamFactory.manufacturePojo(DebtPositionDTO.class);
    Pair<AcaDebtPositionMapper.OPERATION, NewDebtPositionRequest> newDebtPositionRequestAndOperation = Pair.of(AcaDebtPositionMapper.OPERATION.DELETE, podamFactory.manufacturePojo(NewDebtPositionRequest.class));

    Mockito.when(acaDebtPositionMapperMock.mapToNewDebtPositionRequest("IUD", debtPosition)).thenReturn(newDebtPositionRequestAndOperation);
    Mockito.when(brokerRetrieverServiceMock.getBrokerApiKeyAndSegregationCodesByOrganizationId(debtPosition.getOrganizationId(), TestUtils.getFakeAccessToken())).thenReturn(Pair.of(VALID_API_KEYS, VALID_SEGREGATION_CODE));
    //when
    acaFacadeService.sync("IUD", debtPosition, TestUtils.getFakeAccessToken());
    //verify
    Mockito.verify(acaDebtPositionMapperMock, Mockito.times(1)).mapToNewDebtPositionRequest("IUD", debtPosition);
    Mockito.verify(brokerRetrieverServiceMock, Mockito.times(1)).getBrokerApiKeyAndSegregationCodesByOrganizationId(debtPosition.getOrganizationId(), TestUtils.getFakeAccessToken());
    Mockito.verify(acaServiceMock, Mockito.times(1)).paCreatePosition(newDebtPositionRequestAndOperation.getRight(), VALID_ACA_KEY, VALID_SEGREGATION_CODE);
  }
}
