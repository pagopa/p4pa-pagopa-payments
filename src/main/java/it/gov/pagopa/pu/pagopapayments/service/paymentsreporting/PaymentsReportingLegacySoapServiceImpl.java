package it.gov.pagopa.pu.pagopapayments.service.paymentsreporting;

import it.gov.pagopa.pu.pagopapayments.connector.fileshare.FileShareService;
import it.gov.pagopa.pu.pagopapayments.connector.soap.NodeForPaClient;
import it.gov.pagopa.pu.pagopapayments.dto.BrokerForNodoPaDTO;
import it.gov.pagopa.pu.pagopapayments.dto.PaPaymentReportingDTO;
import it.gov.pagopa.pu.pagopapayments.dto.generated.PaymentsReportingIdDTO;
import it.gov.pagopa.pu.pagopapayments.exception.InvalidValueException;
import it.gov.pagopa.pu.pagopapayments.mapper.PaymentsReportingMapper;
import it.gov.pagopa.pu.pagopapayments.service.broker.BrokerRetrieverService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@Service
public class PaymentsReportingLegacySoapServiceImpl implements PaymentsReportingService {

  private final NodeForPaClient nodeForPaClient;
  private final BrokerRetrieverService brokerRetrieverService;
  private final FileShareService fileShareService;

  public PaymentsReportingLegacySoapServiceImpl(NodeForPaClient nodeForPaClient, BrokerRetrieverService brokerRetrieverService, FileShareService fileShareService) {
    this.nodeForPaClient = nodeForPaClient;
    this.brokerRetrieverService = brokerRetrieverService;
    this.fileShareService = fileShareService;
  }

  @Override
  public List<PaymentsReportingIdDTO> getPaymentsReportingList(Long organizationId, OffsetDateTime latestFlowDate, String accessToken) {
    BrokerForNodoPaDTO brokerForNodoPaDTO = brokerRetrieverService.getBrokerForNodoPaDTOByOrganizationId(organizationId, accessToken);
    return nodeForPaClient.getPaymentsReportingList(brokerForNodoPaDTO);
  }

  @Override
  public Long fetchPaymentReporting(Long organizationId, String paymentsReportingId, Long revision, String pspId, String fileName, String accessToken) {
    if (PaymentsReportingMapper.isFilenameInvalid(fileName, paymentsReportingId)) {
      throw new InvalidValueException("PaymentsReporting file name not valid " + fileName + " to fetch file " + paymentsReportingId);
    }
    BrokerForNodoPaDTO brokerForNodoPaDTO = brokerRetrieverService.getBrokerForNodoPaDTOByOrganizationId(organizationId, accessToken);
    PaPaymentReportingDTO response = nodeForPaClient.fetchPaymentReporting(brokerForNodoPaDTO, paymentsReportingId);
    return fileShareService.uploadPaymentReporting(response, organizationId, fileName, accessToken);
  }

}
