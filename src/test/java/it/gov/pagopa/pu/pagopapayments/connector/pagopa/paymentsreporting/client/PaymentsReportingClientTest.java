package it.gov.pagopa.pu.pagopapayments.connector.pagopa.paymentsreporting.client;

import gov.telematici.pagamenti.ws.NodoChiediFlussoRendicontazione;
import it.gov.digitpa.schemas._2011.pagamenti.FlussoRiversamento;
import it.gov.pagopa.nodo.fdrorganization.controller.generated.OrganizationsApi;
import it.gov.pagopa.nodo.fdrorganization.dto.generated.*;
import it.gov.pagopa.pu.organization.dto.generated.BrokerApiKeys;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.pagopapayments.connector.pagopa.paymentsreporting.config.PaymentsReportingApisHolder;
import it.gov.pagopa.pu.pagopapayments.connector.soap.mapper.NodoChiediFlussoRendicontazioneMapper;
import it.gov.pagopa.pu.pagopapayments.dto.BrokerForNodoPaDTO;
import it.gov.pagopa.pu.pagopapayments.dto.PaPaymentReportingDTO;
import it.gov.pagopa.pu.pagopapayments.mapper.PaymentsReportingMapper;
import it.gov.pagopa.pu.pagopapayments.registry.RegistryContextData;
import it.gov.pagopa.pu.pagopapayments.registry.RegistryEventType;
import it.gov.pagopa.pu.pagopapayments.registry.RegistryLogger;
import it.gov.pagopa.pu.pagopapayments.registry.RegistryLoggerTest;
import it.gov.pagopa.pu.pagopapayments.util.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.jemos.podam.api.PodamFactory;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@ExtendWith(MockitoExtension.class)
class PaymentsReportingClientTest {

  public static final NodoChiediFlussoRendicontazione NODO_CHIEDI_FLUSSO_RENDICONTAZIONE = new NodoChiediFlussoRendicontazione();
  @Mock
  private PaymentsReportingApisHolder paymentsReportingApisHolder;
  @Mock
  private OrganizationsApi organizationsApi;
  @Mock
  private RegistryLogger registryLogger;
  @Mock
  private PaymentsReportingMapper paymentsReportingMapper;
  @Mock
  private NodoChiediFlussoRendicontazioneMapper nodoChiediFlussoRendicontazioneMapper;

  @InjectMocks
  private PaymentsReportingClient paymentsReportingClient;

  private static final PodamFactory podamFactory = TestUtils.getPodamFactory();

  public static final String SYNC_PAYMENTS_REPORTING_API_KEY = "syncPaymentsReportingApiKey";
  private static final String ORGANIZATION_FISCAL_CODE = "12345678901";
  private static final String PSP_ID = "12345678901";
  private static final String PAYMENTS_REPORTING_ID = "paymentsReportingId";
  private static final Long REVISION = 1L;

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      paymentsReportingApisHolder,
      organizationsApi,
      registryLogger,
      paymentsReportingMapper,
      nodoChiediFlussoRendicontazioneMapper
    );
  }

  @ParameterizedTest
  @MethodSource("providePaginatedFlowsResponseScenarios")
  void fetchIdList(PaginatedFlowsResponse paginatedFlowsResponse) {
    //given
    BrokerForNodoPaDTO brokerForNodoPaDTO = new BrokerForNodoPaDTO();
    brokerForNodoPaDTO.setBrokerApiKeys(BrokerApiKeys.builder().syncPaymentsReportingKey(SYNC_PAYMENTS_REPORTING_API_KEY).build());
    Organization organization = podamFactory.manufacturePojo(Organization.class);
    organization.setOrgFiscalCode(ORGANIZATION_FISCAL_CODE);
    brokerForNodoPaDTO.setOrganization(organization);
    OffsetDateTime latestFlowDate = OffsetDateTime.now();

    List<FlowByPSP> expectedResult = paginatedFlowsResponse == null ? new ArrayList<>() : paginatedFlowsResponse.getData();

    Mockito.when(paymentsReportingApisHolder.getOrganizationApiByApiKey(SYNC_PAYMENTS_REPORTING_API_KEY))
      .thenReturn(organizationsApi);
    Mockito.when(
      organizationsApi.iOrganizationsControllerGetAllPublishedFlows(
        Mockito.eq(ORGANIZATION_FISCAL_CODE),
        Mockito.eq(latestFlowDate),
        Mockito.anyLong(),
        Mockito.isNull(), Mockito.isNull(), Mockito.isNull()
      )
    ).thenReturn(paginatedFlowsResponse);

    //when
    List<FlowByPSP> actualResult = paymentsReportingClient.fetchIdList(brokerForNodoPaDTO, latestFlowDate);

    //then
    Assertions.assertEquals(expectedResult, actualResult);
  }

  private static Stream<PaginatedFlowsResponse> providePaginatedFlowsResponseScenarios() {
    PaginatedFlowsResponse validPaginatedFlowsResponse = new PaginatedFlowsResponse();
    Metadata validMetadata = new Metadata(2, 1, 1);
    List<FlowByPSP> validExpectedResult = List.of(
      FlowByPSP.builder().fdr("1").build(),
      FlowByPSP.builder().fdr("2").build()
    );
    validPaginatedFlowsResponse.setMetadata(validMetadata);
    validPaginatedFlowsResponse.setData(validExpectedResult);

    List<FlowByPSP> invalidExpectedResult = new ArrayList<>();
    PaginatedFlowsResponse nullMetadataPaginatedFlowsResponse = new PaginatedFlowsResponse();
    nullMetadataPaginatedFlowsResponse.setMetadata(null);
    nullMetadataPaginatedFlowsResponse.setData(invalidExpectedResult);
    PaginatedFlowsResponse nullTotPageMetadataPaginatedFlowsResponse = new PaginatedFlowsResponse();
    Metadata nullTotPageMetadata = new Metadata(null, null, null);
    nullTotPageMetadataPaginatedFlowsResponse.setMetadata(nullTotPageMetadata);
    nullTotPageMetadataPaginatedFlowsResponse.setData(invalidExpectedResult);
    PaginatedFlowsResponse zeroTotPageMetadataPaginatedFlowsResponse = new PaginatedFlowsResponse();
    Metadata zeroTotPageMetadata = new Metadata(0, 0, 0);
    zeroTotPageMetadataPaginatedFlowsResponse.setMetadata(zeroTotPageMetadata);
    zeroTotPageMetadataPaginatedFlowsResponse.setData(invalidExpectedResult);

    return Stream.of(
      validPaginatedFlowsResponse,
      null,
      nullMetadataPaginatedFlowsResponse,
      nullTotPageMetadataPaginatedFlowsResponse,
      zeroTotPageMetadataPaginatedFlowsResponse
    );
  }

  @Test
  void fetchPaymentReportingFlow() {
    //given
    BrokerForNodoPaDTO brokerForNodoPaDTO = new BrokerForNodoPaDTO();
    brokerForNodoPaDTO.setBrokerApiKeys(BrokerApiKeys.builder().syncPaymentsReportingKey(SYNC_PAYMENTS_REPORTING_API_KEY).build());
    Organization organization = podamFactory.manufacturePojo(Organization.class);
    organization.setOrgFiscalCode(ORGANIZATION_FISCAL_CODE);
    brokerForNodoPaDTO.setOrganization(organization);

    SingleFlowResponse expectedResponse = new SingleFlowResponse();

    Mockito.when(paymentsReportingApisHolder.getOrganizationApiByApiKey(SYNC_PAYMENTS_REPORTING_API_KEY))
      .thenReturn(organizationsApi);
    Mockito.when(
      organizationsApi.iOrganizationsControllerGetSinglePublishedFlow(
        PAYMENTS_REPORTING_ID,
        ORGANIZATION_FISCAL_CODE,
        PSP_ID,
        REVISION
      )
    ).thenReturn(expectedResponse);

    //when
    SingleFlowResponse actualResponse = paymentsReportingClient.fetchPaymentReportingFlow(brokerForNodoPaDTO, PAYMENTS_REPORTING_ID, REVISION, PSP_ID);

    //then
    Assertions.assertEquals(expectedResponse, actualResponse);
  }

  @ParameterizedTest
  @MethodSource("providePaginatedPaymentsResponseScenarios")
  void fetchAllPaymentsForPaymentReportingFlow(PaginatedPaymentsResponse paginatedPaymentsResponse) {
    BrokerForNodoPaDTO brokerForNodoPaDTO = new BrokerForNodoPaDTO();
    brokerForNodoPaDTO.setBrokerApiKeys(BrokerApiKeys.builder().syncPaymentsReportingKey(SYNC_PAYMENTS_REPORTING_API_KEY).build());
    Organization organization = podamFactory.manufacturePojo(Organization.class);
    organization.setOrgFiscalCode(ORGANIZATION_FISCAL_CODE);
    brokerForNodoPaDTO.setOrganization(organization);

    SingleFlowResponse singleFlowResponse = new SingleFlowResponse();

    List<Payment> expectedResult = paginatedPaymentsResponse == null ? new ArrayList<>() : paginatedPaymentsResponse.getData();

    Mockito.when(paymentsReportingApisHolder.getOrganizationApiByApiKey(SYNC_PAYMENTS_REPORTING_API_KEY))
      .thenReturn(organizationsApi);
    Mockito.when(
      organizationsApi.iOrganizationsControllerGetPaymentsFromPublishedFlow(
        Mockito.eq(PAYMENTS_REPORTING_ID),
        Mockito.eq(ORGANIZATION_FISCAL_CODE),
        Mockito.eq(PSP_ID),
        Mockito.eq(REVISION),
        Mockito.anyLong(),
        Mockito.isNull()
      )
    ).thenReturn(paginatedPaymentsResponse);
    Mockito.when(
      nodoChiediFlussoRendicontazioneMapper.createFlussoRendicontazioneRequest(
        brokerForNodoPaDTO,
        PAYMENTS_REPORTING_ID
      )
    ).thenReturn(NODO_CHIEDI_FLUSSO_RENDICONTAZIONE);

    this.mockRegistryLogger();

    FlussoRiversamento paymentsReporting = new FlussoRiversamento();
    PaPaymentReportingDTO paPaymentReportingDTO = new PaPaymentReportingDTO();
    paPaymentReportingDTO.setPaymentReportingBytes(new byte[1]);

    Mockito.when(paymentsReportingMapper.mapPaymentsReporting(singleFlowResponse, expectedResult)).thenReturn(paymentsReporting);
    Mockito.when(paymentsReportingMapper.mapPaPaymentsReportingDTO(brokerForNodoPaDTO, paymentsReporting)).thenReturn(paPaymentReportingDTO);

    //when
    List<Payment> actualResult = paymentsReportingClient.fetchAllPaymentsForPaymentReportingFlow(brokerForNodoPaDTO, PAYMENTS_REPORTING_ID, REVISION, PSP_ID, singleFlowResponse);

    //then
    Assertions.assertEquals(expectedResult, actualResult);
  }

  private static Stream<PaginatedPaymentsResponse> providePaginatedPaymentsResponseScenarios() {
    PaginatedPaymentsResponse validPaginatedPaymentsResponse = new PaginatedPaymentsResponse();
    Metadata validMetadata = new Metadata(2, 1, 1);
    List<Payment> validExpectedResult = List.of(
      podamFactory.manufacturePojo(Payment.class),
      podamFactory.manufacturePojo(Payment.class)
    );  //null case
    validPaginatedPaymentsResponse.setMetadata(validMetadata);
    validPaginatedPaymentsResponse.setData(validExpectedResult);

    List<Payment> invalidExpectedResult = new ArrayList<>();
    PaginatedPaymentsResponse nullMetadataPaginatedPaymentsResponse = new PaginatedPaymentsResponse();
    nullMetadataPaginatedPaymentsResponse.setMetadata(null);
    nullMetadataPaginatedPaymentsResponse.setData(invalidExpectedResult);
    PaginatedPaymentsResponse nullTotPageMetadataPaginatedPaymentsResponse = new PaginatedPaymentsResponse();
    Metadata nullTotPageMetadata = new Metadata(null, null, null);
    nullTotPageMetadataPaginatedPaymentsResponse.setMetadata(nullTotPageMetadata);
    nullTotPageMetadataPaginatedPaymentsResponse.setData(invalidExpectedResult);
    PaginatedPaymentsResponse zeroTotPageMetadataPaginatedPaymentsResponse = new PaginatedPaymentsResponse();
    Metadata zeroTotPageMetadata = new Metadata(0, 0, 0);
    zeroTotPageMetadataPaginatedPaymentsResponse.setMetadata(zeroTotPageMetadata);
    zeroTotPageMetadataPaginatedPaymentsResponse.setData(invalidExpectedResult);
    return Stream.of(
      validPaginatedPaymentsResponse,
      null,
      nullMetadataPaginatedPaymentsResponse,
      nullTotPageMetadataPaginatedPaymentsResponse,
      zeroTotPageMetadataPaginatedPaymentsResponse
    );
  }

  private void mockRegistryLogger() {
    RegistryContextData contextData = RegistryContextData.builder()
      .orgFiscalCode(ORGANIZATION_FISCAL_CODE)
      .pspId(PSP_ID)
      .eventType(RegistryEventType.NodeForPa_fetchPaymentReporting)
      .build();
    RegistryLoggerTest.configureRegistryLoggerMock(registryLogger, contextData, NODO_CHIEDI_FLUSSO_RENDICONTAZIONE, false, true);
  }

}
