package it.gov.pagopa.pu.pagopapayments.connector.pagopa.paymentsreporting;

import it.gov.pagopa.pu.pagopapayments.dto.BrokerForNodoPaDTO;
import it.gov.pagopa.pu.pagopapayments.dto.PaPaymentReportingDTO;
import it.gov.pagopa.pu.pagopapayments.dto.generated.PaymentsReportingIdDTO;

import java.time.OffsetDateTime;
import java.util.List;

public interface NodePaymentsReportingService {
  List<PaymentsReportingIdDTO> fetchPaymentReportingIdList(BrokerForNodoPaDTO brokerForNodoPaDTO, OffsetDateTime latestFlowDate);
  PaPaymentReportingDTO fetchPaymentReporting(BrokerForNodoPaDTO brokerForNodoPaDTO, String paymentsReportingId, Long revision, String pspId);
}
