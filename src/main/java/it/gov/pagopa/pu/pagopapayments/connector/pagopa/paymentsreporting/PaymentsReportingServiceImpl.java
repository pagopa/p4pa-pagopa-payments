package it.gov.pagopa.pu.pagopapayments.connector.pagopa.paymentsreporting;

import it.gov.digitpa.schemas._2011.pagamenti.FlussoRiversamento;
import it.gov.pagopa.nodo.fdrorganization.dto.generated.ErrorResponse;
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
import org.springframework.web.client.HttpClientErrorException;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;

import static it.gov.pagopa.pu.pagopapayments.util.Constants.INVALID_EC_FISCAL_CODE_ERROR_CODE;

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
    List<FlowByPSP> flowByPSPList;
    try {
      flowByPSPList = paymentsReportingClient.fetchIdList(brokerForNodoPaDTO, latestFlowDate);
    } catch (HttpClientErrorException.BadRequest ex) {
      ErrorResponse exceptionResponse = ex.getResponseBodyAs(ErrorResponse.class);
      if(exceptionResponse==null || !INVALID_EC_FISCAL_CODE_ERROR_CODE.equals(exceptionResponse.getAppErrorCode())) {
        throw ex;
      }
      String exceptionMessage = exceptionResponse.getErrors() != null ?
        exceptionResponse.getErrors().getFirst().getMessage() :
        "Creditor institution with ID [%s] is invalid or unknown."
          .formatted(brokerForNodoPaDTO.getOrganizationStation().getOrgFiscalCode());
      log.warn("{} Returning empty list", exceptionMessage);
      return Collections.emptyList();
    }
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
