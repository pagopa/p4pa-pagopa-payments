package it.gov.pagopa.pu.pagopapayments.service.paymentsreporting;

import it.gov.pagopa.pu.pagopapayments.connector.fileshare.FileShareService;
import it.gov.pagopa.pu.pagopapayments.connector.pagopa.paymentsreporting.NodePaymentsReportingService;
import it.gov.pagopa.pu.pagopapayments.dto.BrokerForNodoPaDTO;
import it.gov.pagopa.pu.pagopapayments.dto.PaPaymentReportingDTO;
import it.gov.pagopa.pu.pagopapayments.dto.generated.PaymentsReportingIdDTO;
import it.gov.pagopa.pu.pagopapayments.exception.InvalidValueException;
import it.gov.pagopa.pu.pagopapayments.mapper.PaymentsReportingMapper;
import it.gov.pagopa.pu.pagopapayments.service.broker.BrokerRetrieverService;
import lombok.extern.slf4j.Slf4j;

import java.time.OffsetDateTime;
import java.util.List;

import static it.gov.pagopa.pu.pagopapayments.mapper.PaymentsReportingMapper.PAYMENTS_REPORTING_FILE_EXTENSION;

@Slf4j
public class PaymentsReportingRestServiceImpl implements PaymentsReportingService {

  private final NodePaymentsReportingService nodePaymentsReportingService;
  private final BrokerRetrieverService brokerRetrieverService;
  private final FileShareService fileShareService;
  private final PaymentsReportingMapper paymentsReportingMapper;

  public PaymentsReportingRestServiceImpl(
    NodePaymentsReportingService nodePaymentsReportingService,
    BrokerRetrieverService brokerRetrieverService,
    FileShareService fileShareService, PaymentsReportingMapper paymentsReportingMapper) {
    this.nodePaymentsReportingService = nodePaymentsReportingService;
    this.brokerRetrieverService = brokerRetrieverService;
    this.fileShareService = fileShareService;
    this.paymentsReportingMapper = paymentsReportingMapper;
  }

  @Override
  public List<PaymentsReportingIdDTO> getPaymentsReportingList(Long organizationId, OffsetDateTime latestFlowDate, String accessToken) {
    BrokerForNodoPaDTO brokerForNodoPaDTO = brokerRetrieverService.getBrokerForNodoPaDTOByOrganizationId(organizationId, accessToken);
    return nodePaymentsReportingService.fetchPaymentReportingIdList(brokerForNodoPaDTO, latestFlowDate);
  }

  @Override
  public Long fetchPaymentReporting(Long organizationId, String paymentsReportingId, Long revision, String pspId, String fileName, String accessToken) {
    fileName = paymentsReportingId + "_payments_reporting" + PAYMENTS_REPORTING_FILE_EXTENSION; // in PaymentsReportingIdDTO paymentsReportingFileName is not set by getPaymentsReportingList in PaymentsReportingRestServiceImpl
    if(paymentsReportingMapper.isFilenameInvalid(fileName, paymentsReportingId)){
      throw new InvalidValueException("PaymentsReporting file name not valid '" + fileName + "' to fetch file with id " + paymentsReportingId);
    }
    BrokerForNodoPaDTO brokerForNodoPaDTO = brokerRetrieverService.getBrokerForNodoPaDTOByOrganizationId(organizationId, accessToken);
    PaPaymentReportingDTO paPaymentReportingDTO = nodePaymentsReportingService.fetchPaymentReporting(brokerForNodoPaDTO, paymentsReportingId, revision, pspId);
    return fileShareService.uploadPaymentReporting(paPaymentReportingDTO, organizationId, fileName, accessToken);
  }

}
