package it.gov.pagopa.pu.pagopapayments.connector.soap;

import gov.telematici.pagamenti.ws.*;
import it.gov.pagopa.pagopa_api.xsd.common_types.v1_0.CtFaultBean;
import it.gov.pagopa.pu.organization.dto.generated.Broker;
import it.gov.pagopa.pu.organization.dto.generated.BrokerApiKeys;
import it.gov.pagopa.pu.organization.dto.generated.OrganizationStationDTO;
import it.gov.pagopa.pu.pagopapayments.connector.soap.mapper.NodoChiediFlussoRendicontazioneMapper;
import it.gov.pagopa.pu.pagopapayments.dto.BrokerForNodoPaDTO;
import it.gov.pagopa.pu.pagopapayments.dto.PaPaymentReportingDTO;
import it.gov.pagopa.pu.pagopapayments.dto.generated.PaymentsReportingIdDTO;
import it.gov.pagopa.pu.pagopapayments.exception.ApplicationException;
import it.gov.pagopa.pu.pagopapayments.mapper.PaymentsReportingMapper;
import it.gov.pagopa.pu.pagopapayments.registry.RegistryContextData;
import it.gov.pagopa.pu.pagopapayments.registry.RegistryEventType;
import it.gov.pagopa.pu.pagopapayments.registry.RegistryLogger;
import it.gov.pagopa.pu.pagopapayments.registry.RegistryLoggerTest;
import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.activation.FileDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.client.core.WebServiceMessageCallback;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.transport.context.TransportContext;
import org.springframework.ws.transport.context.TransportContextHolder;
import org.springframework.ws.transport.http.HttpUrlConnection;

import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NodeForPaClientImplTest {

  @Mock
  private WebServiceTemplate webServiceTemplateMock;
  @Mock
  private NodoChiediFlussoRendicontazioneMapper fetchPaymentsReportingRequestMapperMock;
  @Mock
  private RegistryLogger registryLoggerMock;
  @Mock
  private PaymentsReportingMapper paymentsReportingMapper;

  private NodeForPaClientImpl nodeForPaClient;

  @BeforeEach
  void init(){
    this.nodeForPaClient = new NodeForPaClientImpl("http://localhost", new Jaxb2Marshaller(), fetchPaymentsReportingRequestMapperMock, registryLoggerMock, paymentsReportingMapper);
    nodeForPaClient.setWebServiceTemplate(webServiceTemplateMock);
  }

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(
      webServiceTemplateMock,
      fetchPaymentsReportingRequestMapperMock,
      registryLoggerMock);
  }

  private void configureRegistryLoggerMock(RegistryContextData contextData, Object request) {
    RegistryLoggerTest.configureRegistryLoggerMock(registryLoggerMock, contextData, request, false, true);
  }

  private static final Broker BROKER = new Broker()
    .brokerFiscalCode("brokerCode")
    .broadcastStationId("stationId")
    .stationId("stationId");

  private static final BrokerApiKeys BROKER_API_KEYS = new BrokerApiKeys()
    .syncKey("syncKey");
  private static final OrganizationStationDTO ORGANIZATION_STATION_DTO = new OrganizationStationDTO()
    .orgFiscalCode("orgFiscalCode");

  private static final BrokerForNodoPaDTO BROKER_FOR_NODO_PA_DTO = BrokerForNodoPaDTO.builder()
    .broker(BROKER)
    .brokerApiKeys(BROKER_API_KEYS)
    .organizationStation(ORGANIZATION_STATION_DTO)
    .build();

  @Test
  void givenValidRequestWhenGetPaymentsReportingListThenReturnResponse() {
    NodoChiediElencoFlussiRendicontazioneRisposta response = new NodoChiediElencoFlussiRendicontazioneRisposta();
    response.setElencoFlussiRendicontazione(new TipoElencoFlussiRendicontazione());
    response.getElencoFlussiRendicontazione().getIdRendicontaziones().add(new TipoIdRendicontazione());

    doReturn(response).when(webServiceTemplateMock).marshalSendAndReceive(any(NodoChiediElencoFlussiRendicontazione.class), any(WebServiceMessageCallback.class));

    List<PaymentsReportingIdDTO> reportingList = nodeForPaClient.getPaymentsReportingList(BROKER_FOR_NODO_PA_DTO);

    Assertions.assertNotNull(reportingList);
    Assertions.assertFalse(reportingList.isEmpty());
  }

  @Test
  void givenWebServiceTemplateThrowsExceptionWhenGetPaymentsReportingListThenThrowException() {
    doThrow(new RuntimeException("WebService error")).when(webServiceTemplateMock).marshalSendAndReceive(any(NodoChiediElencoFlussiRendicontazione.class), any(WebServiceMessageCallback.class));

    RuntimeException exception = Assertions.assertThrows(RuntimeException.class, () -> nodeForPaClient.getPaymentsReportingList(BROKER_FOR_NODO_PA_DTO));
    Assertions.assertEquals("WebService error", exception.getMessage());
  }

  @Test
  void givenFaultCodeIsPPT_DOMINIO_SCONOSCIUTOWhenGetPaymentsReportingListThenReturnEmptyList() {
    NodoChiediElencoFlussiRendicontazioneRisposta response = new NodoChiediElencoFlussiRendicontazioneRisposta();
    response.setFault(new CtFaultBean());
    response.getFault().setFaultCode("PPT_DOMINIO_SCONOSCIUTO");

    doReturn(response).when(webServiceTemplateMock).marshalSendAndReceive(any(NodoChiediElencoFlussiRendicontazione.class), any(WebServiceMessageCallback.class));

    List<PaymentsReportingIdDTO> reportingList = nodeForPaClient.getPaymentsReportingList(BROKER_FOR_NODO_PA_DTO);

    Assertions.assertNotNull(reportingList);
    Assertions.assertTrue(reportingList.isEmpty());
  }

  @Test
  void whenGetMessageCallbackThenSetsSoapActionAndApiKey() throws IOException, TransformerException {
    String apiKey = "testApiKey";
    String soapAction = "testSoapAction";

    SoapMessage soapMessageMock = mock(SoapMessage.class);
    TransportContext transportContextMock = mock(TransportContext.class);
    HttpUrlConnection httpUrlConnectionMock = mock(HttpUrlConnection.class);

    when(transportContextMock.getConnection()).thenReturn(httpUrlConnectionMock);
    TransportContextHolder.setTransportContext(transportContextMock);

    WebServiceMessageCallback callback = nodeForPaClient.getMessageCallback(apiKey, soapAction);
    callback.doWithMessage(soapMessageMock);

    verify(soapMessageMock, times(1)).setSoapAction(soapAction);
    verify(httpUrlConnectionMock, times(1)).addRequestHeader(NodeForPaClientImpl.HEADER_SUBSCRIPTION_KEY, apiKey);
  }

  @Test
  void givenValidRequestWhenFetchPaymentReportingThenReturnPaPaymentReportingDTO() {
    NodoChiediFlussoRendicontazioneRisposta response = new NodoChiediFlussoRendicontazioneRisposta();
    ClassLoader classLoader = getClass().getClassLoader();
    DataSource dataSource = new FileDataSource(classLoader.getResource("nodeForPaClientImplTest.xml").getFile());
    DataHandler dataHandler = new DataHandler(dataSource);
    response.setXmlRendicontazione(dataHandler);

    String reportingId = "reportingId";
    configureFetchPaymentsReportingMocks(reportingId);

    doReturn(response).when(webServiceTemplateMock).marshalSendAndReceive(any(NodoChiediFlussoRendicontazione.class), any(WebServiceMessageCallback.class));

    PaPaymentReportingDTO result = nodeForPaClient.fetchPaymentReporting(BROKER_FOR_NODO_PA_DTO, reportingId);

    Assertions.assertNotNull(result);
    Assertions.assertEquals("brokerCode", result.getIdBrokerPA());
    Assertions.assertEquals("stationId", result.getIdStation());
    Assertions.assertEquals("orgFiscalCode", result.getFiscalCode());
  }

  @Test
  void givenResponseHasFaultWhenFetchPaymentReportingThenThrowException() {
    NodoChiediFlussoRendicontazioneRisposta response = new NodoChiediFlussoRendicontazioneRisposta();
    response.setFault(new CtFaultBean());
    response.getFault().setFaultCode("faultCode");

    String reportingId = "reportingId";
    configureFetchPaymentsReportingMocks(reportingId);

    doReturn(response).when(webServiceTemplateMock).marshalSendAndReceive(any(NodoChiediFlussoRendicontazione.class), any(WebServiceMessageCallback.class));

    ApplicationException exception = Assertions.assertThrows(ApplicationException.class, () -> nodeForPaClient.fetchPaymentReporting(BROKER_FOR_NODO_PA_DTO, reportingId));
    Assertions.assertEquals("Error during the call to the payment node faultCode", exception.getMessage());
  }

  @Test
  void givenWebServiceTemplateThrowsExceptionWhenFetchPaymentReportingThenThrowException() {
    String reportingId = "reportingId";
    configureFetchPaymentsReportingMocks(reportingId);

    doThrow(new RuntimeException("WebService error")).when(webServiceTemplateMock).marshalSendAndReceive(any(NodoChiediFlussoRendicontazione.class), any(WebServiceMessageCallback.class));

    RuntimeException exception = Assertions.assertThrows(RuntimeException.class, () -> nodeForPaClient.fetchPaymentReporting(BROKER_FOR_NODO_PA_DTO, reportingId));
    Assertions.assertEquals("WebService error", exception.getMessage());
  }

  private void configureFetchPaymentsReportingMocks(String reportingId) {
    NodoChiediFlussoRendicontazione expectedRequest = new NodoChiediFlussoRendicontazione();
    expectedRequest.setIdentificativoDominio(BROKER_FOR_NODO_PA_DTO.getOrganizationStation().getOrgFiscalCode());
    expectedRequest.setIdentificativoIntermediarioPA(BROKER_FOR_NODO_PA_DTO.getBroker().getBrokerFiscalCode());
    expectedRequest.setIdentificativoStazioneIntermediarioPA(BROKER_FOR_NODO_PA_DTO.getBroker().getStationId());
    expectedRequest.setIdentificativoFlusso(reportingId);

    when(fetchPaymentsReportingRequestMapperMock.createFlussoRendicontazioneRequest(Mockito.same(BROKER_FOR_NODO_PA_DTO), Mockito.endsWith(reportingId)))
      .thenReturn(expectedRequest);

    RegistryContextData expectedContextData = RegistryContextData.builder()
      .orgFiscalCode(BROKER_FOR_NODO_PA_DTO.getOrganizationStation().getOrgFiscalCode())
      .brokerStationId(BROKER_FOR_NODO_PA_DTO.getBroker().getStationId())
      .eventType(RegistryEventType.NodeForPa_fetchPaymentReporting)
      .build();
    configureRegistryLoggerMock(expectedContextData, expectedRequest);
  }
}
