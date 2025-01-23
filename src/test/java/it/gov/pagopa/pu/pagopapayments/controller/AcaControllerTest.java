package it.gov.pagopa.pu.pagopapayments.controller;

import it.gov.pagopa.pu.pagopapayments.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.pagopapayments.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.pagopapayments.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.pagopapayments.dto.generated.PaymentOptionDTO;
import it.gov.pagopa.pu.pagopapayments.service.aca.AcaService;
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
        .status(InstallmentStatus.TO_SYNC)
        .iuv("444444")
        .iud("777777")
        .build(), InstallmentDTO.builder()
        .installmentId(5L)
        .status(InstallmentStatus.UNPAID)
        .iuv("555555")
        .iud("666666")
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
    Mockito.when(acaServiceMock.create(VALID_DEBT_POSITION, TestUtils.getFakeAccessToken())).thenReturn(List.of("777777"));
    TestUtils.setFakeAccessTokenInContext();
    //when
    ResponseEntity<List<String>> response = acaController.createAca(VALID_DEBT_POSITION);
    //verify
    Assertions.assertNotNull(response);
    Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    Assertions.assertNotNull(response.getBody());
    Assertions.assertIterableEquals(List.of("777777"),response.getBody());
    Mockito.verify(acaServiceMock, Mockito.times(1)).create(VALID_DEBT_POSITION, TestUtils.getFakeAccessToken());
  }

  @Test
  void givenValidDebtPositionWhenUpdateAcaThenOk() {
    //given
    Mockito.when(acaServiceMock.update(VALID_DEBT_POSITION, TestUtils.getFakeAccessToken())).thenReturn(List.of("777777"));
    TestUtils.setFakeAccessTokenInContext();
    //when
    ResponseEntity<List<String>> response = acaController.updateAca(VALID_DEBT_POSITION);
    //verify
    Assertions.assertNotNull(response);
    Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    Assertions.assertNotNull(response.getBody());
    Assertions.assertIterableEquals(List.of("777777"),response.getBody());
    Mockito.verify(acaServiceMock, Mockito.times(1)).update(VALID_DEBT_POSITION, TestUtils.getFakeAccessToken());
  }

  @Test
  void givenValidDebtPositionWhenDeleteAcaThenOk() {
    //given
    Mockito.when(acaServiceMock.delete(VALID_DEBT_POSITION, TestUtils.getFakeAccessToken())).thenReturn(List.of("777777"));
    TestUtils.setFakeAccessTokenInContext();
    //when
    ResponseEntity<List<String>> response = acaController.deleteAca(VALID_DEBT_POSITION);
    //verify
    Assertions.assertNotNull(response);
    Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    Assertions.assertNotNull(response.getBody());
    Assertions.assertIterableEquals(List.of("777777"),response.getBody());
    Mockito.verify(acaServiceMock, Mockito.times(1)).delete(VALID_DEBT_POSITION, TestUtils.getFakeAccessToken());
  }
}
