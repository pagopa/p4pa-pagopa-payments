package it.gov.pagopa.pu.pagopapayments.connector.soap;

import gov.telematici.pagamenti.ws.NodoChiediElencoFlussiRendicontazione;
import gov.telematici.pagamenti.ws.NodoChiediElencoFlussiRendicontazioneRisposta;
import it.gov.pagopa.pu.pagopapayments.dto.BrokerForNodoPaDTO;
import it.gov.pagopa.pu.pagopapayments.dto.generated.PaymentsReportingIdDTO;
import it.gov.pagopa.pu.pagopapayments.exception.ApplicationException;
import it.gov.pagopa.pu.pagopapayments.mapper.PaymentsReportingIdMapper;
import org.springframework.ws.client.core.WebServiceMessageCallback;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.transport.context.TransportContext;
import org.springframework.ws.transport.context.TransportContextHolder;
import org.springframework.ws.transport.http.HttpUrlConnection;

import java.util.ArrayList;
import java.util.List;

public class NodeForPaClientImpl extends WebServiceGatewaySupport implements NodeForPaClient {

  public static final String HEADER_SUBSCRIPTION_KEY = "Ocp-Apim-Subscription-Key";

  @Override
  public List<PaymentsReportingIdDTO> getPaymentsReportingList(BrokerForNodoPaDTO brokerForNodoPaDTO) {
    NodoChiediElencoFlussiRendicontazione request = createRequest(brokerForNodoPaDTO);
    NodoChiediElencoFlussiRendicontazioneRisposta response = (NodoChiediElencoFlussiRendicontazioneRisposta)
      getWebServiceTemplate().marshalSendAndReceive(request, getMessageCallback(brokerForNodoPaDTO.getBrokerApiKeys().getSyncKey(), "nodoChiediElencoFlussiRendicontazione"));

    if (response.getFault() != null) {
      throw new ApplicationException("Error during the call to the payment node " + response.getFault().getFaultCode());
    }

    List<PaymentsReportingIdDTO> reportingList = new ArrayList<>();
    response.getElencoFlussiRendicontazione().getIdRendicontaziones().forEach(idRendicontazione ->
      reportingList.add(PaymentsReportingIdMapper.map(idRendicontazione))
    );

    return reportingList;
  }

  private NodoChiediElencoFlussiRendicontazione createRequest(BrokerForNodoPaDTO brokerForNodoPaDTO) {
    NodoChiediElencoFlussiRendicontazione request = new NodoChiediElencoFlussiRendicontazione();
    request.setIdentificativoDominio(brokerForNodoPaDTO.getOrganization().getOrgFiscalCode());
    request.setPassword("password");
    request.setIdentificativoIntermediarioPA(brokerForNodoPaDTO.getBroker().getBrokerFiscalCode());
    request.setIdentificativoStazioneIntermediarioPA(brokerForNodoPaDTO.getBroker().getStationId());
    request.setIdentificativoPSP(null);
    return request;
  }

  private WebServiceMessageCallback getMessageCallback(String apiKey, String soapAction) {
    return message -> {
      ((SoapMessage) message).setSoapAction(soapAction);
      TransportContext context = TransportContextHolder.getTransportContext();
      HttpUrlConnection connection = (HttpUrlConnection) context.getConnection();
      connection.addRequestHeader(HEADER_SUBSCRIPTION_KEY, apiKey);
    };
  }
}
