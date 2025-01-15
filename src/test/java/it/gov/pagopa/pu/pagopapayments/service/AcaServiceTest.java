package it.gov.pagopa.pu.pagopapayments.service;

import it.gov.pagopa.nodo.pacreateposition.dto.generated.NewDebtPositionRequest;
import it.gov.pagopa.pu.organization.dto.generated.BrokerApiKeys;
import it.gov.pagopa.pu.pagopapayments.connector.AcaClient;
import it.gov.pagopa.pu.pagopapayments.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.pagopapayments.mapper.AcaDebtPositionMapper;
import it.gov.pagopa.pu.pagopapayments.service.aca.AcaService;
import it.gov.pagopa.pu.pagopapayments.service.broker.BrokerService;
import it.gov.pagopa.pu.pagopapayments.util.TestUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class AcaServiceTest {
  @Mock
  private AcaClient acaClientMock;
  @Mock
  private AcaDebtPositionMapper acaDebtPositionMapperMock;
  @Mock
  private BrokerService brokerServiceMock;

  @InjectMocks
  private AcaService acaService;

  private static final Long VALID_ORG_ID = 1L;
  private static final String VALID_ACA_KEY = "validAcaKey";
  private static final String VALID_SEGREGATION_CODE = "01";
  private static final BrokerApiKeys VALID_API_KEYS = new BrokerApiKeys()
    .acaKey(VALID_ACA_KEY)
    .syncKey("validSyncKey");

  private DebtPositionDTO debtPosition;
  private List<NewDebtPositionRequest> newDebtPositionRequestList;

  @BeforeEach
  void setUp() {
    debtPosition = DebtPositionDTO.builder()
      .debtPositionId(1L)
      .description("description")
      .organizationId(VALID_ORG_ID)
      .build();

    newDebtPositionRequestList = List.of(
      new NewDebtPositionRequest()
        .amount(1234)
        .iuv("IUV")
        .entityFiscalCode("FISCAL_CODE"),
      new NewDebtPositionRequest()
        .amount(3456)
        .iuv("IUV2")
        .entityFiscalCode("FISCAL_CODE2")
    );
  }

  @Test
  void givenValidDebtPositionWhenCreateThenOk() {
    //given
    Mockito.when(acaDebtPositionMapperMock.mapToNewDebtPositionRequest(debtPosition)).thenReturn(newDebtPositionRequestList);
    Mockito.when(brokerServiceMock.getBrokerApiKeyAndSegregationCodesByOrganizationId(VALID_ORG_ID, TestUtils.getFakeAccessToken())).thenReturn(Pair.of(VALID_API_KEYS, VALID_SEGREGATION_CODE));
    //when
    acaService.create(debtPosition, TestUtils.getFakeAccessToken());
    //verify
    Mockito.verify(acaDebtPositionMapperMock, Mockito.times(1)).mapToNewDebtPositionRequest(debtPosition);
    Mockito.verify(brokerServiceMock, Mockito.times(1)).getBrokerApiKeyAndSegregationCodesByOrganizationId(VALID_ORG_ID, TestUtils.getFakeAccessToken());
    newDebtPositionRequestList.forEach(newDebtPositionRequest ->
      Mockito.verify(acaClientMock, Mockito.times(1)).paCreatePosition(newDebtPositionRequest, VALID_ACA_KEY, VALID_SEGREGATION_CODE)
    );
  }

  @Test
  void givenValidDebtPositionWhenUpdateThenOk() {
    //given
    Mockito.when(acaDebtPositionMapperMock.mapToNewDebtPositionRequest(debtPosition)).thenReturn(newDebtPositionRequestList);
    Mockito.when(brokerServiceMock.getBrokerApiKeyAndSegregationCodesByOrganizationId(VALID_ORG_ID, TestUtils.getFakeAccessToken())).thenReturn(Pair.of(VALID_API_KEYS, VALID_SEGREGATION_CODE));
    //when
    acaService.update(debtPosition, TestUtils.getFakeAccessToken());
    //verify
    Mockito.verify(acaDebtPositionMapperMock, Mockito.times(1)).mapToNewDebtPositionRequest(debtPosition);
    Mockito.verify(brokerServiceMock, Mockito.times(1)).getBrokerApiKeyAndSegregationCodesByOrganizationId(VALID_ORG_ID, TestUtils.getFakeAccessToken());
    newDebtPositionRequestList.forEach(newDebtPositionRequest ->
      Mockito.verify(acaClientMock, Mockito.times(1)).paCreatePosition(newDebtPositionRequest, VALID_ACA_KEY, VALID_SEGREGATION_CODE)
    );
  }

  @Test
  void givenValidDebtPositionWhenDeleteThenOk() {
    //given
    Mockito.when(acaDebtPositionMapperMock.mapToNewDebtPositionRequest(debtPosition)).thenReturn(newDebtPositionRequestList);
    Mockito.when(brokerServiceMock.getBrokerApiKeyAndSegregationCodesByOrganizationId(VALID_ORG_ID, TestUtils.getFakeAccessToken())).thenReturn(Pair.of(VALID_API_KEYS, VALID_SEGREGATION_CODE));
    //when
    acaService.delete(debtPosition, TestUtils.getFakeAccessToken());
    //verify

    Mockito.verify(acaDebtPositionMapperMock, Mockito.times(1)).mapToNewDebtPositionRequest(debtPosition);
    Mockito.verify(brokerServiceMock, Mockito.times(1)).getBrokerApiKeyAndSegregationCodesByOrganizationId(VALID_ORG_ID, TestUtils.getFakeAccessToken());
    newDebtPositionRequestList.forEach(newDebtPositionRequest ->
      Mockito.verify(acaClientMock, Mockito.times(1)).paCreatePosition(
        Mockito.argThat(x -> x.getAmount()==0 && x.equals(newDebtPositionRequest)), Mockito.eq(VALID_ACA_KEY), Mockito.eq(VALID_SEGREGATION_CODE))
    );
  }
}
