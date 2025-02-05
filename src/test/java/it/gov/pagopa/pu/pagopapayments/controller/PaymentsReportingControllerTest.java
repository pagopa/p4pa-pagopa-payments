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
    List<PaymentsReportingIdDTO> expectedResponse = List.of(new PaymentsReportingIdDTO());

    Mockito.when(paymentsReportingServiceMock.getPaymentsReportingList(organizationId, TestUtils.getFakeAccessToken())).thenReturn(expectedResponse);
    TestUtils.setFakeAccessTokenInContext();

    ResponseEntity<List<PaymentsReportingIdDTO>> response = paymentsReportingController.getPaymentsReportingList(organizationId);

    Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    Assertions.assertEquals(expectedResponse, response.getBody());
  }

  @Test
  void uploadOfPaymentReporting_whenValidRequest_thenReturnFileId() {
    Long organizationId = 1L;
    String flowId = "flowId";
    String expectedResponse = "fileId";

    Mockito.when(paymentsReportingServiceMock.fetchPaymentReporting(organizationId, flowId, TestUtils.getFakeAccessToken())).thenReturn(expectedResponse);
    TestUtils.setFakeAccessTokenInContext();

    ResponseEntity<String> response = paymentsReportingController.fetchPaymentReporting(organizationId, flowId);

    Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    Assertions.assertEquals(expectedResponse, response.getBody());
  }

}
