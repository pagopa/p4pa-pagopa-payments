package it.gov.pagopa.pu.pagopapayments.service;

import it.gov.pagopa.pu.aca.gpd.v1.dto.generated.PaymentPositionModel;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionOrigin;
import it.gov.pagopa.pu.organization.dto.generated.Broker;
import it.gov.pagopa.pu.organization.dto.generated.BrokerApiKeys;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.pagopapayments.connector.pagopa.aca.AcaService;
import it.gov.pagopa.pu.pagopapayments.dto.BrokerForNodoPaDTO;
import it.gov.pagopa.pu.pagopapayments.enums.Operation;
import it.gov.pagopa.pu.pagopapayments.mapper.AcaDebtPositionMapper;
import it.gov.pagopa.pu.pagopapayments.service.acagpdsync.aca.AcaFacadeService;
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
  private final PodamFactory podamFactory = TestUtils.getPodamFactory();

  @Test
  void givenOperationCreateWhenSyncThenInvokePaCreatePositionUsingAcaKey() {
    // given
    String iud = "IUD";
    String accessToken = TestUtils.getFakeAccessToken();

    DebtPositionDTO debtPosition = podamFactory.manufacturePojo(DebtPositionDTO.class);
    debtPosition.setDebtPositionOrigin(DebtPositionOrigin.ORDINARY);

    Organization organization = podamFactory.manufacturePojo(Organization.class);
    PaymentPositionModel model = podamFactory.manufacturePojo(PaymentPositionModel.class);

    BrokerForNodoPaDTO brokerForNodoPaDTO = BrokerForNodoPaDTO.builder()
      .organization(organization)
      .brokerApiKeys(new BrokerApiKeys().acaKey(VALID_ACA_KEY).gpdKey("OTHER_KEY"))
      .broker(new Broker())
      .build();

    Mockito.when(brokerRetrieverServiceMock.getBrokerForNodoPaDTOByOrganizationId(debtPosition.getOrganizationId(), accessToken))
      .thenReturn(brokerForNodoPaDTO);

    Mockito.when(acaDebtPositionMapperMock.mapToNewPaymentPositionModel(iud, debtPosition, organization.getOrgName()))
      .thenReturn(Pair.of(Operation.CREATE, model));

    // when
    acaFacadeService.sync(iud, debtPosition, accessToken);

    // then
    Mockito.verify(acaServiceMock).paCreatePosition(
      Mockito.eq(VALID_ACA_KEY),
      Mockito.eq(organization.getOrgFiscalCode()),
      Mockito.same(model)
    );
    Mockito.verifyNoMoreInteractions(acaServiceMock);
  }

  @Test
  void givenOperationUpdateWhenSyncThenInvokePaUpdatePositionUsingAcaKey() {
    // given
    String iud = "IUD";
    String accessToken = TestUtils.getFakeAccessToken();

    DebtPositionDTO debtPosition = podamFactory.manufacturePojo(DebtPositionDTO.class);
    debtPosition.setDebtPositionOrigin(DebtPositionOrigin.ORDINARY);

    Organization organization = podamFactory.manufacturePojo(Organization.class);
    PaymentPositionModel model = podamFactory.manufacturePojo(PaymentPositionModel.class);

    BrokerForNodoPaDTO brokerForNodoPaDTO = BrokerForNodoPaDTO.builder()
      .organization(organization)
      .brokerApiKeys(new BrokerApiKeys().acaKey(VALID_ACA_KEY).gpdKey("OTHER_KEY"))
      .broker(new Broker())
      .build();

    Mockito.when(brokerRetrieverServiceMock.getBrokerForNodoPaDTOByOrganizationId(debtPosition.getOrganizationId(), accessToken))
      .thenReturn(brokerForNodoPaDTO);

    Mockito.when(acaDebtPositionMapperMock.mapToNewPaymentPositionModel(iud, debtPosition, organization.getOrgName()))
      .thenReturn(Pair.of(Operation.UPDATE, model));

    // when
    acaFacadeService.sync(iud, debtPosition, accessToken);

    // then
    Mockito.verify(acaServiceMock).paUpdatePosition(
      Mockito.eq(VALID_ACA_KEY),
      Mockito.eq(organization.getOrgFiscalCode()),
      Mockito.eq(model.getIupd()),
      Mockito.same(model)
    );
    Mockito.verifyNoMoreInteractions(acaServiceMock);
  }

  @Test
  void givenOperationDeleteWhenSyncThenInvokePaDeletePositionUsingAcaKey() {
    // given
    String iud = "IUD";
    String accessToken = TestUtils.getFakeAccessToken();

    DebtPositionDTO debtPosition = podamFactory.manufacturePojo(DebtPositionDTO.class);
    debtPosition.setDebtPositionOrigin(DebtPositionOrigin.ORDINARY);

    Organization organization = podamFactory.manufacturePojo(Organization.class);
    PaymentPositionModel model = podamFactory.manufacturePojo(PaymentPositionModel.class);

    BrokerForNodoPaDTO brokerForNodoPaDTO = BrokerForNodoPaDTO.builder()
      .organization(organization)
      .brokerApiKeys(new BrokerApiKeys().acaKey(VALID_ACA_KEY).gpdKey("OTHER_KEY"))
      .broker(new Broker())
      .build();

    Mockito.when(brokerRetrieverServiceMock.getBrokerForNodoPaDTOByOrganizationId(debtPosition.getOrganizationId(), accessToken))
      .thenReturn(brokerForNodoPaDTO);

    Mockito.when(acaDebtPositionMapperMock.mapToNewPaymentPositionModel(iud, debtPosition, organization.getOrgName()))
      .thenReturn(Pair.of(Operation.DELETE, model));

    // when
    acaFacadeService.sync(iud, debtPosition, accessToken);

    // then
    Mockito.verify(acaServiceMock).paDeletePosition(
      Mockito.eq(VALID_ACA_KEY),
      Mockito.eq(organization.getOrgFiscalCode()),
      Mockito.eq(model.getIupd()),
      Mockito.same(model)
    );
    Mockito.verifyNoMoreInteractions(acaServiceMock);
  }

  @Test
  void givenExcludedOriginWhenSyncThenSkipExecution() {
    // given
    String iud = "IUD";
    DebtPositionDTO debtPosition = podamFactory.manufacturePojo(DebtPositionDTO.class);
    debtPosition.setDebtPositionOrigin(DebtPositionOrigin.SPONTANEOUS);

    // when
    acaFacadeService.sync(iud, debtPosition, TestUtils.getFakeAccessToken());

    // then
    Mockito.verifyNoInteractions(brokerRetrieverServiceMock, acaDebtPositionMapperMock, acaServiceMock);
  }

  @Test
  void givenOperationCreateWithBrokerDelegateWhenSyncACAThenInvokePaCreateDPUsingOrgOfTransferOwner(){
    // given
    String iud = "IUD";
    String accessToken = TestUtils.getFakeAccessToken();
    String orgName = "ORG";
    String orgFiscalCode = "11111222223";
    DebtPositionDTO debtPosition = podamFactory.manufacturePojo(DebtPositionDTO.class);
    debtPosition.setDebtPositionOrigin(DebtPositionOrigin.ORDINARY);
    debtPosition.getPaymentOptions().getFirst().getInstallments().getFirst().getTransfers().getFirst().setFlagOwner(Boolean.TRUE);
    debtPosition.getPaymentOptions().getFirst().getInstallments().getFirst().getTransfers().getFirst().setOrgFiscalCode(orgFiscalCode);
    debtPosition.getPaymentOptions().getFirst().getInstallments().getFirst().getTransfers().getFirst().setOrgName(orgName);
    Organization organization = podamFactory.manufacturePojo(Organization.class);
    Broker broker = podamFactory.manufacturePojo(Broker.class);
    broker.setFlagDelegate(Boolean.TRUE);
    PaymentPositionModel model = podamFactory.manufacturePojo(PaymentPositionModel.class);

    BrokerForNodoPaDTO brokerForNodoPaDTO = BrokerForNodoPaDTO.builder()
      .organization(organization)
      .brokerApiKeys(new BrokerApiKeys().acaKey(VALID_ACA_KEY).gpdKey("OTHER_KEY"))
      .broker(broker)
      .build();

    Mockito.when(brokerRetrieverServiceMock.getBrokerForNodoPaDTOByOrganizationId(debtPosition.getOrganizationId(), accessToken))
      .thenReturn(brokerForNodoPaDTO);

    Mockito.when(acaDebtPositionMapperMock.mapToNewPaymentPositionModel(iud, debtPosition, orgName))
      .thenReturn(Pair.of(Operation.CREATE, model));

    // when
    acaFacadeService.sync(iud, debtPosition, accessToken);

    // then
    Mockito.verify(acaServiceMock).paCreatePosition(
      Mockito.eq(VALID_ACA_KEY),
      Mockito.eq(orgFiscalCode),
      Mockito.same(model)
    );
    Mockito.verifyNoMoreInteractions(acaServiceMock);
  }
}
