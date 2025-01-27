package it.gov.pagopa.pu.pagopapayments.connector.soap;

import gov.telematici.pagamenti.ws.NodoChiediElencoFlussiRendicontazione;
import gov.telematici.pagamenti.ws.NodoChiediElencoFlussiRendicontazioneRisposta;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ws.client.core.WebServiceMessageCallback;
import org.springframework.ws.client.core.WebServiceTemplate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NodeForPaClientImplTest {

  @Mock
  private WebServiceTemplate webServiceTemplate;

  @InjectMocks
  private NodeForPaClientImpl nodeForPaClient;

  @Test
  void nodoChiediElencoFlussiRendicontazione_whenValidRequest_thenReturnResponse() {
    NodoChiediElencoFlussiRendicontazione request = new NodoChiediElencoFlussiRendicontazione();
    NodoChiediElencoFlussiRendicontazioneRisposta expectedResponse = new NodoChiediElencoFlussiRendicontazioneRisposta();
    String apiKey = "testApiKey";

    when(webServiceTemplate.marshalSendAndReceive(eq(request), any(WebServiceMessageCallback.class)))
      .thenReturn(expectedResponse);

    NodoChiediElencoFlussiRendicontazioneRisposta response = nodeForPaClient.nodoChiediElencoFlussiRendicontazione(request, apiKey);

    Assertions.assertNotNull(response);
    Assertions.assertEquals(expectedResponse, response);
    verify(webServiceTemplate, times(1)).marshalSendAndReceive(eq(request), any(WebServiceMessageCallback.class));
  }

  @Test
  void nodoChiediElencoFlussiRendicontazione_whenWebServiceTemplateThrowsException_thenThrowException() {
    NodoChiediElencoFlussiRendicontazione request = new NodoChiediElencoFlussiRendicontazione();
    String apiKey = "testApiKey";

    when(webServiceTemplate.marshalSendAndReceive(eq(request), any(WebServiceMessageCallback.class)))
      .thenThrow(new RuntimeException("WebService error"));

    RuntimeException exception = Assertions.assertThrows(RuntimeException.class, () -> nodeForPaClient.nodoChiediElencoFlussiRendicontazione(request, apiKey));
    Assertions.assertEquals("WebService error", exception.getMessage());
    verify(webServiceTemplate, times(1)).marshalSendAndReceive(eq(request), any(WebServiceMessageCallback.class));
  }
}
