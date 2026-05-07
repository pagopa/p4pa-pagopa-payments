package it.gov.pagopa.pu.pagopapayments.service;

import it.gov.pagopa.nodo.gpd.dto.generated.PaymentPositionModelV3;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.organization.dto.generated.Broker;
import it.gov.pagopa.pu.organization.dto.generated.BrokerApiKeys;
import it.gov.pagopa.pu.organization.dto.generated.OrganizationStationDTO;
import it.gov.pagopa.pu.pagopapayments.connector.pagopa.gpd.GpdService;
import it.gov.pagopa.pu.pagopapayments.dto.BrokerForNodoPaDTO;
import it.gov.pagopa.pu.pagopapayments.enums.Operation;
import it.gov.pagopa.pu.pagopapayments.mapper.GpdDebtPositionMapper;
import it.gov.pagopa.pu.pagopapayments.service.broker.BrokerRetrieverService;
import it.gov.pagopa.pu.pagopapayments.service.sync.gpd.GpdFacadeService;
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
  private static final Boolean FORCE_SPONTANEOUS = Boolean.FALSE;
  private final PodamFactory podamFactory = TestUtils.getPodamFactory();

  @Test
  void givenOperationCreateWhenSyncThenInvokePaCreatePositionUsingGpdKey() {
    // given
    String iud = "IUD";
    String accessToken = TestUtils.getFakeAccessToken();
    DebtPositionDTO debtPosition = podamFactory.manufacturePojo(DebtPositionDTO.class);
    OrganizationStationDTO organizationStationDTO = podamFactory.manufacturePojo(OrganizationStationDTO.class);
    PaymentPositionModelV3 model = podamFactory.manufacturePojo(PaymentPositionModelV3.class);

    BrokerForNodoPaDTO brokerForNodoPaDTO = BrokerForNodoPaDTO.builder()
      .organizationStation(organizationStationDTO)
      .brokerApiKeys(new BrokerApiKeys().gpdKey(VALID_GPD_KEY).acaKey("OTHER_KEY"))
      .broker(new Broker())
      .build();

    Mockito.when(brokerRetrieverServiceMock.getBrokerForNodoPaDTOByOrganizationId(debtPosition.getOrganizationId(), accessToken))
      .thenReturn(brokerForNodoPaDTO);

    Mockito.when(gpdDebtPositionMapperMock.mapToNewPaymentPositionModel(iud, debtPosition, organizationStationDTO.getOrgName()))
      .thenReturn(Pair.of(Operation.CREATE, model));

    // when
    gpdFacadeService.sync(iud, debtPosition, FORCE_SPONTANEOUS, accessToken);

    // then
    Mockito.verify(gpdServiceMock).paCreatePosition(
      Mockito.eq(VALID_GPD_KEY),
      Mockito.eq(organizationStationDTO.getOrgFiscalCode()),
      Mockito.same(model)
    );
    Mockito.verifyNoMoreInteractions(gpdServiceMock);
  }

  @Test
  void givenOperationUpdateWhenSyncThenInvokePaUpdatePositionUsingGpdKey() {
    // given
    String iud = "IUD";
    String accessToken = TestUtils.getFakeAccessToken();
    DebtPositionDTO debtPosition = podamFactory.manufacturePojo(DebtPositionDTO.class);
    OrganizationStationDTO organizationStationDTO = podamFactory.manufacturePojo(OrganizationStationDTO.class);
    PaymentPositionModelV3 model = podamFactory.manufacturePojo(PaymentPositionModelV3.class);

    BrokerForNodoPaDTO brokerForNodoPaDTO = BrokerForNodoPaDTO.builder()
      .organizationStation(organizationStationDTO)
      .brokerApiKeys(new BrokerApiKeys().gpdKey(VALID_GPD_KEY).acaKey("OTHER_KEY"))
      .broker(new Broker())
      .build();

    Mockito.when(brokerRetrieverServiceMock.getBrokerForNodoPaDTOByOrganizationId(debtPosition.getOrganizationId(), accessToken))
      .thenReturn(brokerForNodoPaDTO);

    Mockito.when(gpdDebtPositionMapperMock.mapToNewPaymentPositionModel(iud, debtPosition, organizationStationDTO.getOrgName()))
      .thenReturn(Pair.of(Operation.UPDATE, model));

    // when
    gpdFacadeService.sync(iud, debtPosition, FORCE_SPONTANEOUS, accessToken);

    // then
    Mockito.verify(gpdServiceMock).paUpdatePosition(
      Mockito.eq(VALID_GPD_KEY),
      Mockito.eq(organizationStationDTO.getOrgFiscalCode()),
      Mockito.eq(model.getIupd()),
      Mockito.same(model)
    );
    Mockito.verifyNoMoreInteractions(gpdServiceMock);
  }

  @Test
  void givenOperationDeleteWhenSyncThenInvokePaDeletePositionUsingGpdKey() {
    // given
    String iud = "IUD";
    String accessToken = TestUtils.getFakeAccessToken();
    DebtPositionDTO debtPosition = podamFactory.manufacturePojo(DebtPositionDTO.class);
    OrganizationStationDTO organizationStationDTO = podamFactory.manufacturePojo(OrganizationStationDTO.class);
    PaymentPositionModelV3 model = podamFactory.manufacturePojo(PaymentPositionModelV3.class);

    BrokerForNodoPaDTO brokerForNodoPaDTO = BrokerForNodoPaDTO.builder()
      .organizationStation(organizationStationDTO)
      .brokerApiKeys(new BrokerApiKeys().gpdKey(VALID_GPD_KEY).acaKey("OTHER_KEY"))
      .broker(new Broker())
      .build();

    Mockito.when(brokerRetrieverServiceMock.getBrokerForNodoPaDTOByOrganizationId(debtPosition.getOrganizationId(), accessToken))
      .thenReturn(brokerForNodoPaDTO);

    Mockito.when(gpdDebtPositionMapperMock.mapToNewPaymentPositionModel(iud, debtPosition, organizationStationDTO.getOrgName()))
      .thenReturn(Pair.of(Operation.DELETE, model));

    // when
    gpdFacadeService.sync(iud, debtPosition, FORCE_SPONTANEOUS, accessToken);

    // then
    Mockito.verify(gpdServiceMock).paDeletePosition(
      Mockito.eq(VALID_GPD_KEY),
      Mockito.eq(organizationStationDTO.getOrgFiscalCode()),
      Mockito.eq(model.getIupd()),
      Mockito.same(model)
    );
    Mockito.verifyNoMoreInteractions(gpdServiceMock);
  }

  @Test
  void givenOperationCreateWithBrokerDelegateWhenSyncGPDThenInvokePaCreateDPUsingOrgOfTransferOwner(){
    // given
    String iud = "IUD";
    String accessToken = TestUtils.getFakeAccessToken();
    String orgName = "ORG";
    String orgFiscalCode = "11111222223";
    DebtPositionDTO debtPosition = podamFactory.manufacturePojo(DebtPositionDTO.class);
    debtPosition.getPaymentOptions().getFirst().getInstallments().getFirst().getTransfers().getFirst().setFlagOwner(Boolean.TRUE);
    debtPosition.getPaymentOptions().getFirst().getInstallments().getFirst().getTransfers().getFirst().setOrgFiscalCode(orgFiscalCode);
    debtPosition.getPaymentOptions().getFirst().getInstallments().getFirst().getTransfers().getFirst().setOrgName(orgName);
    OrganizationStationDTO organizationStationDTO = podamFactory.manufacturePojo(OrganizationStationDTO.class);
    Broker broker = podamFactory.manufacturePojo(Broker.class);
    broker.setFlagDelegate(Boolean.TRUE);
    PaymentPositionModelV3 model = podamFactory.manufacturePojo(PaymentPositionModelV3.class);

    BrokerForNodoPaDTO brokerForNodoPaDTO = BrokerForNodoPaDTO.builder()
      .brokerApiKeys(new BrokerApiKeys().gpdKey(VALID_GPD_KEY).acaKey("OTHER_KEY"))
      .broker(broker)
      .organizationStation(organizationStationDTO)
      .build();

    Mockito.when(brokerRetrieverServiceMock.getBrokerForNodoPaDTOByOrganizationId(debtPosition.getOrganizationId(), accessToken))
      .thenReturn(brokerForNodoPaDTO);

    Mockito.when(gpdDebtPositionMapperMock.mapToNewPaymentPositionModel(iud, debtPosition, orgName))
      .thenReturn(Pair.of(Operation.CREATE, model));

    // when
    gpdFacadeService.sync(iud, debtPosition, FORCE_SPONTANEOUS, accessToken);

    // then
    Mockito.verify(gpdServiceMock).paCreatePosition(
      Mockito.eq(VALID_GPD_KEY),
      Mockito.eq(orgFiscalCode),
      Mockito.same(model)
    );
    Mockito.verifyNoMoreInteractions(gpdServiceMock);
  }
}

