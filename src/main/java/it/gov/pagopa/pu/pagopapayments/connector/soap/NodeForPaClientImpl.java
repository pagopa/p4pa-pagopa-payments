package it.gov.pagopa.pu.pagopapayments.connector.soap;

import gov.telematici.pagamenti.ws.NodoChiediElencoFlussiRendicontazione;
import gov.telematici.pagamenti.ws.NodoChiediElencoFlussiRendicontazioneRisposta;
import org.springframework.ws.client.core.WebServiceMessageCallback;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.transport.context.TransportContext;
import org.springframework.ws.transport.context.TransportContextHolder;
import org.springframework.ws.transport.http.HttpUrlConnection;

public class NodeForPaClientImpl extends WebServiceGatewaySupport implements NodeForPaClient {

  public static final String SUBSCRIPTION_KEY_KEY = "Ocp-Apim-Subscription-Key";

  @Override
  public NodoChiediElencoFlussiRendicontazioneRisposta nodoChiediElencoFlussiRendicontazione(NodoChiediElencoFlussiRendicontazione request, String apiKey) {
    return (NodoChiediElencoFlussiRendicontazioneRisposta)
      getWebServiceTemplate().marshalSendAndReceive(request, getMessageCallback(apiKey, "nodoChiediElencoFlussiRendicontazione"));
  }

  private WebServiceMessageCallback getMessageCallback(String apiKey, String soapAction) {
    return message -> {
      //fix SoapAction header not included automatically by Spring WS
      ((SoapMessage)message).setSoapAction(soapAction);
      //set subscription key
      TransportContext context = TransportContextHolder.getTransportContext();
      HttpUrlConnection connection = (HttpUrlConnection) context.getConnection();
      connection.addRequestHeader(SUBSCRIPTION_KEY_KEY, apiKey);
    };
  }
}
