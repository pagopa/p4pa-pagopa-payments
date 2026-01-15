package it.gov.pagopa.pu.pagopapayments.controller;

import it.gov.pagopa.pu.pagopapayments.dto.generated.PaymentsReportingIdDTO;
import it.gov.pagopa.pu.pagopapayments.service.paymentsreporting.PaymentsReportingService;
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
  private PaymentsReportingService paymentsReportingServiceMock;

  @InjectMocks
  private PaymentsReportingController paymentsReportingController;


  @AfterEach
  void clear(){
    SecurityContextHolder.clearContext();
  }

  @Test
  void getReportingList_whenValidRequest_thenReturnReportingList() {
    Long organizationId = 1L;
    OffsetDateTime latestFlowDate = OffsetDateTime.now();
    List<PaymentsReportingIdDTO> expectedResponse = List.of(new PaymentsReportingIdDTO());

    Mockito.when(paymentsReportingServiceMock.getPaymentsReportingList(organizationId, latestFlowDate, TestUtils.getFakeAccessToken())).thenReturn(expectedResponse);
    TestUtils.setFakeAccessTokenInContext();

    ResponseEntity<List<PaymentsReportingIdDTO>> response = paymentsReportingController.getPaymentsReportingList(organizationId, latestFlowDate);

    Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    Assertions.assertEquals(expectedResponse, response.getBody());
  }

  @Test
  void uploadOfPaymentReporting_whenValidRequest_thenReturnFileId() {
    Long organizationId = 1L;
    String flowId = "flowId";
    String fileName = "fileName";
    Long revision = 1L;
    String pspId = "pspId";
    Long expectedIngestionFlowFileId = 2L;

    Mockito.when(paymentsReportingServiceMock.fetchPaymentReporting(organizationId, flowId, revision, pspId, fileName, TestUtils.getFakeAccessToken()))
      .thenReturn(expectedIngestionFlowFileId);
    TestUtils.setFakeAccessTokenInContext();

    ResponseEntity<Long> response = paymentsReportingController.fetchPaymentReporting(organizationId, flowId, fileName, revision, pspId);

    Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    Assertions.assertEquals(expectedIngestionFlowFileId, response.getBody());
  }

}
