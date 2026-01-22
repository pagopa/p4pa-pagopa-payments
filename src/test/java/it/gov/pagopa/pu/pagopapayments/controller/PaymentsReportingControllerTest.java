package it.gov.pagopa.pu.pagopapayments.controller;

import it.gov.pagopa.pu.pagopapayments.dto.generated.PaymentsReportingIdDTO;
import it.gov.pagopa.pu.pagopapayments.service.paymentsreporting.PaymentsReportingRestService;
import it.gov.pagopa.pu.pagopapayments.service.paymentsreporting.PaymentsReportingSoapService;
import it.gov.pagopa.pu.pagopapayments.util.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.OffsetDateTime;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class PaymentsReportingControllerTest {

  @Mock
  private PaymentsReportingSoapService paymentsReportingSoapServiceMock;
  @Mock
  private PaymentsReportingRestService paymentsReportingRestServiceMock;

  @InjectMocks
  private PaymentsReportingController paymentsReportingController;

  @AfterEach
  void clear(){
    SecurityContextHolder.clearContext();
  }

  @Test
  void soapGetReportingList_whenValidRequest_thenReturnReportingList() {
    Long organizationId = 1L;
    List<PaymentsReportingIdDTO> expectedResponse = List.of(new PaymentsReportingIdDTO());

    Mockito.when(paymentsReportingSoapServiceMock.getPaymentsReportingList(organizationId, TestUtils.getFakeAccessToken())).thenReturn(expectedResponse);
    TestUtils.setFakeAccessTokenInContext();

    ResponseEntity<List<PaymentsReportingIdDTO>> response = paymentsReportingController.soapGetPaymentsReportingList(organizationId);

    Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    Assertions.assertEquals(expectedResponse, response.getBody());
  }

  @Test
  void soapUploadOfPaymentReporting_whenValidRequest_thenReturnFileId() {
    Long organizationId = 1L;
    String flowId = "flowId";
    String fileName = "fileName";
    Long expectedIngestionFlowFileId = 2L;

    Mockito.when(paymentsReportingSoapServiceMock.fetchPaymentReporting(organizationId, flowId, fileName, TestUtils.getFakeAccessToken()))
      .thenReturn(expectedIngestionFlowFileId);
    TestUtils.setFakeAccessTokenInContext();

    ResponseEntity<Long> response = paymentsReportingController.soapFetchPaymentReporting(organizationId, flowId, fileName);

    Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    Assertions.assertEquals(expectedIngestionFlowFileId, response.getBody());
  }

  @Test
  void restGetReportingList_whenValidRequest_thenReturnReportingList() {
    Long organizationId = 1L;
    OffsetDateTime latestFlowDate = OffsetDateTime.now();
    List<PaymentsReportingIdDTO> expectedResponse = List.of(new PaymentsReportingIdDTO());

    Mockito.when(paymentsReportingRestServiceMock.getPaymentsReportingList(organizationId, latestFlowDate, TestUtils.getFakeAccessToken())).thenReturn(expectedResponse);
    TestUtils.setFakeAccessTokenInContext();

    ResponseEntity<List<PaymentsReportingIdDTO>> response = paymentsReportingController.restGetPaymentsReportingList(organizationId, latestFlowDate);

    Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    Assertions.assertEquals(expectedResponse, response.getBody());
  }

  @Test
  void restUploadOfPaymentReporting_whenValidRequest_thenReturnFileId() {
    Long organizationId = 1L;
    String flowId = "flowId";
    String fileName = "fileName";
    Long revision = 1L;
    String pspId = "pspId";
    Long expectedIngestionFlowFileId = 2L;

    Mockito.when(paymentsReportingRestServiceMock.fetchPaymentReporting(organizationId, flowId, revision, pspId, fileName, TestUtils.getFakeAccessToken()))
      .thenReturn(expectedIngestionFlowFileId);
    TestUtils.setFakeAccessTokenInContext();

    ResponseEntity<Long> response = paymentsReportingController.restFetchPaymentReporting(organizationId, flowId, fileName, revision, pspId);

    Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    Assertions.assertEquals(expectedIngestionFlowFileId, response.getBody());
  }

}
