package it.gov.pagopa.pu.pagopapayments.service;

import it.gov.pagopa.pu.organization.dto.generated.Broker;
import it.gov.pagopa.pu.organization.dto.generated.BrokerApiKeys;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.pagopapayments.connector.FileShareClient;
import it.gov.pagopa.pu.pagopapayments.connector.soap.NodeForPaClient;
import it.gov.pagopa.pu.pagopapayments.dto.BrokerForNodoPaDTO;
import it.gov.pagopa.pu.pagopapayments.dto.PaPaymentReporingDTO;
import it.gov.pagopa.pu.pagopapayments.dto.generated.PaymentsReportingIdDTO;
import it.gov.pagopa.pu.pagopapayments.exception.ApplicationException;
import it.gov.pagopa.pu.pagopapayments.service.broker.BrokerService;
import it.gov.pagopa.pu.pagopapayments.service.paymentsreporting.PaymentsReportingService;
import it.gov.pagopa.pu.pagopapayments.util.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentsReportingServiceTest {

  @Mock
  private BrokerService brokerServiceMock;

  @Mock
  private NodeForPaClient nodeForPaClientMock;
  @Mock
  private FileShareClient fileShareClientMock;

  @InjectMocks
  private PaymentsReportingService paymentsReportingService;

  private static final Long ORGANIZATION_ID = 1L;
  private static final String FILE_NAME = "fileName.xml";
  private static final String REPORTING_ID = "2";
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

    ApplicationException exception = Assertions.assertThrows(ApplicationException.class, () -> paymentsReportingService.getPaymentsReportingList(ORGANIZATION_ID, accessToken));
    Assertions.assertEquals("Broker service error", exception.getMessage());
    verify(brokerServiceMock, times(1)).getBrokerForNodoPaDTOByOrganizationId(ORGANIZATION_ID, accessToken);
    verify(nodeForPaClientMock, never()).getPaymentsReportingList(BROKER_FOR_NODO_PA_DTO);
  }

  @Test
  void getReportingList_whenNodeForPaClientReturnsEmptyList_thenReturnEmptyReportingList() {
    String accessToken = TestUtils.getFakeAccessToken();

    List<PaymentsReportingIdDTO> response = new ArrayList<>();

    Mockito.when(brokerServiceMock.getBrokerForNodoPaDTOByOrganizationId(ORGANIZATION_ID, accessToken)).thenReturn(BROKER_FOR_NODO_PA_DTO);
    Mockito.when(nodeForPaClientMock.getPaymentsReportingList(BROKER_FOR_NODO_PA_DTO)).thenReturn(response);

    List<PaymentsReportingIdDTO> result = paymentsReportingService.getPaymentsReportingList(ORGANIZATION_ID, accessToken);

    Assertions.assertNotNull(result);
    Assertions.assertTrue(result.isEmpty());
    verify(brokerServiceMock, times(1)).getBrokerForNodoPaDTOByOrganizationId(ORGANIZATION_ID, accessToken);
    verify(nodeForPaClientMock, times(1)).getPaymentsReportingList(BROKER_FOR_NODO_PA_DTO);
  }

  @Test
  void getReportingList_whenNodeForPaClientThrowsException_thenThrowApplicationException() {
    String accessToken = TestUtils.getFakeAccessToken();

    Mockito.when(brokerServiceMock.getBrokerForNodoPaDTOByOrganizationId(ORGANIZATION_ID, accessToken)).thenReturn(BROKER_FOR_NODO_PA_DTO);
    Mockito.when(nodeForPaClientMock.getPaymentsReportingList(BROKER_FOR_NODO_PA_DTO)).thenThrow(new ApplicationException("Node client error"));

    ApplicationException exception = Assertions.assertThrows(ApplicationException.class, () -> paymentsReportingService.getPaymentsReportingList(ORGANIZATION_ID, accessToken));
    Assertions.assertEquals("Node client error", exception.getMessage());
    verify(brokerServiceMock, times(1)).getBrokerForNodoPaDTOByOrganizationId(ORGANIZATION_ID, accessToken);
    verify(nodeForPaClientMock, times(1)).getPaymentsReportingList(BROKER_FOR_NODO_PA_DTO);
  }


  @Test
  void getPaymentsReportingList_whenNodeForPaClientReturnsNonEmptyList_thenReturnPaymentsReportingList() {
    String accessToken = TestUtils.getFakeAccessToken();

    List<PaymentsReportingIdDTO> response = new ArrayList<>();
    response.add(new PaymentsReportingIdDTO().pagopaPaymentsReportingId("idRendicontazione"));


    Mockito.when(brokerServiceMock.getBrokerForNodoPaDTOByOrganizationId(ORGANIZATION_ID, accessToken)).thenReturn(BROKER_FOR_NODO_PA_DTO);
    Mockito.when(nodeForPaClientMock.getPaymentsReportingList(BROKER_FOR_NODO_PA_DTO)).thenReturn(response);

    List<PaymentsReportingIdDTO> result = paymentsReportingService.getPaymentsReportingList(ORGANIZATION_ID, accessToken);

    Assertions.assertNotNull(result);
    Assertions.assertFalse(result.isEmpty());
    Mockito.verify(brokerServiceMock, times(1)).getBrokerForNodoPaDTOByOrganizationId(ORGANIZATION_ID, accessToken);
    Mockito.verify(nodeForPaClientMock, times(1)).getPaymentsReportingList(BROKER_FOR_NODO_PA_DTO);
  }

  @Test
  void uploadPaymentsReporting_whenValidRequest_thenReturnFileId() {
    String accessToken = TestUtils.getFakeAccessToken();
    PaPaymentReporingDTO response = new PaPaymentReporingDTO();
    response.setIdBrokerPA("brokerCode");
    response.setIdStation("stationId");
    response.setFiscalCode("orgFiscalCode");

    Mockito.when(brokerServiceMock.getBrokerForNodoPaDTOByOrganizationId(ORGANIZATION_ID, accessToken)).thenReturn(BROKER_FOR_NODO_PA_DTO);
    Mockito.when(nodeForPaClientMock.uploadOfPaymentReporting(BROKER_FOR_NODO_PA_DTO, REPORTING_ID)).thenReturn(response);
    Mockito.when(fileShareClientMock.uploadPaymentReporting(response, ORGANIZATION_ID, accessToken)).thenReturn("fileId");

    String result = paymentsReportingService.uploadPaymentsReporting(ORGANIZATION_ID, REPORTING_ID, FILE_NAME, accessToken);

    Assertions.assertNotNull(result);
    Assertions.assertEquals("fileId", result);
    Mockito.verify(brokerServiceMock, Mockito.times(1)).getBrokerForNodoPaDTOByOrganizationId(ORGANIZATION_ID, accessToken);
    Mockito.verify(nodeForPaClientMock, Mockito.times(1)).uploadOfPaymentReporting(BROKER_FOR_NODO_PA_DTO, REPORTING_ID);
    Mockito.verify(fileShareClientMock, Mockito.times(1)).uploadPaymentReporting(response, ORGANIZATION_ID, accessToken);
  }

  @Test
  void uploadPaymentsReporting_whenBrokerServiceThrowsException_thenThrowApplicationException() {
    String accessToken = TestUtils.getFakeAccessToken();
    Mockito.when(brokerServiceMock.getBrokerForNodoPaDTOByOrganizationId(ORGANIZATION_ID, accessToken)).thenThrow(new ApplicationException("Broker service error"));

    ApplicationException exception = Assertions.assertThrows(ApplicationException.class, () -> paymentsReportingService.uploadPaymentsReporting(ORGANIZATION_ID, REPORTING_ID, FILE_NAME, accessToken));
    Assertions.assertEquals("Broker service error", exception.getMessage());
    Mockito.verify(brokerServiceMock, Mockito.times(1)).getBrokerForNodoPaDTOByOrganizationId(ORGANIZATION_ID, accessToken);
    Mockito.verify(nodeForPaClientMock, Mockito.never()).uploadOfPaymentReporting(Mockito.any(), Mockito.any());
    Mockito.verify(fileShareClientMock, Mockito.never()).uploadPaymentReporting(Mockito.any(), Mockito.any(), Mockito.any());
  }

  @Test
  void uploadPaymentsReporting_whenNodeForPaClientThrowsException_thenThrowApplicationException() {
    String accessToken = TestUtils.getFakeAccessToken();
    Mockito.when(brokerServiceMock.getBrokerForNodoPaDTOByOrganizationId(ORGANIZATION_ID, accessToken)).thenReturn(BROKER_FOR_NODO_PA_DTO);
    Mockito.when(nodeForPaClientMock.uploadOfPaymentReporting(BROKER_FOR_NODO_PA_DTO, REPORTING_ID)).thenThrow(new ApplicationException("Node client error"));

    ApplicationException exception = Assertions.assertThrows(ApplicationException.class, () -> paymentsReportingService.uploadPaymentsReporting(ORGANIZATION_ID, REPORTING_ID, FILE_NAME, accessToken));
    Assertions.assertEquals("Node client error", exception.getMessage());
    Mockito.verify(brokerServiceMock, Mockito.times(1)).getBrokerForNodoPaDTOByOrganizationId(ORGANIZATION_ID, accessToken);
    Mockito.verify(nodeForPaClientMock, Mockito.times(1)).uploadOfPaymentReporting(BROKER_FOR_NODO_PA_DTO, REPORTING_ID);
    Mockito.verify(fileShareClientMock, Mockito.never()).uploadPaymentReporting(Mockito.any(), Mockito.any(), Mockito.any());
  }

  @Test
  void uploadPaymentsReporting_whenFileShareClientThrowsException_thenThrowApplicationException() {
    String accessToken = TestUtils.getFakeAccessToken();
    PaPaymentReporingDTO response = new PaPaymentReporingDTO();
    response.setIdBrokerPA("brokerCode");
    response.setIdStation("stationId");
    response.setFiscalCode("orgFiscalCode");

    Mockito.when(brokerServiceMock.getBrokerForNodoPaDTOByOrganizationId(ORGANIZATION_ID, accessToken)).thenReturn(BROKER_FOR_NODO_PA_DTO);
    Mockito.when(nodeForPaClientMock.uploadOfPaymentReporting(BROKER_FOR_NODO_PA_DTO, REPORTING_ID)).thenReturn(response);
    Mockito.when(fileShareClientMock.uploadPaymentReporting(response, ORGANIZATION_ID, accessToken)).thenThrow(new ApplicationException("File share client error"));

    ApplicationException exception = Assertions.assertThrows(ApplicationException.class, () -> paymentsReportingService.uploadPaymentsReporting(ORGANIZATION_ID, REPORTING_ID, FILE_NAME, accessToken));
    Assertions.assertEquals("File share client error", exception.getMessage());
    Mockito.verify(brokerServiceMock, Mockito.times(1)).getBrokerForNodoPaDTOByOrganizationId(ORGANIZATION_ID, accessToken);
    Mockito.verify(nodeForPaClientMock, Mockito.times(1)).uploadOfPaymentReporting(BROKER_FOR_NODO_PA_DTO, REPORTING_ID);
    Mockito.verify(fileShareClientMock, Mockito.times(1)).uploadPaymentReporting(response, ORGANIZATION_ID, accessToken);
  }


}
