package it.gov.pagopa.pu.pagopapayments.service.paymentsreporting;

import it.gov.pagopa.pu.pagopapayments.connector.FileShareClient;
import it.gov.pagopa.pu.pagopapayments.connector.soap.NodeForPaClient;
import it.gov.pagopa.pu.pagopapayments.dto.BrokerForNodoPaDTO;
import it.gov.pagopa.pu.pagopapayments.dto.PaPaymentReportingDTO;
import it.gov.pagopa.pu.pagopapayments.dto.generated.PaymentsReportingIdDTO;
import it.gov.pagopa.pu.pagopapayments.service.broker.BrokerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class PaymentsReportingService {

  private final NodeForPaClient nodeForPaClient;
  private final BrokerService brokerService;
  private final FileShareClient fileShareClient;

  public PaymentsReportingService(NodeForPaClient nodeForPaClient, BrokerService brokerService, FileShareClient fileShareClient) {
    this.nodeForPaClient = nodeForPaClient;
    this.brokerService = brokerService;
      this.fileShareClient = fileShareClient;
  }

  public List<PaymentsReportingIdDTO> getPaymentsReportingList(Long organizationId, String accessToken) {
    BrokerForNodoPaDTO brokerForNodoPaDTO = brokerService.getBrokerForNodoPaDTOByOrganizationId(organizationId, accessToken);
    return nodeForPaClient.getPaymentsReportingList(brokerForNodoPaDTO);
  }

  public String fetchPaymentReporting(Long organizationId, String reportingId, String accessToken) {

    BrokerForNodoPaDTO brokerForNodoPaDTO = brokerService.getBrokerForNodoPaDTOByOrganizationId(organizationId, accessToken);
    PaPaymentReportingDTO response = nodeForPaClient.fetchPaymentReporting(brokerForNodoPaDTO, reportingId);
    return fileShareClient.uploadPaymentReporting(response, organizationId, accessToken);
  }

}
