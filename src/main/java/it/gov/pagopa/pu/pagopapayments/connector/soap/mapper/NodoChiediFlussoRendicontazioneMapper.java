package it.gov.pagopa.pu.pagopapayments.connector.soap.mapper;

import gov.telematici.pagamenti.ws.NodoChiediFlussoRendicontazione;
import it.gov.pagopa.pu.organization.dto.generated.Broker;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.pagopapayments.dto.BrokerForNodoPaDTO;
import org.springframework.stereotype.Service;

@Service
public class NodoChiediFlussoRendicontazioneMapper {

  public NodoChiediFlussoRendicontazione createFlussoRendicontazioneRequest(BrokerForNodoPaDTO brokerForNodoPaDTO, String reportingId) {
    Broker broker = brokerForNodoPaDTO.getBroker();
    Organization organization = brokerForNodoPaDTO.getOrganization();

    NodoChiediFlussoRendicontazione request = new NodoChiediFlussoRendicontazione();
    request.setIdentificativoDominio(organization.getOrgFiscalCode());
    request.setPassword("password");
    request.setIdentificativoIntermediarioPA(broker.getBrokerFiscalCode());
    request.setIdentificativoStazioneIntermediarioPA(broker.getDefaultStationId());
    request.setIdentificativoPSP(null);
    request.setIdentificativoFlusso(reportingId);
    return request;
  }
}
