package it.gov.pagopa.pu.pagopapayments.service.paymentsreporting;

import it.gov.pagopa.pu.pagopapayments.dto.generated.PaymentsReportingIdDTO;

import java.time.OffsetDateTime;
import java.util.List;

public interface PaymentsReportingService {
  List<PaymentsReportingIdDTO> getPaymentsReportingList(Long organizationId, OffsetDateTime latestFlowDate, String accessToken);
  Long fetchPaymentReporting(Long organizationId, String paymentsReportingId, Long revision, String pspId, String fileName, String accessToken);
}
