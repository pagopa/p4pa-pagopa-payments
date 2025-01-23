package it.gov.pagopa.pu.pagopapayments.connector.soap;

import gov.telematici.pagamenti.ws.NodoChiediElencoFlussiRendicontazione;
import gov.telematici.pagamenti.ws.NodoChiediElencoFlussiRendicontazioneRisposta;

public class NodeForPaClientImpl extends AbstractBaseClientImpl implements NodeForPaClient {

  public final static String SUBSCRIPTION_KEY_KEY = "Ocp-Apim-Subscription-Key";

  @Override
  public NodoChiediElencoFlussiRendicontazioneRisposta nodoChiediElencoFlussiRendicontazione(NodoChiediElencoFlussiRendicontazione request, String apiKey) {
    //TODO XXX.setHeader(SUBSCRIPTION_KEY_KEY, apiKey);
    NodoChiediElencoFlussiRendicontazioneRisposta response = (NodoChiediElencoFlussiRendicontazioneRisposta)
      getWebServiceTemplate().marshalSendAndReceive(request, getMessageCallback());
    return response;
  }
}
