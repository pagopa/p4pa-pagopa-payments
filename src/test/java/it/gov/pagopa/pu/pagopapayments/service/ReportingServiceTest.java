package it.gov.pagopa.pu.pagopapayments.service;

import gov.telematici.pagamenti.ws.TipoElencoFlussiRendicontazione;
import gov.telematici.pagamenti.ws.TipoIdRendicontazione;
import it.gov.pagopa.pu.organization.dto.generated.Broker;
import it.gov.pagopa.pu.organization.dto.generated.BrokerApiKeys;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.pagopapayments.connector.soap.NodeForPaClient;
import it.gov.pagopa.pu.pagopapayments.dto.BrokerForNodoPaDTO;
import it.gov.pagopa.pu.pagopapayments.dto.generated.ReportingIdDTO;
import it.gov.pagopa.pu.pagopapayments.exception.ApplicationException;
import it.gov.pagopa.pu.pagopapayments.service.broker.BrokerService;
import gov.telematici.pagamenti.ws.NodoChiediElencoFlussiRendicontazione;
import gov.telematici.pagamenti.ws.NodoChiediElencoFlussiRendicontazioneRisposta;
import it.gov.pagopa.pu.pagopapayments.service.reporting.ReportingService;
import it.gov.pagopa.pu.pagopapayments.util.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportingServiceTest {

  @Mock
  private BrokerService brokerServiceMock;

  @Mock
  private NodeForPaClient nodeForPaClientMock;

  @InjectMocks
  private ReportingService reportingService;

  private static final Long ORGANIZATION_ID = 1L;
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
  void getReportingList_whenBrokerServiceThrowsException_thenThrowApplicationException() {
    String accessToken = TestUtils.getFakeAccessToken();

    Mockito.when(brokerServiceMock.getBrokerForNodoPaDTOByOrganizationId(ORGANIZATION_ID, accessToken)).thenThrow(new ApplicationException("Broker service error"));

    ApplicationException exception = Assertions.assertThrows(ApplicationException.class, () -> reportingService.getReportingList(ORGANIZATION_ID, accessToken));
    Assertions.assertEquals("Broker service error", exception.getMessage());
    verify(brokerServiceMock, times(1)).getBrokerForNodoPaDTOByOrganizationId(ORGANIZATION_ID, accessToken);
    verify(nodeForPaClientMock, never()).nodoChiediElencoFlussiRendicontazione(any(NodoChiediElencoFlussiRendicontazione.class), anyString());
  }

  @Test
  void getReportingList_whenNodeForPaClientReturnsEmptyList_thenReturnEmptyReportingList() {
    String accessToken = TestUtils.getFakeAccessToken();

    NodoChiediElencoFlussiRendicontazioneRisposta response = new NodoChiediElencoFlussiRendicontazioneRisposta();
    response.setElencoFlussiRendicontazione(new TipoElencoFlussiRendicontazione());

    Mockito.when(brokerServiceMock.getBrokerForNodoPaDTOByOrganizationId(ORGANIZATION_ID, accessToken)).thenReturn(BROKER_FOR_NODO_PA_DTO);
    Mockito.when(nodeForPaClientMock.nodoChiediElencoFlussiRendicontazione(any(NodoChiediElencoFlussiRendicontazione.class), eq("syncKey"))).thenReturn(response);

    List<ReportingIdDTO> result = reportingService.getReportingList(ORGANIZATION_ID, accessToken);

    Assertions.assertNotNull(result);
    Assertions.assertTrue(result.isEmpty());
    verify(brokerServiceMock, times(1)).getBrokerForNodoPaDTOByOrganizationId(ORGANIZATION_ID, accessToken);
    verify(nodeForPaClientMock, times(1)).nodoChiediElencoFlussiRendicontazione(any(NodoChiediElencoFlussiRendicontazione.class), eq("syncKey"));
  }

  @Test
  void getReportingList_whenNodeForPaClientThrowsException_thenThrowApplicationException() {
    String accessToken = TestUtils.getFakeAccessToken();

    Mockito.when(brokerServiceMock.getBrokerForNodoPaDTOByOrganizationId(ORGANIZATION_ID, accessToken)).thenReturn(BROKER_FOR_NODO_PA_DTO);
    Mockito.when(nodeForPaClientMock.nodoChiediElencoFlussiRendicontazione(any(NodoChiediElencoFlussiRendicontazione.class), eq("syncKey"))).thenThrow(new ApplicationException("Node client error"));

    ApplicationException exception = Assertions.assertThrows(ApplicationException.class, () -> reportingService.getReportingList(ORGANIZATION_ID, accessToken));
    Assertions.assertEquals("Node client error", exception.getMessage());
    verify(brokerServiceMock, times(1)).getBrokerForNodoPaDTOByOrganizationId(ORGANIZATION_ID, accessToken);
    verify(nodeForPaClientMock, times(1)).nodoChiediElencoFlussiRendicontazione(any(NodoChiediElencoFlussiRendicontazione.class), eq("syncKey"));
  }


  @Test
  void getReportingList_whenNodeForPaClientReturnsNonEmptyList_thenReturnReportingList() {
    String accessToken = TestUtils.getFakeAccessToken();


    NodoChiediElencoFlussiRendicontazioneRisposta response = new NodoChiediElencoFlussiRendicontazioneRisposta();
    TipoElencoFlussiRendicontazione elenco = new TipoElencoFlussiRendicontazione();
    elenco.getIdRendicontaziones().add(new TipoIdRendicontazione());
    response.setElencoFlussiRendicontazione(elenco);

    Mockito.when(brokerServiceMock.getBrokerForNodoPaDTOByOrganizationId(ORGANIZATION_ID, accessToken)).thenReturn(BROKER_FOR_NODO_PA_DTO);
    Mockito.when(nodeForPaClientMock.nodoChiediElencoFlussiRendicontazione(any(NodoChiediElencoFlussiRendicontazione.class), eq("syncKey"))).thenReturn(response);

    List<ReportingIdDTO> result = reportingService.getReportingList(ORGANIZATION_ID, accessToken);

    Assertions.assertNotNull(result);
    Assertions.assertFalse(result.isEmpty());
    Mockito.verify(brokerServiceMock, times(1)).getBrokerForNodoPaDTOByOrganizationId(ORGANIZATION_ID, accessToken);
    Mockito.verify(nodeForPaClientMock, times(1)).nodoChiediElencoFlussiRendicontazione(any(NodoChiediElencoFlussiRendicontazione.class), eq("syncKey"));
  }

}
