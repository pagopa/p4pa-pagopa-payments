package it.gov.pagopa.pu.pagopapayments.connector.pagopa.paymentsreporting;

import it.gov.digitpa.schemas._2011.pagamenti.FlussoRiversamento;
import it.gov.pagopa.nodo.fdrorganization.dto.generated.FlowByPSP;
import it.gov.pagopa.nodo.fdrorganization.dto.generated.Payment;
import it.gov.pagopa.nodo.fdrorganization.dto.generated.SingleFlowResponse;
import it.gov.pagopa.pu.pagopapayments.connector.pagopa.paymentsreporting.client.PaymentsReportingClient;
import it.gov.pagopa.pu.pagopapayments.dto.BrokerForNodoPaDTO;
import it.gov.pagopa.pu.pagopapayments.dto.PaPaymentReportingDTO;
import it.gov.pagopa.pu.pagopapayments.dto.generated.PaymentsReportingIdDTO;
import it.gov.pagopa.pu.pagopapayments.mapper.PaymentsReportingMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentsReportingServiceImplTest {

  @Mock
  private PaymentsReportingClient paymentsReportingClient;
  @Mock
  private PaymentsReportingMapper paymentsReportingMapper;

  @InjectMocks
  private PaymentsReportingServiceImpl nodePaymentsReportingService;

  @AfterEach
  void tearDown() {
    Mockito.verifyNoMoreInteractions(
      paymentsReportingClient,
      paymentsReportingMapper
    );
  }

  @Test
  void fetchPaymentReportingIdList() {
    //given
    BrokerForNodoPaDTO brokerForNodoPaDTO = new BrokerForNodoPaDTO();
    OffsetDateTime latestFlowDate = OffsetDateTime.now();

    List<FlowByPSP> flowByPSPList = List.of(new FlowByPSP());
    List<PaymentsReportingIdDTO> expectedResult = List.of(new PaymentsReportingIdDTO());

    when(paymentsReportingClient.fetchIdList(brokerForNodoPaDTO, latestFlowDate))
      .thenReturn(flowByPSPList);
    when(paymentsReportingMapper.mapToIdDtoList(flowByPSPList))
      .thenReturn(expectedResult);

    //when
    List<PaymentsReportingIdDTO> actualResult = nodePaymentsReportingService.fetchPaymentReportingIdList(brokerForNodoPaDTO, latestFlowDate);

    //then
    Assertions.assertEquals(expectedResult, actualResult);
  }

  @Test
  void fetchPaymentReporting() {
    //given
    BrokerForNodoPaDTO brokerForNodoPaDTO = new BrokerForNodoPaDTO();
    String paymentsReportingId = "paymentsReportingId";
    long revision = 1L;
    String pspId = "pspId";

    SingleFlowResponse singleFlowResponse = new SingleFlowResponse();
    List<Payment> paymentsResponse = List.of(new Payment());
    FlussoRiversamento paymentsReporting = new FlussoRiversamento();
    PaPaymentReportingDTO expectedResult = new PaPaymentReportingDTO();

    when(paymentsReportingClient.fetchPaymentReportingFlow(brokerForNodoPaDTO, paymentsReportingId, revision, pspId))
      .thenReturn(singleFlowResponse);
    when(paymentsReportingClient.fetchAllPaymentsForPaymentReportingFlow(brokerForNodoPaDTO, paymentsReportingId, revision, pspId, singleFlowResponse))
      .thenReturn(paymentsResponse);
    when(paymentsReportingMapper.mapPaymentsReporting(singleFlowResponse, paymentsResponse))
      .thenReturn(paymentsReporting);
    when(paymentsReportingMapper.mapPaPaymentsReportingDTO(brokerForNodoPaDTO, paymentsReporting))
      .thenReturn(expectedResult);

    //when
    PaPaymentReportingDTO actualResult = nodePaymentsReportingService.fetchPaymentReporting(brokerForNodoPaDTO, paymentsReportingId, revision, pspId);

    //then
    Assertions.assertEquals(expectedResult, actualResult);
  }

}
