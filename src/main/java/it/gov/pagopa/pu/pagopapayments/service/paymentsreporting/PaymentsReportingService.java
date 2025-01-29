package it.gov.pagopa.pu.pagopapayments.service.paymentsreporting;

import it.gov.pagopa.pu.pagopapayments.connector.soap.NodeForPaClient;
import it.gov.pagopa.pu.pagopapayments.dto.BrokerForNodoPaDTO;
import it.gov.pagopa.pu.pagopapayments.dto.generated.ReportingIdDTO;
import it.gov.pagopa.pu.pagopapayments.service.broker.BrokerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class PaymentsReportingService {

  private final NodeForPaClient nodeForPaClient;
  private final BrokerService brokerService;

  public PaymentsReportingService(NodeForPaClient nodeForPaClient, BrokerService brokerService) {
    this.nodeForPaClient = nodeForPaClient;
    this.brokerService = brokerService;
  }

  public List<ReportingIdDTO> getPaymentsReportingList(Long organizationId, String accessToken) {
    BrokerForNodoPaDTO brokerForNodoPaDTO = brokerService.getBrokerForNodoPaDTOByOrganizationId(organizationId, accessToken);
    return nodeForPaClient.getPaymentsReportingList(brokerForNodoPaDTO);
  }
}
