package it.gov.pagopa.pu.pagopapayments.service;

import it.gov.pagopa.nodo.gpd.dto.generated.PaymentPositionModel;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.organization.dto.generated.Broker;
import it.gov.pagopa.pu.organization.dto.generated.BrokerApiKeys;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.pagopapayments.connector.pagopa.gpd.GpdService;
import it.gov.pagopa.pu.pagopapayments.dto.BrokerForNodoPaDTO;
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
  void givenValidDebtPositionWhenSyncInsertThenOk() {
    //given
    DebtPositionDTO debtPosition = podamFactory.manufacturePojo(DebtPositionDTO.class);
    Organization organization = podamFactory.manufacturePojo(Organization.class);
    Pair<GpdDebtPositionMapper.OPERATION, PaymentPositionModel> paymentPositionModelAndOperation =
      Pair.of(GpdDebtPositionMapper.OPERATION.CREATE, podamFactory.manufacturePojo(PaymentPositionModel.class));

    BrokerForNodoPaDTO brokerForNodoPaDTO = BrokerForNodoPaDTO.builder()
      .organization(organization)
      .brokerApiKeys(new BrokerApiKeys().gpdKey(VALID_GPD_KEY))
      .broker(new Broker())
      .build();

    Mockito.when(gpdDebtPositionMapperMock.mapToNewPaymentPositionModel("IUD", debtPosition, organization)).thenReturn(paymentPositionModelAndOperation);

    Mockito.when(brokerRetrieverServiceMock.getBrokerForNodoPaDTOByOrganizationId(debtPosition.getOrganizationId(), TestUtils.getFakeAccessToken()))
      .thenReturn(brokerForNodoPaDTO);

    //when
    gpdFacadeService.sync("IUD", debtPosition, TestUtils.getFakeAccessToken());
    //verify
    Mockito.verify(gpdDebtPositionMapperMock, Mockito.times(1)).mapToNewPaymentPositionModel("IUD", debtPosition, organization);
    Mockito.verify(brokerRetrieverServiceMock, Mockito.times(1)).getBrokerForNodoPaDTOByOrganizationId(debtPosition.getOrganizationId(), TestUtils.getFakeAccessToken());
    Mockito.verify(gpdServiceMock, Mockito.times(1)).paCreatePosition(
      VALID_GPD_KEY,
      organization.getOrgFiscalCode(),
      paymentPositionModelAndOperation.getRight());
  }

  @Test
  void givenValidDebtPositionWhenSyncUpdateThenOk() {
    //given
    DebtPositionDTO debtPosition = podamFactory.manufacturePojo(DebtPositionDTO.class);
    Organization organization = podamFactory.manufacturePojo(Organization.class);
    Pair<GpdDebtPositionMapper.OPERATION, PaymentPositionModel> paymentPositionModelAndOperation =
      Pair.of(GpdDebtPositionMapper.OPERATION.UPDATE, podamFactory.manufacturePojo(PaymentPositionModel.class));

    BrokerForNodoPaDTO brokerForNodoPaDTO = BrokerForNodoPaDTO.builder()
      .organization(organization)
      .brokerApiKeys(new BrokerApiKeys().gpdKey(VALID_GPD_KEY))
      .broker(new Broker())
      .build();

    Mockito.when(gpdDebtPositionMapperMock.mapToNewPaymentPositionModel("IUD", debtPosition, organization)).thenReturn(paymentPositionModelAndOperation);

    Mockito.when(brokerRetrieverServiceMock.getBrokerForNodoPaDTOByOrganizationId(debtPosition.getOrganizationId(), TestUtils.getFakeAccessToken()))
      .thenReturn(brokerForNodoPaDTO);

    //when
    gpdFacadeService.sync("IUD", debtPosition, TestUtils.getFakeAccessToken());
    //verify
    Mockito.verify(gpdDebtPositionMapperMock, Mockito.times(1)).mapToNewPaymentPositionModel("IUD", debtPosition, organization);
    Mockito.verify(brokerRetrieverServiceMock, Mockito.times(1)).getBrokerForNodoPaDTOByOrganizationId(debtPosition.getOrganizationId(), TestUtils.getFakeAccessToken());
    Mockito.verify(gpdServiceMock, Mockito.times(1)).paUpdatePosition(
      Mockito.eq(VALID_GPD_KEY),
      Mockito.eq(organization.getOrgFiscalCode()),
      Mockito.eq(paymentPositionModelAndOperation.getRight().getIupd()),
      Mockito.same(paymentPositionModelAndOperation.getRight()));
  }
  @Test
  void givenValidDebtPositionWhenSyncDeleteThenOk() {
    //given
    DebtPositionDTO debtPosition = podamFactory.manufacturePojo(DebtPositionDTO.class);
    Organization organization = podamFactory.manufacturePojo(Organization.class);
    Pair<GpdDebtPositionMapper.OPERATION, PaymentPositionModel> paymentPositionModelAndOperation =
      Pair.of(GpdDebtPositionMapper.OPERATION.DELETE, podamFactory.manufacturePojo(PaymentPositionModel.class));

    BrokerForNodoPaDTO brokerForNodoPaDTO = BrokerForNodoPaDTO.builder()
      .organization(organization)
      .brokerApiKeys(new BrokerApiKeys().gpdKey(VALID_GPD_KEY))
      .broker(new Broker())
      .build();

    Mockito.when(gpdDebtPositionMapperMock.mapToNewPaymentPositionModel("IUD", debtPosition, organization)).thenReturn(paymentPositionModelAndOperation);

    Mockito.when(brokerRetrieverServiceMock.getBrokerForNodoPaDTOByOrganizationId(debtPosition.getOrganizationId(), TestUtils.getFakeAccessToken()))
      .thenReturn(brokerForNodoPaDTO);

    //when
    gpdFacadeService.sync("IUD", debtPosition, TestUtils.getFakeAccessToken());
    //verify
    Mockito.verify(gpdDebtPositionMapperMock, Mockito.times(1)).mapToNewPaymentPositionModel("IUD", debtPosition, organization);
    Mockito.verify(brokerRetrieverServiceMock, Mockito.times(1)).getBrokerForNodoPaDTOByOrganizationId(debtPosition.getOrganizationId(), TestUtils.getFakeAccessToken());
    Mockito.verify(gpdServiceMock, Mockito.times(1)).paDeletePosition(
      Mockito.eq(VALID_GPD_KEY),
      Mockito.eq(organization.getOrgFiscalCode()),
      Mockito.eq(paymentPositionModelAndOperation.getRight().getIupd()),
      Mockito.same(paymentPositionModelAndOperation.getRight()));
  }



}
