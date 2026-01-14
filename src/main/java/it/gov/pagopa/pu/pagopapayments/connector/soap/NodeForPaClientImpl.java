package it.gov.pagopa.pu.pagopapayments.connector.soap;

import gov.telematici.pagamenti.ws.NodoChiediElencoFlussiRendicontazione;
import gov.telematici.pagamenti.ws.NodoChiediElencoFlussiRendicontazioneRisposta;
import gov.telematici.pagamenti.ws.NodoChiediFlussoRendicontazione;
import gov.telematici.pagamenti.ws.NodoChiediFlussoRendicontazioneRisposta;
import it.gov.pagopa.pu.pagopapayments.connector.soap.mapper.NodoChiediFlussoRendicontazioneMapper;
import it.gov.pagopa.pu.pagopapayments.dto.BrokerForNodoPaDTO;
import it.gov.pagopa.pu.pagopapayments.dto.PaPaymentReportingDTO;
import it.gov.pagopa.pu.pagopapayments.dto.generated.PaymentsReportingIdDTO;
import it.gov.pagopa.pu.pagopapayments.exception.ApplicationException;
import it.gov.pagopa.pu.pagopapayments.mapper.PaymentsReportingMapper;
import it.gov.pagopa.pu.pagopapayments.registry.RegistryContextData;
import it.gov.pagopa.pu.pagopapayments.registry.RegistryEventType;
import it.gov.pagopa.pu.pagopapayments.registry.RegistryLogger;
import it.gov.pagopa.pu.registries.dto.generated.RegistryOutcome;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.client.core.WebServiceMessageCallback;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.transport.context.TransportContext;
import org.springframework.ws.transport.context.TransportContextHolder;
import org.springframework.ws.transport.http.HttpUrlConnection;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
public class NodeForPaClientImpl extends WebServiceGatewaySupport implements NodeForPaClient {

  public static final String HEADER_SUBSCRIPTION_KEY = "Ocp-Apim-Subscription-Key";
  public static final String ERROR_MESSAGE = "Error during the call to the payment node ";

  private final RegistryLogger registryLogger;
  private final NodoChiediFlussoRendicontazioneMapper fetchPaymentsReportingRequestMapper;

  public NodeForPaClientImpl(
    String defaultUri,
    Jaxb2Marshaller marshaller,
    NodoChiediFlussoRendicontazioneMapper fetchPaymentsReportingRequestMapper,
    RegistryLogger registryLogger
  ) {
    this.registryLogger = registryLogger;
    this.fetchPaymentsReportingRequestMapper = fetchPaymentsReportingRequestMapper;

    setDefaultUri(defaultUri);
    setMarshaller(marshaller);
    setUnmarshaller(marshaller);
  }

  @Override
  public List<PaymentsReportingIdDTO> getPaymentsReportingList(BrokerForNodoPaDTO brokerForNodoPaDTO) {
    NodoChiediElencoFlussiRendicontazione request = createElencoFlussiRendicontazioneRequest(brokerForNodoPaDTO);
    NodoChiediElencoFlussiRendicontazioneRisposta response = (NodoChiediElencoFlussiRendicontazioneRisposta)
      getWebServiceTemplate().marshalSendAndReceive(request, getMessageCallback(brokerForNodoPaDTO.getBrokerApiKeys().getSyncKey(), "nodoChiediElencoFlussiRendicontazione"));

    if (response != null && response.getFault() != null) {
      if(response.getFault().getFaultCode().equals("PPT_DOMINIO_SCONOSCIUTO")) {
        log.info("Retrieved fault code PPT_DOMINIO_SCONOSCIUTO for org {}. Returning empty list",brokerForNodoPaDTO.getOrganization().getOrgFiscalCode());
        return Collections.emptyList();
      }
      else
        throw new ApplicationException(ERROR_MESSAGE + response.getFault().getFaultCode());
    }

    List<PaymentsReportingIdDTO> reportingList = new ArrayList<>();
    if(response == null || response.getElencoFlussiRendicontazione() == null) {
      log.info("No PaymentsReportingId found for org {}. Returning empty list",brokerForNodoPaDTO.getOrganization().getOrgFiscalCode());
      return Collections.emptyList();
    }
    response.getElencoFlussiRendicontazione().getIdRendicontaziones().forEach(
      idRendicontazione -> reportingList.add(
        PaymentsReportingMapper.mapIdDto(idRendicontazione)
      )
    );

    return reportingList;
  }

public PaPaymentReportingDTO fetchPaymentReporting(BrokerForNodoPaDTO brokerForNodoPaDTO, String reportingId) {
  NodoChiediFlussoRendicontazione request = fetchPaymentsReportingRequestMapper.createFlussoRendicontazioneRequest(brokerForNodoPaDTO, reportingId);

  byte[] bytes = fetchPaymentReporting(brokerForNodoPaDTO, request);

  return PaPaymentReportingDTO.builder()
    .idPA(brokerForNodoPaDTO.getOrganization().getOrgFiscalCode())
    .idBrokerPA(brokerForNodoPaDTO.getBroker().getBrokerFiscalCode())
    .idStation(brokerForNodoPaDTO.getBroker().getStationId())
    .fiscalCode(brokerForNodoPaDTO.getOrganization().getOrgFiscalCode())
    .paymentReportingBytes(bytes)
    .build();
  }

  private byte[] fetchPaymentReporting(BrokerForNodoPaDTO brokerForNodoPaDTO, NodoChiediFlussoRendicontazione request) {
    RegistryContextData contextData = RegistryContextData.builder()
      .orgFiscalCode(request.getIdentificativoDominio())
      .pspId(request.getIdentificativoPSP())
      .brokerStationId(request.getIdentificativoStazioneIntermediarioPA())
      .eventType(RegistryEventType.NodeForPa_fetchPaymentReporting)
      .build();

    byte[][] xmlBytes = new byte[1][];
    Exception[] xmlReadingException = new Exception[1];
    NodoChiediFlussoRendicontazioneRisposta response = registryLogger.execute(
      contextData,
      request,
      () -> {
        NodoChiediFlussoRendicontazioneRisposta out = (NodoChiediFlussoRendicontazioneRisposta)
          getWebServiceTemplate().marshalSendAndReceive(request, getMessageCallback(brokerForNodoPaDTO.getBrokerApiKeys().getSyncKey(), "nodoChiediFlussoRendicontazione"));
        return Triple.of(out, null,
          out.getFault()==null
            ? RegistryOutcome.OK
          : RegistryOutcome.KO);
      },
      null,
      null,
      out -> {
        if (out.getFault() == null) {
          try (InputStream inputStream = out.getXmlRendicontazione().getInputStream()) {
            xmlBytes[0] = inputStream.readAllBytes();
            return Map.of(
              RegistryLogger.SKIP_PAYLOAD_KEY, true,
              "xml", new String(xmlBytes[0]));
          } catch (Exception e) {
            xmlReadingException[0] = e;
          }
        }
        return Map.of();
      });

    if (response.getFault() != null) {
      throw new ApplicationException(ERROR_MESSAGE + response.getFault().getFaultCode());
    }
    if(xmlReadingException[0] != null){
      throw new ApplicationException(ERROR_MESSAGE + xmlReadingException[0].getMessage());
    }
    return xmlBytes[0];
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

}
