package it.gov.pagopa.pu.pagopapayments.config;

import it.gov.pagopa.pu.pagopapayments.connector.fileshare.FileShareService;
import it.gov.pagopa.pu.pagopapayments.connector.pagopa.paymentsreporting.NodePaymentsReportingService;
import it.gov.pagopa.pu.pagopapayments.connector.soap.NodeForPaClient;
import it.gov.pagopa.pu.pagopapayments.mapper.PaymentsReportingMapper;
import it.gov.pagopa.pu.pagopapayments.service.broker.BrokerRetrieverService;
import it.gov.pagopa.pu.pagopapayments.service.paymentsreporting.PaymentsReportingLegacySoapServiceImpl;
import it.gov.pagopa.pu.pagopapayments.service.paymentsreporting.PaymentsReportingRestServiceImpl;
import it.gov.pagopa.pu.pagopapayments.service.paymentsreporting.PaymentsReportingService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PaymentsReportingServiceConfiguration {

  private final String legacyFeatureFlag;

  public PaymentsReportingServiceConfiguration(
    @Value("${feature-flags.payments-reporting.legacy}") String legacyFeatureFlag) {
    this.legacyFeatureFlag = legacyFeatureFlag;
  }

  @Bean
  public PaymentsReportingService paymentsReportingService(
    NodeForPaClient paymentsReportingSoapClient,
    NodePaymentsReportingService nodePaymentsReportingService,
    BrokerRetrieverService brokerRetrieverService,
    FileShareService fileShareService,
    PaymentsReportingMapper paymentsReportingMapper) {
    if (Boolean.parseBoolean(legacyFeatureFlag)) {
      return new PaymentsReportingLegacySoapServiceImpl(
        paymentsReportingSoapClient,
        brokerRetrieverService,
        fileShareService,
        paymentsReportingMapper
      );
    }
    return new PaymentsReportingRestServiceImpl(
      nodePaymentsReportingService,
      brokerRetrieverService,
      fileShareService,
      paymentsReportingMapper
    );
  }

}
