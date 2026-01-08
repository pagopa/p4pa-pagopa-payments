package it.gov.pagopa.pu.pagopapayments.service;

import it.gov.pagopa.nodo.gpd.dto.generated.PaymentPositionModelV3;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.organization.dto.generated.BrokerApiKeys;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.pagopapayments.connector.pagopa.gpd.GpdService;
import it.gov.pagopa.pu.pagopapayments.dto.BrokerForNodoPaDTO;
import it.gov.pagopa.pu.pagopapayments.enums.Operation;
import it.gov.pagopa.pu.pagopapayments.mapper.GpdDebtPositionMapper;
import it.gov.pagopa.pu.pagopapayments.service.aca_gpd.AbstractPaymentPositionFacadeService;
import it.gov.pagopa.pu.pagopapayments.service.broker.BrokerRetrieverService;
import it.gov.pagopa.pu.pagopapayments.util.TestUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.jemos.podam.api.PodamFactory;

@ExtendWith(MockitoExtension.class)
class AbstractPaymentPositionFacadeServiceTest {

  @Mock
  private GpdService gpdServiceMock;
  @Mock
  private GpdDebtPositionMapper gpdDebtPositionMapperMock;
  @Mock
  private BrokerRetrieverService brokerRetrieverServiceMock;

  private AbstractPaymentPositionFacadeService abstractService;

  private final PodamFactory podamFactory = TestUtils.getPodamFactory();
  private static final String TEST_KEY = "TEST_KEY";

  @BeforeEach
  void setUp() {
    abstractService = new AbstractPaymentPositionFacadeService(
      gpdServiceMock,
      gpdDebtPositionMapperMock,
      brokerRetrieverServiceMock,
      "TEST_SERVICE"
    ) {
      @Override
      protected String getApiKey(BrokerApiKeys keys) {
        return TEST_KEY;
      }
    };
  }

  @Test
  void givenOperationCreateWhenSyncThenInvokePaCreatePosition() {
    //given
    String iud = "IUD";
    String accessToken = "TOKEN";
    DebtPositionDTO debtPosition = podamFactory.manufacturePojo(DebtPositionDTO.class);
    Organization organization = podamFactory.manufacturePojo(Organization.class);
    PaymentPositionModelV3 model = podamFactory.manufacturePojo(PaymentPositionModelV3.class);
    BrokerForNodoPaDTO brokerInfo = podamFactory.manufacturePojo(BrokerForNodoPaDTO.class);
    brokerInfo.setOrganization(organization);

    Mockito.when(brokerRetrieverServiceMock.getBrokerForNodoPaDTOByOrganizationId(debtPosition.getOrganizationId(), accessToken))
      .thenReturn(brokerInfo);
    Mockito.when(gpdDebtPositionMapperMock.mapToNewPaymentPositionModel(iud, debtPosition, organization))
      .thenReturn(Pair.of(Operation.CREATE, model));

    //when
    abstractService.sync(iud, debtPosition, accessToken);

    //then
    Mockito.verify(gpdServiceMock).paCreatePosition(TEST_KEY, organization.getOrgFiscalCode(), model);
  }

  @Test
  void givenOperationUpdateWhenSyncThenInvokePaUpdatePosition() {
    //given
    String iud = "IUD";
    String accessToken = "TOKEN";
    DebtPositionDTO debtPosition = podamFactory.manufacturePojo(DebtPositionDTO.class);
    Organization organization = podamFactory.manufacturePojo(Organization.class);
    PaymentPositionModelV3 model = podamFactory.manufacturePojo(PaymentPositionModelV3.class);
    BrokerForNodoPaDTO brokerInfo = podamFactory.manufacturePojo(BrokerForNodoPaDTO.class);
    brokerInfo.setOrganization(organization);

    Mockito.when(brokerRetrieverServiceMock.getBrokerForNodoPaDTOByOrganizationId(debtPosition.getOrganizationId(), accessToken))
      .thenReturn(brokerInfo);
    Mockito.when(gpdDebtPositionMapperMock.mapToNewPaymentPositionModel(iud, debtPosition, organization))
      .thenReturn(Pair.of(Operation.UPDATE, model));

    //when
    abstractService.sync(iud, debtPosition, accessToken);

    //then
    Mockito.verify(gpdServiceMock).paUpdatePosition(TEST_KEY, organization.getOrgFiscalCode(), model.getIupd(), model);
  }

  @Test
  void givenOperationDeleteWhenSyncThenInvokePaDeletePosition() {
    //given
    String iud = "IUD";
    String accessToken = "TOKEN";
    DebtPositionDTO debtPosition = podamFactory.manufacturePojo(DebtPositionDTO.class);
    Organization organization = podamFactory.manufacturePojo(Organization.class);
    PaymentPositionModelV3 model = podamFactory.manufacturePojo(PaymentPositionModelV3.class);
    BrokerForNodoPaDTO brokerInfo = podamFactory.manufacturePojo(BrokerForNodoPaDTO.class);
    brokerInfo.setOrganization(organization);

    Mockito.when(brokerRetrieverServiceMock.getBrokerForNodoPaDTOByOrganizationId(debtPosition.getOrganizationId(), accessToken))
      .thenReturn(brokerInfo);
    Mockito.when(gpdDebtPositionMapperMock.mapToNewPaymentPositionModel(iud, debtPosition, organization))
      .thenReturn(Pair.of(Operation.DELETE, model));

    //when
    abstractService.sync(iud, debtPosition, accessToken);

    //then
    Mockito.verify(gpdServiceMock).paDeletePosition(TEST_KEY, organization.getOrgFiscalCode(), model.getIupd(), model);
  }
}
