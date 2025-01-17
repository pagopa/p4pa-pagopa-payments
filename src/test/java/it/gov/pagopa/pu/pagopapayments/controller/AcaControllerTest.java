package it.gov.pagopa.pu.pagopapayments.controller;

import it.gov.pagopa.pu.pagopapayments.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.pagopapayments.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.pagopapayments.dto.generated.PaymentOptionDTO;
import it.gov.pagopa.pu.pagopapayments.service.aca.AcaService;
import it.gov.pagopa.pu.pagopapayments.service.synchronouspayments.SynchronousPaymentService;
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
class AcaControllerTest {

  @Mock
  private AcaService acaServiceMock;

  @InjectMocks
  private AcaController acaController;

  private static final DebtPositionDTO VALID_DEBT_POSITION = DebtPositionDTO.builder()
    .debtPositionId(1L)
    .description("descr")
    .debtPositionTypeOrgId(2L)
    .paymentOptions(List.of(PaymentOptionDTO.builder()
      .paymentOptionId(3L)
      .installments(List.of(InstallmentDTO.builder()
        .installmentId(4L)
        .status(SynchronousPaymentService.PaymentStatus.TO_SYNCH.name())
        .iuv("444444")
        .build(), InstallmentDTO.builder()
        .installmentId(5L)
        .status(SynchronousPaymentService.PaymentStatus.UNPAID.name())
        .iuv("555555")
        .build()))
      .build()))
    .build();

  @AfterEach
  void clear(){
    SecurityContextHolder.clearContext();
  }

  @Test
  void givenValidDebtPositionWhenCreateAcaThenOk() {
    //given
    Mockito.doNothing().when(acaServiceMock).create(VALID_DEBT_POSITION, TestUtils.getFakeAccessToken());
    TestUtils.setFakeAccessTokenInContext();
    //when
    ResponseEntity<Void> response = acaController.createAca(VALID_DEBT_POSITION);
    //verify
    Assertions.assertNotNull(response);
    Assertions.assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    Mockito.verify(acaServiceMock, Mockito.times(1)).create(VALID_DEBT_POSITION, TestUtils.getFakeAccessToken());
  }

  @Test
  void givenValidDebtPositionWhenUpdateAcaThenOk() {
    //given
    Mockito.doNothing().when(acaServiceMock).update(VALID_DEBT_POSITION, TestUtils.getFakeAccessToken());
    TestUtils.setFakeAccessTokenInContext();
    //when
    ResponseEntity<Void> response = acaController.updateAca(VALID_DEBT_POSITION);
    //verify
    Assertions.assertNotNull(response);
    Assertions.assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    Mockito.verify(acaServiceMock, Mockito.times(1)).update(VALID_DEBT_POSITION, TestUtils.getFakeAccessToken());
  }

  @Test
  void givenValidDebtPositionWhenDeleteAcaThenOk() {
    //given
    Mockito.doNothing().when(acaServiceMock).delete(VALID_DEBT_POSITION, TestUtils.getFakeAccessToken());
    TestUtils.setFakeAccessTokenInContext();
    //when
    ResponseEntity<Void> response = acaController.deleteAca(VALID_DEBT_POSITION);
    //verify
    Assertions.assertNotNull(response);
    Assertions.assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    Mockito.verify(acaServiceMock, Mockito.times(1)).delete(VALID_DEBT_POSITION, TestUtils.getFakeAccessToken());
  }
}
