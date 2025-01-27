package it.gov.pagopa.pu.pagopapayments.service.reporting;

import gov.telematici.pagamenti.ws.NodoChiediElencoFlussiRendicontazione;
import gov.telematici.pagamenti.ws.NodoChiediElencoFlussiRendicontazioneRisposta;
import it.gov.pagopa.pu.organization.dto.generated.Broker;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.pagopapayments.connector.soap.NodeForPaClient;
import it.gov.pagopa.pu.pagopapayments.dto.BrokerForNodoPaDTO;
import it.gov.pagopa.pu.pagopapayments.dto.generated.ReportingIdDTO;
import it.gov.pagopa.pu.pagopapayments.exception.ApplicationException;
import it.gov.pagopa.pu.pagopapayments.mapper.ReportingIdMapper;
import it.gov.pagopa.pu.pagopapayments.service.broker.BrokerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class ReportingService {

  private final NodeForPaClient nodeForPaClient;
  private final BrokerService brokerService;

  public ReportingService(NodeForPaClient nodeForPaClient, BrokerService brokerService) {
    this.nodeForPaClient = nodeForPaClient;
    this.brokerService = brokerService;
  }

  public List<ReportingIdDTO> getReportingList(Long organizationId, String accessToken) {


    BrokerForNodoPaDTO brokerForNodoPaDTO = brokerService.getBrokerForNodoPaDTOByOrganizationId(organizationId, accessToken);
    NodoChiediElencoFlussiRendicontazione request = getNodoChiediElencoFlussiRendicontazione(brokerForNodoPaDTO);

    NodoChiediElencoFlussiRendicontazioneRisposta response = nodeForPaClient.nodoChiediElencoFlussiRendicontazione(request, brokerForNodoPaDTO.getBrokerApiKeys().getSyncKey());

    if (response.getFault() != null) {
      throw new ApplicationException("Error during the call to the payment node " + response.getFault().getFaultCode());
    }

    List<ReportingIdDTO> reportingList = new ArrayList<>();

    response.getElencoFlussiRendicontazione().getIdRendicontaziones().forEach(idRendicontazione -> reportingList.add(ReportingIdMapper.map(idRendicontazione)));


    return reportingList;
  }

  private static NodoChiediElencoFlussiRendicontazione getNodoChiediElencoFlussiRendicontazione(BrokerForNodoPaDTO brokerForNodoPaDTO) {
    Broker broker = brokerForNodoPaDTO.getBroker();
    Organization organization = brokerForNodoPaDTO.getOrganization();

    NodoChiediElencoFlussiRendicontazione request = new NodoChiediElencoFlussiRendicontazione();
    request.setIdentificativoDominio(organization.getOrgFiscalCode());
    request.setPassword("password");
    request.setIdentificativoIntermediarioPA(broker.getBrokerFiscalCode());
    request.setIdentificativoStazioneIntermediarioPA(broker.getBroadcastStationId());
    request.setIdentificativoPSP(null);
    return request;
  }

}
