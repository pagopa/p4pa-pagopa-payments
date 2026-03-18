package it.gov.pagopa.pu.pagopapayments.controller;

import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import it.gov.pagopa.pu.pagopapayments.service.sync.aca.AcaFacadeService;
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
  private AcaFacadeService acaFacadeServiceMock;

  @InjectMocks
  private AcaController acaController;

  private static final DebtPositionDTO VALID_DEBT_POSITION = DebtPositionDTO.builder()
    .debtPositionId(1L)
    .debtPositionOrigin(DebtPositionOrigin.ORDINARY)
    .organizationId(1L)
    .description("descr")
    .debtPositionTypeOrgId(2L)
    .flagPuPagoPaPayment(true)
    .paymentOptions(List.of(PaymentOptionDTO.builder()
      .paymentOptionId(3L)
      .totalAmountCents(21_00L)
      .paymentOptionType(PaymentOptionType.INSTALLMENTS)
      .installments(List.of(InstallmentDTO.builder()
          .installmentId(4L)
          .iuv("444444")
          .iud("777777")
          .status(InstallmentStatus.TO_SYNC)
          .amountCents(11_00L)
          .remittanceInformation("REMITTANCE4")
          .debtor(TestUtils.getPodamFactory().manufacturePojo(PersonDTO.class))
          .transfers(List.of(TransferDTO.builder()
              .transferId(4L)
              .orgFiscalCode("ORGFC")
              .orgName("ORG")
              .remittanceInformation("REMITTANCE4")
              .category("CATEGORY")
              .transferIndex(1)
              .amountCents(11_00L)
            .build()))
          .build(),
        InstallmentDTO.builder()
          .installmentId(5L)
          .iuv("555555")
          .iud("666666")
          .status(InstallmentStatus.UNPAID)
          .amountCents(10_00L)
          .remittanceInformation("REMITTANCE5")
          .debtor(TestUtils.getPodamFactory().manufacturePojo(PersonDTO.class))
          .transfers(List.of(TransferDTO.builder()
            .transferId(5L)
            .orgFiscalCode("ORGFC")
            .orgName("ORG")
            .remittanceInformation("REMITTANCE5")
            .category("CATEGORY")
            .transferIndex(1)
            .amountCents(10_00L)
            .build()))
          .build()))
      .build()))
    .build();

  @AfterEach
  void clear() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void givenValidDebtPositionWhenSyncAcaThenOk() {
    //given
    Mockito.doNothing().when(acaFacadeServiceMock).sync("IUD", VALID_DEBT_POSITION, TestUtils.getFakeAccessToken());
    TestUtils.setFakeAccessTokenInContext();
    //when
    ResponseEntity<Void> response = acaController.syncAca("IUD", VALID_DEBT_POSITION);
    //verify
    Assertions.assertNotNull(response);
    Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    Mockito.verify(acaFacadeServiceMock, Mockito.times(1)).sync("IUD", VALID_DEBT_POSITION, TestUtils.getFakeAccessToken());
  }
}
