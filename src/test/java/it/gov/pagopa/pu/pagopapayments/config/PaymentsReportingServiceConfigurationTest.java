package it.gov.pagopa.pu.pagopapayments.config;

import it.gov.pagopa.pu.pagopapayments.connector.fileshare.FileShareService;
import it.gov.pagopa.pu.pagopapayments.connector.pagopa.paymentsreporting.NodePaymentsReportingService;
import it.gov.pagopa.pu.pagopapayments.connector.soap.NodeForPaClient;
import it.gov.pagopa.pu.pagopapayments.mapper.PaymentsReportingMapper;
import it.gov.pagopa.pu.pagopapayments.service.broker.BrokerRetrieverService;
import it.gov.pagopa.pu.pagopapayments.service.paymentsreporting.PaymentsReportingLegacySoapServiceImpl;
import it.gov.pagopa.pu.pagopapayments.service.paymentsreporting.PaymentsReportingRestServiceImpl;
import it.gov.pagopa.pu.pagopapayments.service.paymentsreporting.PaymentsReportingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

@ExtendWith(MockitoExtension.class)
class PaymentsReportingServiceConfigurationTest {

  @Mock
  private NodeForPaClient paymentsReportingSoapClient;
  @Mock
  private NodePaymentsReportingService nodePaymentsReportingService;
  @Mock
  private BrokerRetrieverService brokerRetrieverService;
  @Mock
  private FileShareService fileShareService;
  @Mock
  private PaymentsReportingMapper paymentsReportingMapper;

  private PaymentsReportingServiceConfiguration paymentsReportingServiceConfiguration;

  @Test
  public void LegacyTest() {
    String isLeagcyString = Boolean.TRUE.toString();
    paymentsReportingServiceConfiguration =
      new PaymentsReportingServiceConfiguration(isLeagcyString);

    PaymentsReportingService paymentsReportingService = paymentsReportingServiceConfiguration
        .paymentsReportingService(
          paymentsReportingSoapClient,
          nodePaymentsReportingService,
          brokerRetrieverService,
          fileShareService,
          paymentsReportingMapper
        );
    assertInstanceOf(
      PaymentsReportingLegacySoapServiceImpl.class,
      paymentsReportingService
    );
  }

  @Test
  public void NonLegacyTest() {
    String isLegacyString = Boolean.FALSE.toString();
    paymentsReportingServiceConfiguration =
      new PaymentsReportingServiceConfiguration(isLegacyString);

    PaymentsReportingService paymentsReportingService = paymentsReportingServiceConfiguration
      .paymentsReportingService(
        paymentsReportingSoapClient,
        nodePaymentsReportingService,
        brokerRetrieverService,
        fileShareService,
        paymentsReportingMapper
      );
    assertInstanceOf(
      PaymentsReportingRestServiceImpl.class,
      paymentsReportingService
    );
  }

}
