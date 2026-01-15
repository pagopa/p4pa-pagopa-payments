package it.gov.pagopa.pu.pagopapayments.service;

import it.gov.pagopa.nodo.gpd.dto.generated.PaymentPositionModelV3;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.organization.dto.generated.Broker;
import it.gov.pagopa.pu.organization.dto.generated.BrokerApiKeys;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.pagopapayments.connector.pagopa.gpd.GpdService;
import it.gov.pagopa.pu.pagopapayments.dto.BrokerForNodoPaDTO;
import it.gov.pagopa.pu.pagopapayments.enums.Operation;
import it.gov.pagopa.pu.pagopapayments.mapper.GpdDebtPositionMapper;
import it.gov.pagopa.pu.pagopapayments.service.broker.BrokerRetrieverService;
import it.gov.pagopa.pu.pagopapayments.service.gpd.GpdFacadeService;
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
class GpdFacadeServiceTest {
  @Mock
  private GpdService gpdServiceMock;
  @Mock
  private GpdDebtPositionMapper gpdDebtPositionMapperMock;
  @Mock
  private BrokerRetrieverService brokerRetrieverServiceMock;

  @InjectMocks
  private GpdFacadeService gpdFacadeService;

  private static final String VALID_GPD_KEY = "validGpdKey";
  private final PodamFactory podamFactory = TestUtils.getPodamFactory();

  @Test
  void givenValidDebtPositionWhenSyncThenUseGpdKey() {
    //given
    String iud = "IUD";
    DebtPositionDTO debtPosition = podamFactory.manufacturePojo(DebtPositionDTO.class);
    Organization organization = podamFactory.manufacturePojo(Organization.class);
    PaymentPositionModelV3 model = podamFactory.manufacturePojo(PaymentPositionModelV3.class);

    BrokerForNodoPaDTO brokerForNodoPaDTO = BrokerForNodoPaDTO.builder()
      .organization(organization)
      .brokerApiKeys(new BrokerApiKeys().gpdKey(VALID_GPD_KEY).acaKey("OTHER_KEY"))
      .broker(new Broker())
      .build();

    Mockito.when(brokerRetrieverServiceMock.getBrokerForNodoPaDTOByOrganizationId(debtPosition.getOrganizationId(), TestUtils.getFakeAccessToken()))
      .thenReturn(brokerForNodoPaDTO);
    Mockito.when(gpdDebtPositionMapperMock.mapToNewPaymentPositionModel(iud, debtPosition, organization))
      .thenReturn(Pair.of(Operation.CREATE, model));

    //when
    gpdFacadeService.sync(iud, debtPosition, TestUtils.getFakeAccessToken());

    //verify
    Mockito.verify(gpdServiceMock).paCreatePosition(
      Mockito.eq(VALID_GPD_KEY),
      Mockito.eq(organization.getOrgFiscalCode()),
      Mockito.same(model)
    );
  }
}
