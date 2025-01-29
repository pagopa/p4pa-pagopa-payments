package it.gov.pagopa.pu.pagopapayments.connector.soap;

import gov.telematici.pagamenti.ws.NodoChiediElencoFlussiRendicontazione;
import gov.telematici.pagamenti.ws.NodoChiediElencoFlussiRendicontazioneRisposta;
import gov.telematici.pagamenti.ws.TipoElencoFlussiRendicontazione;
import gov.telematici.pagamenti.ws.TipoIdRendicontazione;
import it.gov.pagopa.pu.organization.dto.generated.Broker;
import it.gov.pagopa.pu.organization.dto.generated.BrokerApiKeys;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.pagopapayments.dto.BrokerForNodoPaDTO;
import it.gov.pagopa.pu.pagopapayments.dto.generated.PaymentsReportingIdDTO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
  private WebServiceTemplate webServiceTemplate;

  @InjectMocks
  private NodeForPaClientImpl nodeForPaClient;

  private static final Broker BROKER = new Broker()
    .brokerFiscalCode("brokerCode")
    .broadcastStationId("stationId");

  private static final BrokerApiKeys BROKER_API_KEYS = new BrokerApiKeys()
    .syncKey("syncKey");
  private static final Organization ORGANIZATION = new Organization()
    .orgFiscalCode("orgFiscalCode");

  private static final BrokerForNodoPaDTO BROKER_FOR_NODO_PA_DTO = BrokerForNodoPaDTO.builder()
    .broker(BROKER)
    .organization(ORGANIZATION)
    .brokerApiKeys(BROKER_API_KEYS)
    .build();



  @Test
  void nodoChiediElencoFlussiRendicontazione_whenValidRequest_thenReturnResponse() {
    NodoChiediElencoFlussiRendicontazioneRisposta response = new NodoChiediElencoFlussiRendicontazioneRisposta();
    response.setElencoFlussiRendicontazione(new TipoElencoFlussiRendicontazione());
    response.getElencoFlussiRendicontazione().getIdRendicontaziones().add(new TipoIdRendicontazione());

    doReturn(response).when(webServiceTemplate).marshalSendAndReceive(any(NodoChiediElencoFlussiRendicontazione.class), any(WebServiceMessageCallback.class));

    List<PaymentsReportingIdDTO> reportingList = nodeForPaClient.getPaymentsReportingList(BROKER_FOR_NODO_PA_DTO);

    Assertions.assertNotNull(reportingList);
    Assertions.assertFalse(reportingList.isEmpty());
    verify(webServiceTemplate, times(1)).marshalSendAndReceive(any(NodoChiediElencoFlussiRendicontazione.class), any(WebServiceMessageCallback.class));
  }

  @Test
  void nodoChiediElencoFlussiRendicontazione_whenWebServiceTemplateThrowsException_thenThrowException() {
    doThrow(new RuntimeException("WebService error")).when(webServiceTemplate).marshalSendAndReceive(any(NodoChiediElencoFlussiRendicontazione.class), any(WebServiceMessageCallback.class));

    RuntimeException exception = Assertions.assertThrows(RuntimeException.class, () -> nodeForPaClient.getPaymentsReportingList(BROKER_FOR_NODO_PA_DTO));
    Assertions.assertEquals("WebService error", exception.getMessage());
    verify(webServiceTemplate, times(1)).marshalSendAndReceive(any(NodoChiediElencoFlussiRendicontazione.class), any(WebServiceMessageCallback.class));
  }

  @Test
  void getMessageCallback_whenCalled_thenSetsSoapActionAndApiKey() throws IOException, TransformerException {
    String apiKey = "testApiKey";
    String soapAction = "testSoapAction";
    NodeForPaClientImpl client = new NodeForPaClientImpl();

    SoapMessage soapMessageMock = mock(SoapMessage.class);
    TransportContext transportContextMock = mock(TransportContext.class);
    HttpUrlConnection httpUrlConnectionMock = mock(HttpUrlConnection.class);

    when(transportContextMock.getConnection()).thenReturn(httpUrlConnectionMock);
    TransportContextHolder.setTransportContext(transportContextMock);

    WebServiceMessageCallback callback = client.getMessageCallback(apiKey, soapAction);
    callback.doWithMessage(soapMessageMock);

    verify(soapMessageMock, times(1)).setSoapAction(soapAction);
    verify(httpUrlConnectionMock, times(1)).addRequestHeader(NodeForPaClientImpl.HEADER_SUBSCRIPTION_KEY, apiKey);
  }

}
