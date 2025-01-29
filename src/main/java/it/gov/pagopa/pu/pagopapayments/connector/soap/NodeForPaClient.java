package it.gov.pagopa.pu.pagopapayments.connector.soap;

import it.gov.pagopa.pu.pagopapayments.dto.BrokerForNodoPaDTO;
import it.gov.pagopa.pu.pagopapayments.dto.generated.ReportingIdDTO;

import java.util.List;

public interface NodeForPaClient {
  List<ReportingIdDTO> getPaymentsReportingList(BrokerForNodoPaDTO brokerForNodoPaDTO);
}
