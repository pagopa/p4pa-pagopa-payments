package it.gov.pagopa.pu.pagopapayments.controller;

import it.gov.pagopa.pu.pagopapayments.dto.generated.ReportingIdDTO;
import it.gov.pagopa.pu.pagopapayments.service.paymentsReporting.PaymentsReportingService;
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
  PaymentsReportingService paymentsReportingServiceMock;

  @InjectMocks
  PaymentsReportingController paymentsReportingController;


  @AfterEach
  void clear(){
    SecurityContextHolder.clearContext();
  }

  @Test
  void getReportingList_whenValidRequest_thenReturnReportingList() {
    Long organizationId = 1L;
    List<ReportingIdDTO> expectedResponse = List.of(new ReportingIdDTO());

    Mockito.when(paymentsReportingServiceMock.getReportingList(organizationId, TestUtils.getFakeAccessToken())).thenReturn(expectedResponse);
    TestUtils.setFakeAccessTokenInContext();

    ResponseEntity<List<ReportingIdDTO>> response = paymentsReportingController.getPaymentsReportingList(organizationId);

    Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    Assertions.assertEquals(expectedResponse, response.getBody());
  }

}
