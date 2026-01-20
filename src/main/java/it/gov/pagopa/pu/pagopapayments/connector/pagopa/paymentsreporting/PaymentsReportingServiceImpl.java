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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@Service
public class PaymentsReportingServiceImpl implements PaymentsReportingService {

  private final PaymentsReportingClient paymentsReportingClient;
  private final PaymentsReportingMapper paymentsReportingMapper;

  public PaymentsReportingServiceImpl(PaymentsReportingClient paymentsReportingClient, PaymentsReportingMapper paymentsReportingMapper) {
    this.paymentsReportingClient = paymentsReportingClient;
    this.paymentsReportingMapper = paymentsReportingMapper;
  }

  @Override
  public List<PaymentsReportingIdDTO> fetchPaymentReportingIdList(BrokerForNodoPaDTO brokerForNodoPaDTO, OffsetDateTime latestFlowDate) {
    List<FlowByPSP> flowByPSPList = paymentsReportingClient.fetchIdList(brokerForNodoPaDTO, latestFlowDate);
    return paymentsReportingMapper.mapToIdDtoList(flowByPSPList);
  }

  @Override
  public PaPaymentReportingDTO fetchPaymentReporting(BrokerForNodoPaDTO brokerForNodoPaDTO, String paymentsReportingId, Long revision, String pspId) {
    SingleFlowResponse singleFlowResponse = paymentsReportingClient.fetchPaymentReportingFlow(
      brokerForNodoPaDTO,
      paymentsReportingId,
      revision,
      pspId
    );
    List<Payment> paymentsResponse = paymentsReportingClient.fetchAllPaymentsForPaymentReportingFlow(
      brokerForNodoPaDTO,
      paymentsReportingId,
      revision,
      pspId,
      singleFlowResponse
    );
    FlussoRiversamento paymentsReporting = paymentsReportingMapper.mapPaymentsReporting(
      singleFlowResponse,
      paymentsResponse
    );
    return paymentsReportingMapper.mapPaPaymentsReportingDTO(
      brokerForNodoPaDTO,
      paymentsReporting
    );
  }
}
