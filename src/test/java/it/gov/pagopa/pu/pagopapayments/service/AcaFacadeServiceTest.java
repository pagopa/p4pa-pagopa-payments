package it.gov.pagopa.pu.pagopapayments.service;

import it.gov.pagopa.nodo.gpd.dto.generated.PaymentPositionModelV3;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionOrigin;
import it.gov.pagopa.pu.organization.dto.generated.Broker;
import it.gov.pagopa.pu.organization.dto.generated.BrokerApiKeys;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.pagopapayments.connector.pagopa.gpd.GpdService;
import it.gov.pagopa.pu.pagopapayments.dto.BrokerForNodoPaDTO;
import it.gov.pagopa.pu.pagopapayments.enums.Operation;
import it.gov.pagopa.pu.pagopapayments.mapper.GpdDebtPositionMapper;
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
  private GpdService acaServiceWrapperMock;
  @Mock
  private GpdDebtPositionMapper gpdDebtPositionMapperMock;
  @Mock
  private BrokerRetrieverService brokerRetrieverServiceMock;

  @InjectMocks
  private AcaFacadeService acaFacadeService;

  private static final String VALID_ACA_KEY = "validAcaKey";
  private final PodamFactory podamFactory = TestUtils.getPodamFactory();

  @Test
  void givenValidDebtPositionWhenSyncThenUseAcaKey() {
    //given
    String iud = "IUD";
    DebtPositionDTO debtPosition = podamFactory.manufacturePojo(DebtPositionDTO.class);
    debtPosition.setDebtPositionOrigin(DebtPositionOrigin.ORDINARY);

    Organization organization = podamFactory.manufacturePojo(Organization.class);
    PaymentPositionModelV3 model = podamFactory.manufacturePojo(PaymentPositionModelV3.class);

    BrokerForNodoPaDTO brokerForNodoPaDTO = BrokerForNodoPaDTO.builder()
      .organization(organization)
      .brokerApiKeys(new BrokerApiKeys().acaKey(VALID_ACA_KEY).gpdKey("OTHER_KEY"))
      .broker(new Broker())
      .build();

    Mockito.when(brokerRetrieverServiceMock.getBrokerForNodoPaDTOByOrganizationId(debtPosition.getOrganizationId(), TestUtils.getFakeAccessToken()))
      .thenReturn(brokerForNodoPaDTO);
    Mockito.when(gpdDebtPositionMapperMock.mapToNewPaymentPositionModel(iud, debtPosition, organization))
      .thenReturn(Pair.of(Operation.CREATE, model));

    //when
    acaFacadeService.sync(iud, debtPosition, TestUtils.getFakeAccessToken());

    //verify
    Mockito.verify(acaServiceWrapperMock).paCreatePosition(
      Mockito.eq(VALID_ACA_KEY),
      Mockito.eq(organization.getOrgFiscalCode()),
      Mockito.same(model)
    );
  }

  @Test
  void givenExcludedOriginWhenSyncThenSkipExecution() {
    //given
    String iud = "IUD";
    DebtPositionDTO debtPosition = podamFactory.manufacturePojo(DebtPositionDTO.class);
    debtPosition.setDebtPositionOrigin(DebtPositionOrigin.SPONTANEOUS);

    //when
    acaFacadeService.sync(iud, debtPosition, TestUtils.getFakeAccessToken());

    //verify
    Mockito.verifyNoInteractions(brokerRetrieverServiceMock, gpdDebtPositionMapperMock, acaServiceWrapperMock);
  }
}
