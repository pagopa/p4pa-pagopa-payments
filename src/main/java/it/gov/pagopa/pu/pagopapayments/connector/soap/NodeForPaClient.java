package it.gov.pagopa.pu.pagopapayments.connector.soap;

import it.gov.pagopa.pu.pagopapayments.dto.BrokerForNodoPaDTO;
import it.gov.pagopa.pu.pagopapayments.dto.PaPaymentReporingDTO;
import it.gov.pagopa.pu.pagopapayments.dto.generated.PaymentsReportingIdDTO;

import java.util.List;

public interface NodeForPaClient {
  List<PaymentsReportingIdDTO> getPaymentsReportingList(BrokerForNodoPaDTO brokerForNodoPaDTO);
  PaPaymentReporingDTO uploadOfPaymentReporting(BrokerForNodoPaDTO brokerForNodoPaDTO, String reportingId);
}
