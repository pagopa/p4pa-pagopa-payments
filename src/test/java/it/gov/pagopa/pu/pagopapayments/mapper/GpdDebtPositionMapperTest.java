package it.gov.pagopa.pu.pagopapayments.mapper;

import it.gov.pagopa.nodo.gpd.dto.generated.InstallmentModel;
import it.gov.pagopa.nodo.gpd.dto.generated.PaymentOptionModelV3;
import it.gov.pagopa.nodo.gpd.dto.generated.PaymentPositionModelV3;
import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import it.gov.pagopa.pu.pagopapayments.enums.Operation;
import it.gov.pagopa.pu.pagopapayments.exception.InvalidValueException;
import it.gov.pagopa.pu.pagopapayments.util.ConversionUtils;
import it.gov.pagopa.pu.pagopapayments.util.TestUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.common.AttributeStrategy;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@ExtendWith(MockitoExtension.class)
class GpdDebtPositionMapperTest {
  @InjectMocks
  private GpdDebtPositionMapper gpdDebtPositionMapper;

  private final PodamFactory podamFactory;

  private DebtPositionDTO debtPosition;
  private static final String ORG_NAME = "orgName";

  GpdDebtPositionMapperTest() {
    podamFactory = TestUtils.getPodamFactory();
    podamFactory.getStrategy().setDefaultNumberOfCollectionElements(3);
    podamFactory.getStrategy().addOrReplaceAttributeStrategy(InstallmentDTO.class, "amountCents",
      (AttributeStrategy<Long>) (attrType, attrAnnotations) -> RandomUtils.insecure().randomLong(1, 1000000));
  }

  @BeforeEach
  void init() {
    debtPosition = podamFactory.manufacturePojo(DebtPositionDTO.class);
    debtPosition.getPaymentOptions().forEach(paymentOption ->
      paymentOption.getInstallments().forEach(installment -> {
        installment.getDebtor().setEntityType(PersonEntityType.F);
        installment.setStatus(InstallmentStatus.UNPAID);
        installment.setSyncStatus(null);
        installment.setDueDate(LocalDate.now().plusDays(10));
        installment.getTransfers().forEach(transfer -> transfer.setTransferIndex(1));
      }));
  }

  private InstallmentDTO setSyncStatus(DebtPositionDTO debtPosition, int indexPaymentOption, int indexInstallment, InstallmentStatus syncStatusFrom, InstallmentStatus syncStatusTo) {
    InstallmentDTO installmentDTO = debtPosition.getPaymentOptions().get(indexPaymentOption).getInstallments().get(indexInstallment);
    installmentDTO.setStatus(InstallmentStatus.TO_SYNC);
    installmentDTO.setSyncStatus(InstallmentSyncStatus.builder()
      .syncStatusFrom(syncStatusFrom)
      .syncStatusTo(syncStatusTo)
      .build());
    installmentDTO.setTransfers(List.of(installmentDTO.getTransfers().getFirst()));
    return installmentDTO;
  }

  @Test
  void givenValidDebtPositionExpiringWhenMapToPaymentPositionModelThenOk() {
    //given
    InstallmentDTO toSync = setSyncStatus(debtPosition, 0, 0, InstallmentStatus.DRAFT, InstallmentStatus.UNPAID);
    TransferDTO transferAmountZero = podamFactory.manufacturePojo(TransferDTO.class);
    transferAmountZero.setAmountCents(0L);
    toSync.getTransfers().add(transferAmountZero);

    //when
    Pair<Operation, PaymentPositionModelV3> response = gpdDebtPositionMapper.mapToNewPaymentPositionModel(toSync.getIud(), debtPosition, ORG_NAME);

    //verify
    Assertions.assertNotNull(response);
    Assertions.assertEquals(Operation.CREATE, response.getLeft());

    PaymentPositionModelV3 paymentPositionModel = response.getRight();
    Assertions.assertNotNull(paymentPositionModel);
    Assertions.assertEquals(toSync.getIupdPagopa(), paymentPositionModel.getIupd());
    Assertions.assertEquals(ORG_NAME, paymentPositionModel.getCompanyName());
    Assertions.assertFalse(paymentPositionModel.getPaymentOption().isEmpty());

    PaymentOptionModelV3 paymentOption = paymentPositionModel.getPaymentOption().getFirst();
    Assertions.assertNotNull(paymentOption.getDebtor());
    Assertions.assertFalse(paymentOption.getInstallments().isEmpty());

    InstallmentModel installment = paymentOption.getInstallments().getFirst();
    Assertions.assertEquals(toSync.getNav(), installment.getNav());
  }

  @Test
  void givenValidDebtPositionWithVariousOpsWhenMapToPaymentPositionModelThenOk() {
    //given
    List<Pair<InstallmentDTO, Operation>> toSyncList = List.of(
      Pair.of(setSyncStatus(debtPosition, 0, 0, InstallmentStatus.DRAFT, InstallmentStatus.UNPAID), Operation.CREATE),
      Pair.of(setSyncStatus(debtPosition, 0, 1, InstallmentStatus.UNPAID, InstallmentStatus.UNPAID), Operation.UPDATE),
      Pair.of(setSyncStatus(debtPosition, 0, 2, InstallmentStatus.EXPIRED, InstallmentStatus.UNPAID), Operation.UPDATE),
      Pair.of(setSyncStatus(debtPosition, 1, 0, InstallmentStatus.UNPAID, InstallmentStatus.CANCELLED), Operation.DELETE),
      Pair.of(setSyncStatus(debtPosition, 1, 1, InstallmentStatus.UNPAID, InstallmentStatus.INVALID), Operation.DELETE),
      Pair.of(setSyncStatus(debtPosition, 2, 0, InstallmentStatus.EXPIRED, InstallmentStatus.CANCELLED), Operation.DELETE),
      Pair.of(setSyncStatus(debtPosition, 2, 1, InstallmentStatus.EXPIRED, InstallmentStatus.INVALID), Operation.DELETE),
      Pair.of(setSyncStatus(debtPosition, 2, 2, InstallmentStatus.UNPAID, InstallmentStatus.EXPIRED), Operation.DELETE)
    );

    toSyncList.forEach(pair -> {
      Pair<Operation, PaymentPositionModelV3> response =
        gpdDebtPositionMapper.mapToNewPaymentPositionModel(
          pair.getLeft().getIud(), debtPosition, ORG_NAME
        );

      Assertions.assertNotNull(response);
      Assertions.assertEquals(pair.getRight(), response.getLeft());

      PaymentPositionModelV3 model = response.getRight();
      Assertions.assertNotNull(model);

      PaymentOptionModelV3 paymentOption = model.getPaymentOption().getFirst();
      InstallmentModel installment = paymentOption.getInstallments().getFirst();

      Assertions.assertEquals(
        Objects.requireNonNull(ConversionUtils.atEndOfDay(pair.getLeft().getDueDate())).toString(),
        installment.getDueDate()
      );
      Assertions.assertEquals(pair.getLeft().getNav(), installment.getNav());
    });
  }

  @Test
  void givenValidDebtPositionWithInvalidStatusWhenMapToPaymentPositionModelThenException() {
    //given
    InstallmentDTO installment = setSyncStatus(debtPosition, 1, 2, InstallmentStatus.PAID, InstallmentStatus.UNPAID);
    //others installments will be ignored

    //when
    String iud = installment.getIud();
    InvalidValueException response = Assertions.assertThrows(InvalidValueException.class, () -> gpdDebtPositionMapper.mapToNewPaymentPositionModel(iud, debtPosition, ORG_NAME));

    //verify
    Assertions.assertNotNull(response);
    Assertions.assertEquals("[INVALID_SYNC_STATUS] Invalid sync status [%s->%s] for installment [%s]".formatted(
      installment.getSyncStatus().getSyncStatusFrom(), installment.getSyncStatus().getSyncStatusTo(), installment.getIud()), response.getMessage());
  }

  @Test
  void givenFineWithStatusUnpayableWhenMapToPaymentPositionModelThenOk() {
    //given
    InstallmentDTO toSync = setSyncStatus(debtPosition, 0, 0, InstallmentStatus.UNPAYABLE, InstallmentStatus.UNPAID);

    //when
    Pair<Operation, PaymentPositionModelV3> response = gpdDebtPositionMapper.mapToNewPaymentPositionModel(toSync.getIud(), debtPosition, ORG_NAME);

    //verify
    Assertions.assertNotNull(response);
    Assertions.assertEquals(Operation.CREATE, response.getLeft());

    PaymentPositionModelV3 model = response.getRight();
    Assertions.assertEquals(toSync.getIupdPagopa(), model.getIupd());

    InstallmentModel installment =
      model.getPaymentOption().getFirst().getInstallments().getFirst();

    Assertions.assertEquals(toSync.getNav(), installment.getNav());
  }

  @Test
  void givenInstallmentToSyncWithNullSyncStatusWhenMapToPaymentPositionModelThenException() {
    //given
    InstallmentDTO installment = debtPosition.getPaymentOptions().get(0).getInstallments().get(0);
    installment.setStatus(InstallmentStatus.TO_SYNC);
    installment.setSyncStatus(null);
    String iud = installment.getIud();

    //when
    InvalidValueException ex = Assertions.assertThrows(
      InvalidValueException.class,
      () -> gpdDebtPositionMapper.mapToNewPaymentPositionModel(iud, debtPosition, ORG_NAME)
    );

    //verify
    Assertions.assertNotNull(ex);
    Assertions.assertEquals("[INVALID_SYNC_STATUS] Sync status is null for installment [%s]".formatted(iud), ex.getMessage()
    );
  }
}
