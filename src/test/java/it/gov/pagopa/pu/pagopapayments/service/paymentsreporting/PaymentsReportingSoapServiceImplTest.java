package it.gov.pagopa.pu.pagopapayments.service.paymentsreporting;

import it.gov.pagopa.pu.organization.dto.generated.Broker;
import it.gov.pagopa.pu.organization.dto.generated.BrokerApiKeys;
import it.gov.pagopa.pu.organization.dto.generated.OrganizationStationDTO;
import it.gov.pagopa.pu.pagopapayments.connector.fileshare.FileShareService;
import it.gov.pagopa.pu.pagopapayments.connector.soap.NodeForPaClient;
import it.gov.pagopa.pu.pagopapayments.dto.BrokerForNodoPaDTO;
import it.gov.pagopa.pu.pagopapayments.dto.PaPaymentReportingDTO;
import it.gov.pagopa.pu.pagopapayments.dto.generated.PaymentsReportingIdDTO;
import it.gov.pagopa.pu.pagopapayments.exception.ApplicationException;
import it.gov.pagopa.pu.pagopapayments.exception.InvalidValueException;
import it.gov.pagopa.pu.pagopapayments.mapper.PaymentsReportingMapper;
import it.gov.pagopa.pu.pagopapayments.service.broker.BrokerRetrieverService;
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
class PaymentsReportingSoapServiceImplTest {

  @Mock
  private BrokerRetrieverService brokerRetrieverServiceMock;

  @Mock
  private NodeForPaClient nodeForPaClientMock;
  @Mock
  private FileShareService fileShareServiceMock;
  @Mock
  private PaymentsReportingMapper paymentsReportingMapper;

  @InjectMocks
  private PaymentsReportingSoapServiceImpl paymentsReportingSoapServiceImpl;

  private static final Long ORGANIZATION_ID = 1L;
  private static final String REPORTING_ID = "2";
  private static final Broker BROKER = new Broker()
    .brokerFiscalCode("brokerCode")
    .broadcastStationId("stationId");

  private static final BrokerApiKeys BROKER_API_KEYS = new BrokerApiKeys()
    .syncKey("syncKey");
  private static final OrganizationStationDTO ORGANIZATION_STATION_DTO = new OrganizationStationDTO()
    .orgFiscalCode("orgFiscalCode");

  private static final BrokerForNodoPaDTO BROKER_FOR_NODO_PA_DTO = BrokerForNodoPaDTO.builder()
    .broker(BROKER)
    .brokerApiKeys(BROKER_API_KEYS)
    .organizationStation(ORGANIZATION_STATION_DTO)
    .build();


  @Test
  void getReportingList_whenBrokerServiceThrowsException_thenThrowApplicationException() {
    String accessToken = TestUtils.getFakeAccessToken();

    Mockito.when(brokerRetrieverServiceMock.getBrokerForNodoPaDTOByOrganizationId(ORGANIZATION_ID, accessToken))
      .thenThrow(new ApplicationException("ERRORCODE", "Broker service error"));

    ApplicationException exception = Assertions.assertThrows(ApplicationException.class, () -> paymentsReportingSoapServiceImpl.getPaymentsReportingList(ORGANIZATION_ID, accessToken));
    Assertions.assertEquals("Broker service error", exception.getMessage());
    verify(brokerRetrieverServiceMock, times(1)).getBrokerForNodoPaDTOByOrganizationId(ORGANIZATION_ID, accessToken);
    verify(nodeForPaClientMock, never()).getPaymentsReportingList(BROKER_FOR_NODO_PA_DTO);
  }

  @Test
  void getReportingList_whenNodeForPaClientReturnsEmptyList_thenReturnEmptyReportingList() {
    String accessToken = TestUtils.getFakeAccessToken();

    List<PaymentsReportingIdDTO> response = new ArrayList<>();

    Mockito.when(brokerRetrieverServiceMock.getBrokerForNodoPaDTOByOrganizationId(ORGANIZATION_ID, accessToken)).thenReturn(BROKER_FOR_NODO_PA_DTO);
    Mockito.when(nodeForPaClientMock.getPaymentsReportingList(BROKER_FOR_NODO_PA_DTO)).thenReturn(response);

    List<PaymentsReportingIdDTO> result = paymentsReportingSoapServiceImpl.getPaymentsReportingList(ORGANIZATION_ID, accessToken);

    Assertions.assertNotNull(result);
    Assertions.assertTrue(result.isEmpty());
    verify(brokerRetrieverServiceMock, times(1)).getBrokerForNodoPaDTOByOrganizationId(ORGANIZATION_ID, accessToken);
    verify(nodeForPaClientMock, times(1)).getPaymentsReportingList(BROKER_FOR_NODO_PA_DTO);
  }

  @Test
  void getReportingList_whenNodeForPaClientThrowsException_thenThrowApplicationException() {
    String accessToken = TestUtils.getFakeAccessToken();

    Mockito.when(brokerRetrieverServiceMock.getBrokerForNodoPaDTOByOrganizationId(ORGANIZATION_ID, accessToken)).thenReturn(BROKER_FOR_NODO_PA_DTO);
    Mockito.when(nodeForPaClientMock.getPaymentsReportingList(BROKER_FOR_NODO_PA_DTO))
      .thenThrow(new ApplicationException("ERRORCODE", "Node client error"));

    ApplicationException exception = Assertions.assertThrows(ApplicationException.class, () -> paymentsReportingSoapServiceImpl.getPaymentsReportingList(ORGANIZATION_ID, accessToken));
    Assertions.assertEquals("Node client error", exception.getMessage());
    verify(brokerRetrieverServiceMock, times(1)).getBrokerForNodoPaDTOByOrganizationId(ORGANIZATION_ID, accessToken);
    verify(nodeForPaClientMock, times(1)).getPaymentsReportingList(BROKER_FOR_NODO_PA_DTO);
  }


  @Test
  void getPaymentsReportingList_whenNodeForPaClientReturnsNonEmptyList_thenReturnPaymentsReportingList() {
    String accessToken = TestUtils.getFakeAccessToken();

    List<PaymentsReportingIdDTO> response = new ArrayList<>();
    response.add(new PaymentsReportingIdDTO().pagopaPaymentsReportingId("idRendicontazione"));


    Mockito.when(brokerRetrieverServiceMock.getBrokerForNodoPaDTOByOrganizationId(ORGANIZATION_ID, accessToken)).thenReturn(BROKER_FOR_NODO_PA_DTO);
    Mockito.when(nodeForPaClientMock.getPaymentsReportingList(BROKER_FOR_NODO_PA_DTO)).thenReturn(response);

    List<PaymentsReportingIdDTO> result = paymentsReportingSoapServiceImpl.getPaymentsReportingList(ORGANIZATION_ID, accessToken);

    Assertions.assertNotNull(result);
    Assertions.assertFalse(result.isEmpty());
    Mockito.verify(brokerRetrieverServiceMock, times(1)).getBrokerForNodoPaDTOByOrganizationId(ORGANIZATION_ID, accessToken);
    Mockito.verify(nodeForPaClientMock, times(1)).getPaymentsReportingList(BROKER_FOR_NODO_PA_DTO);
  }

  @Test
  void uploadPaymentsReporting_whenValidRequest_thenReturnFileId() {
    String accessToken = TestUtils.getFakeAccessToken();
    String fileName = REPORTING_ID + "fileName.xml";
    PaPaymentReportingDTO response = new PaPaymentReportingDTO();
    response.setIdBrokerPA("brokerCode");
    response.setIdStation("stationId");
    response.setFiscalCode("orgFiscalCode");
    Long ingestionFlowFileId = 1L;

    Mockito.when(paymentsReportingMapper.isFilenameInvalid(fileName, REPORTING_ID)).thenReturn(false);
    Mockito.when(brokerRetrieverServiceMock.getBrokerForNodoPaDTOByOrganizationId(ORGANIZATION_ID, accessToken)).thenReturn(BROKER_FOR_NODO_PA_DTO);
    Mockito.when(nodeForPaClientMock.fetchPaymentReporting(BROKER_FOR_NODO_PA_DTO, REPORTING_ID)).thenReturn(response);
    Mockito.when(fileShareServiceMock.uploadPaymentReporting(response, ORGANIZATION_ID, fileName, accessToken)).thenReturn(ingestionFlowFileId);

    Long result = paymentsReportingSoapServiceImpl.fetchPaymentReporting(ORGANIZATION_ID, REPORTING_ID, fileName, accessToken);

    Assertions.assertNotNull(result);
    Assertions.assertEquals(ingestionFlowFileId, result);
    Mockito.verify(paymentsReportingMapper, Mockito.times(1)).isFilenameInvalid(fileName,REPORTING_ID);
    Mockito.verify(brokerRetrieverServiceMock, Mockito.times(1)).getBrokerForNodoPaDTOByOrganizationId(ORGANIZATION_ID, accessToken);
    Mockito.verify(nodeForPaClientMock, Mockito.times(1)).fetchPaymentReporting(BROKER_FOR_NODO_PA_DTO, REPORTING_ID);
    Mockito.verify(fileShareServiceMock, Mockito.times(1)).uploadPaymentReporting(response, ORGANIZATION_ID, fileName, accessToken);
  }

  @Test
  void uploadPaymentsReporting_whenInvalidFileName_thenThrowInvalidValueException() {
    String accessToken = TestUtils.getFakeAccessToken();
    String fileName = "fileName.xml";

    Mockito.when(paymentsReportingMapper.isFilenameInvalid(fileName, REPORTING_ID)).thenReturn(true);

    Assertions.assertThrows(InvalidValueException.class, () -> paymentsReportingSoapServiceImpl
      .fetchPaymentReporting(ORGANIZATION_ID, REPORTING_ID, fileName, accessToken));
    Mockito.verify(paymentsReportingMapper, Mockito.times(1)).isFilenameInvalid(fileName,REPORTING_ID);
  }


}
