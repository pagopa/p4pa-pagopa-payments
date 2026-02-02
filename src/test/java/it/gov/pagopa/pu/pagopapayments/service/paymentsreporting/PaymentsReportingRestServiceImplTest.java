package it.gov.pagopa.pu.pagopapayments.service.paymentsreporting;

import it.gov.pagopa.pu.pagopapayments.connector.fileshare.FileShareService;
import it.gov.pagopa.pu.pagopapayments.connector.pagopa.paymentsreporting.PaymentsReportingService;
import it.gov.pagopa.pu.pagopapayments.dto.BrokerForNodoPaDTO;
import it.gov.pagopa.pu.pagopapayments.dto.PaPaymentReportingDTO;
import it.gov.pagopa.pu.pagopapayments.dto.generated.PaymentsReportingIdDTO;
import it.gov.pagopa.pu.pagopapayments.exception.InvalidValueException;
import it.gov.pagopa.pu.pagopapayments.mapper.PaymentsReportingMapper;
import it.gov.pagopa.pu.pagopapayments.service.broker.BrokerRetrieverService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class PaymentsReportingRestServiceImplTest {

  @Mock
  private PaymentsReportingService paymentsReportingService;
  @Mock
  private BrokerRetrieverService brokerRetrieverService;
  @Mock
  private FileShareService fileShareService;
  @Mock
  private PaymentsReportingMapper paymentsReportingMapper;

  @InjectMocks
  private PaymentsReportingRestServiceImpl paymentsReportingRestService;

  @AfterEach
  void tearDown() {
    Mockito.verifyNoMoreInteractions(
      paymentsReportingService,
      brokerRetrieverService,
      fileShareService,
      paymentsReportingMapper
    );
  }

  @Test
  void testGetPaymentsReportingList() {
    //given
    Long organizationId = 1L;
    String pspId = "pspId";
    String accessToken = "accessToken";
    String paymentsReportingId = "paymentReportingId";
    String paymentsReportingFileName = "paymentReportingId_payments_reporting.xml";
    OffsetDateTime latestFlowDate = OffsetDateTime.now();

    BrokerForNodoPaDTO brokerForNodoPaDTO = new BrokerForNodoPaDTO();
    List<PaymentsReportingIdDTO> expectedResult = List.of(
      PaymentsReportingIdDTO.builder()
        .pagopaPaymentsReportingId(paymentsReportingId)
        .paymentsReportingFileName(paymentsReportingFileName)
        .revision(1)
        .pspId(pspId)
        .flowDateTime(latestFlowDate)
        .build()
    );

    Mockito.when(
      brokerRetrieverService.getBrokerForNodoPaDTOByOrganizationId(
        organizationId,
        accessToken
      )
    ).thenReturn(brokerForNodoPaDTO);

    Mockito.when(
      paymentsReportingService.fetchPaymentReportingIdList(
        brokerForNodoPaDTO,
        latestFlowDate
      )
    ).thenReturn(expectedResult);

    //when
    List<PaymentsReportingIdDTO> actualResult = paymentsReportingRestService.getPaymentsReportingList(
      organizationId,
      latestFlowDate,
      accessToken
    );
    //then
    Assertions.assertEquals(expectedResult, actualResult);
  }

  @Test
  void givenValidFilenameWhenFetchPaymentReportingReturnNumOfBytesUploaded() {
    //given
    Long organizationId = 1L;
    String paymentsReportingId = "paymentReportingId";
    Long revision = 1L;
    String paymentsReportingFileName = "paymentReportingId_payments_reporting.xml";
    String pspId = "pspId";
    String accessToken = "accessToken";

    BrokerForNodoPaDTO brokerForNodoPaDTO = new BrokerForNodoPaDTO();
    PaPaymentReportingDTO paPaymentReportingDTO = new PaPaymentReportingDTO();
    Long expectedFileBytesNum = 123L;

    Mockito.when(
      paymentsReportingMapper.isFilenameInvalid(
        paymentsReportingFileName,
        paymentsReportingId
      )
    ).thenReturn(false);

    Mockito.when(
      brokerRetrieverService.getBrokerForNodoPaDTOByOrganizationId(
        organizationId,
        accessToken
      )
    ).thenReturn(brokerForNodoPaDTO);

    Mockito.when(
      paymentsReportingService.fetchPaymentReporting(
        brokerForNodoPaDTO, paymentsReportingId, revision, pspId
      )
    ).thenReturn(paPaymentReportingDTO);

    Mockito.when(fileShareService.uploadPaymentReporting(
      paPaymentReportingDTO, organizationId, paymentsReportingFileName, accessToken
    )).thenReturn(expectedFileBytesNum);

    //when
    Long actualFileBytesNum = paymentsReportingRestService.fetchPaymentReporting(
      organizationId, paymentsReportingId, revision, pspId, paymentsReportingFileName,
      accessToken
    );
    //then
    Assertions.assertEquals(expectedFileBytesNum, actualFileBytesNum);
  }

  @Test
  void givenInvalidFilenameWhenFetchPaymentReportingThrowInvalidValueException() {
    //given
    Long organizationId = 1L;
    String paymentsReportingId = "paymentReportingId";
    Long revision = 1L;
    String paymentsReportingFileName = "paymentReportingId_payments_reporting.xml";
    String pspId = "pspId";
    String accessToken = "accessToken";

    String expectedExceptionMessage = "[INVALID_FILE_NAME] PaymentsReporting file name not valid '%s' to fetch file with id %s"
      .formatted(paymentsReportingFileName, paymentsReportingId);

    Mockito.when(
      paymentsReportingMapper.isFilenameInvalid(
        paymentsReportingFileName,
        paymentsReportingId
      )
    ).thenReturn(true);

    //when
    InvalidValueException invalidValueException = assertThrows(
      InvalidValueException.class,
      () -> paymentsReportingRestService.fetchPaymentReporting(
        organizationId, paymentsReportingId, revision, pspId, paymentsReportingFileName,
        accessToken
      )
    );

    //then
    Assertions.assertEquals(expectedExceptionMessage, invalidValueException.getMessage());
  }
}
