package it.gov.pagopa.pu.pagopapayments.connector.soap;

import gov.telematici.pagamenti.ws.NodoChiediElencoFlussiRendicontazione;
import gov.telematici.pagamenti.ws.NodoChiediElencoFlussiRendicontazioneRisposta;
import gov.telematici.pagamenti.ws.NodoChiediFlussoRendicontazione;
import gov.telematici.pagamenti.ws.NodoChiediFlussoRendicontazioneRisposta;
import it.gov.pagopa.pu.organization.dto.generated.Broker;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.pagopapayments.dto.BrokerForNodoPaDTO;
import it.gov.pagopa.pu.pagopapayments.dto.PaPaymentReportingDTO;
import it.gov.pagopa.pu.pagopapayments.dto.generated.PaymentsReportingIdDTO;
import it.gov.pagopa.pu.pagopapayments.exception.ApplicationException;
import it.gov.pagopa.pu.pagopapayments.mapper.PaymentsReportingIdMapper;
import org.springframework.ws.client.core.WebServiceMessageCallback;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.transport.context.TransportContext;
import org.springframework.ws.transport.context.TransportContextHolder;
import org.springframework.ws.transport.http.HttpUrlConnection;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class NodeForPaClientImpl extends WebServiceGatewaySupport implements NodeForPaClient {

  public static final String HEADER_SUBSCRIPTION_KEY = "Ocp-Apim-Subscription-Key";
  public static final String ERROR_MESSAGE = "Error during the call to the payment node ";

  @Override
  public List<PaymentsReportingIdDTO> getPaymentsReportingList(BrokerForNodoPaDTO brokerForNodoPaDTO) {
    NodoChiediElencoFlussiRendicontazione request = createElencoFlussiRendicontazioneRequest(brokerForNodoPaDTO);
    NodoChiediElencoFlussiRendicontazioneRisposta response = (NodoChiediElencoFlussiRendicontazioneRisposta)
      getWebServiceTemplate().marshalSendAndReceive(request, getMessageCallback(brokerForNodoPaDTO.getBrokerApiKeys().getSyncKey(), "nodoChiediElencoFlussiRendicontazione"));

    if (response.getFault() != null) {
      throw new ApplicationException(ERROR_MESSAGE + response.getFault().getFaultCode());
    }

    List<PaymentsReportingIdDTO> reportingList = new ArrayList<>();
    response.getElencoFlussiRendicontazione().getIdRendicontaziones().forEach(idRendicontazione ->
      reportingList.add(PaymentsReportingIdMapper.map(idRendicontazione))
    );

    return reportingList;
  }

public PaPaymentReportingDTO fetchPaymentReporting(BrokerForNodoPaDTO brokerForNodoPaDTO, String reportingId) {
  NodoChiediFlussoRendicontazione request = createFlussoRendicontazioneRequest(brokerForNodoPaDTO, reportingId);

  NodoChiediFlussoRendicontazioneRisposta response = (NodoChiediFlussoRendicontazioneRisposta)
    getWebServiceTemplate().marshalSendAndReceive(request, getMessageCallback(brokerForNodoPaDTO.getBrokerApiKeys().getSyncKey(), "nodoChiediFlussoRendicontazione"));

  if (response.getFault() != null) {
    throw new ApplicationException(ERROR_MESSAGE + response.getFault().getFaultCode());
  }
  byte[] bytes;
  try (InputStream inputStream = response.getXmlRendicontazione().getInputStream()) {
    bytes = inputStream.readAllBytes();
  }
  catch (Exception e) {
    throw new ApplicationException(ERROR_MESSAGE + e.getMessage());
  }



  return PaPaymentReportingDTO.builder()
    .idPA(brokerForNodoPaDTO.getOrganization().getOrgFiscalCode())
    .idBrokerPA(brokerForNodoPaDTO.getBroker().getBrokerFiscalCode())
    .idStation(brokerForNodoPaDTO.getBroker().getStationId())
    .fiscalCode(brokerForNodoPaDTO.getOrganization().getOrgFiscalCode())
    .paymentReportingBytes(bytes)
    .build();
  }

  private NodoChiediElencoFlussiRendicontazione createElencoFlussiRendicontazioneRequest(BrokerForNodoPaDTO brokerForNodoPaDTO) {
    NodoChiediElencoFlussiRendicontazione request = new NodoChiediElencoFlussiRendicontazione();
    request.setIdentificativoDominio(brokerForNodoPaDTO.getOrganization().getOrgFiscalCode());
    request.setPassword("password"); //parameter for retrocompatibility but not used. it is required by the wsdl
    request.setIdentificativoIntermediarioPA(brokerForNodoPaDTO.getBroker().getBrokerFiscalCode());
    request.setIdentificativoStazioneIntermediarioPA(brokerForNodoPaDTO.getBroker().getStationId());
    request.setIdentificativoPSP(null);
    return request;
  }



  WebServiceMessageCallback getMessageCallback(String apiKey, String soapAction) {
    return message -> {
      ((SoapMessage) message).setSoapAction(soapAction);
      TransportContext context = TransportContextHolder.getTransportContext();
      HttpUrlConnection connection = (HttpUrlConnection) context.getConnection();
      connection.addRequestHeader(HEADER_SUBSCRIPTION_KEY, apiKey);
    };
  }

  private static NodoChiediFlussoRendicontazione createFlussoRendicontazioneRequest(BrokerForNodoPaDTO brokerForNodoPaDTO, String reportingId) {
    Broker broker = brokerForNodoPaDTO.getBroker();
    Organization organization = brokerForNodoPaDTO.getOrganization();

    NodoChiediFlussoRendicontazione request = new NodoChiediFlussoRendicontazione();
    request.setIdentificativoDominio(organization.getOrgFiscalCode());
    request.setPassword("password");
    request.setIdentificativoIntermediarioPA(broker.getBrokerFiscalCode());
    request.setIdentificativoStazioneIntermediarioPA(broker.getStationId());
    request.setIdentificativoPSP(null);
    request.setIdentificativoFlusso(reportingId);
    return request;
  }

}
