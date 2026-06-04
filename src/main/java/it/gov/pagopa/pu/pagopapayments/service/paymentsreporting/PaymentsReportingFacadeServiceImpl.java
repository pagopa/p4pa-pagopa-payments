package it.gov.pagopa.pu.pagopapayments.service.paymentsreporting;

import it.gov.pagopa.pu.pagopapayments.connector.fileshare.FileShareService;
import it.gov.pagopa.pu.pagopapayments.connector.pagopa.paymentsreporting.PaymentsReportingService;
import it.gov.pagopa.pu.pagopapayments.dto.BrokerForNodoPaDTO;
import it.gov.pagopa.pu.pagopapayments.dto.PaPaymentReportingDTO;
import it.gov.pagopa.pu.pagopapayments.dto.generated.PaymentsReportingIdDTO;
import it.gov.pagopa.pu.pagopapayments.exception.InvalidValueException;
import it.gov.pagopa.pu.pagopapayments.mapper.PaymentsReportingMapper;
import it.gov.pagopa.pu.pagopapayments.service.broker.BrokerRetrieverService;
import it.gov.pagopa.pu.pagopapayments.util.ErrorCodeConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static it.gov.pagopa.pu.pagopapayments.mapper.PaymentsReportingMapper.PAYMENTS_REPORTING_FILE_EXTENSION;

@Slf4j
@Service
public class PaymentsReportingFacadeServiceImpl implements PaymentsReportingFacadeService {

  private final PaymentsReportingService paymentsReportingService;
  private final BrokerRetrieverService brokerRetrieverService;
  private final FileShareService fileShareService;
  private final PaymentsReportingMapper paymentsReportingMapper;

  public PaymentsReportingFacadeServiceImpl(
    PaymentsReportingService paymentsReportingService,
    BrokerRetrieverService brokerRetrieverService,
    FileShareService fileShareService, PaymentsReportingMapper paymentsReportingMapper) {
    this.paymentsReportingService = paymentsReportingService;
    this.brokerRetrieverService = brokerRetrieverService;
    this.fileShareService = fileShareService;
    this.paymentsReportingMapper = paymentsReportingMapper;
  }

  @Override
  public List<PaymentsReportingIdDTO> getPaymentsReportingList(Long organizationId, OffsetDateTime latestFlowDate, String accessToken) {
    BrokerForNodoPaDTO brokerForNodoPaDTO = brokerRetrieverService.getBrokerForNodoPaDTOByOrganizationId(organizationId, accessToken);
    return paymentsReportingService.fetchPaymentReportingIdList(brokerForNodoPaDTO, latestFlowDate);
  }

  @Override
  public Long fetchPaymentReporting(Long organizationId, String paymentsReportingId, Long revision, String pspId, String fileName, String accessToken) {
    String nonNullFileName = Optional.ofNullable(fileName)
      .orElse(paymentsReportingId + "_payments_reporting" + PAYMENTS_REPORTING_FILE_EXTENSION); // paymentsReportingFileName in PaymentsReportingIdDTO may be not set by getPaymentsReportingList in PaymentsReportingRestServiceImpl
    if(paymentsReportingMapper.isFilenameInvalid(nonNullFileName, paymentsReportingId)){
      throw new InvalidValueException(ErrorCodeConstants.ERROR_CODE_INVALID_FILE_NAME, "PaymentsReporting file name not valid '" + nonNullFileName + "' to fetch file with id " + paymentsReportingId);
    }
    BrokerForNodoPaDTO brokerForNodoPaDTO = brokerRetrieverService.getBrokerForNodoPaDTOByOrganizationId(organizationId, accessToken);
    PaPaymentReportingDTO paPaymentReportingDTO = paymentsReportingService.fetchPaymentReporting(brokerForNodoPaDTO, paymentsReportingId, revision, pspId);
    return fileShareService.uploadPaymentReporting(paPaymentReportingDTO, organizationId, nonNullFileName, accessToken);
  }

}
