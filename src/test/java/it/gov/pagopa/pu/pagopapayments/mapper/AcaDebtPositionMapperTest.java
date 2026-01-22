package it.gov.pagopa.pu.pagopapayments.mapper;

import it.gov.pagopa.pu.aca.gpd.v1.dto.generated.PaymentPositionModel;
import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
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

@ExtendWith(MockitoExtension.class)
class AcaDebtPositionMapperTest {

  @InjectMocks
  private AcaDebtPositionMapper acaDebtPositionMapper;

  private final PodamFactory podamFactory;

  private DebtPositionDTO debtPosition;
  private Organization organization;

  AcaDebtPositionMapperTest() {
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
        installment.getTransfers().forEach(transfer ->
          transfer.setTransferIndex(1));

        organization = podamFactory.manufacturePojo(Organization.class);
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

    //when
    Pair<Operation, PaymentPositionModel> response = acaDebtPositionMapper.mapToNewPaymentPositionModel(toSync.getIud(), debtPosition, organization);

    //verify
    Assertions.assertNotNull(response);
    PaymentPositionModel paymentPositionModel = response.getRight();
    Assertions.assertNotNull(paymentPositionModel);
    TestUtils.checkNotNullFields(paymentPositionModel, "payStandIn", "streetName", "civicNumber", "postalCode", "city", "province", "country", "region", "email", "phone", "officeName", "validityDate", "paymentDate", "status");

    Assertions.assertEquals(toSync.getIupdPagopa(), paymentPositionModel.getIupd());
    Assertions.assertEquals(toSync.getNav(), paymentPositionModel.getPaymentOption().getFirst().getNav());
    Assertions.assertEquals(Operation.CREATE, response.getLeft());
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
      //when
      Pair<Operation, PaymentPositionModel> response = acaDebtPositionMapper.mapToNewPaymentPositionModel(pair.getLeft().getIud(), debtPosition, organization);

      Assertions.assertNotNull(response);
      PaymentPositionModel paymentPositionModel = response.getRight();
      Assertions.assertNotNull(paymentPositionModel);
      TestUtils.checkNotNullFields(paymentPositionModel, "payStandIn", "streetName", "civicNumber", "postalCode", "city", "province", "country", "region", "email", "phone", "officeName", "validityDate", "paymentDate", "status");

      Assertions.assertEquals(ConversionUtils.atEndOfDay(pair.getLeft().getDueDate()).toString(), paymentPositionModel.getPaymentOption().getFirst().getDueDate());
      Assertions.assertEquals(pair.getLeft().getNav(), paymentPositionModel.getPaymentOption().getFirst().getNav());
      Assertions.assertEquals(pair.getRight(), response.getLeft());
    });
  }

  @Test
  void givenValidDebtPositionWithInvalidStatusWhenMapToPaymentPositionModelThenException() {
    //given
    InstallmentDTO installment = setSyncStatus(debtPosition, 1, 2, InstallmentStatus.PAID, InstallmentStatus.UNPAID);
    //others installments will be ignored

    //when
    String iud = installment.getIud();
    InvalidValueException response = Assertions.assertThrows(InvalidValueException.class, () -> acaDebtPositionMapper.mapToNewPaymentPositionModel(iud, debtPosition, organization));

    //verify
    Assertions.assertNotNull(response);
    Assertions.assertEquals("Invalid sync status [%s->%s] for installment [%s]".formatted(
      installment.getSyncStatus().getSyncStatusFrom(), installment.getSyncStatus().getSyncStatusTo(), installment.getIud()), response.getMessage());
  }

  @Test
  void givenFineWithStatusUnpayableWhenMapToPaymentPositionModelThenOk() {
    //given
    InstallmentDTO toSync = setSyncStatus(debtPosition, 0, 0, InstallmentStatus.UNPAYABLE, InstallmentStatus.UNPAID);

    //when
    Pair<Operation, PaymentPositionModel> response = acaDebtPositionMapper.mapToNewPaymentPositionModel(toSync.getIud(), debtPosition, organization);

    //verify
    Assertions.assertNotNull(response);
    PaymentPositionModel paymentPositionModel = response.getRight();
    Assertions.assertNotNull(paymentPositionModel);
    TestUtils.checkNotNullFields(paymentPositionModel, "payStandIn", "streetName", "civicNumber", "postalCode", "city", "province", "country", "region", "email", "phone", "officeName", "validityDate", "paymentDate", "status");

    Assertions.assertEquals(toSync.getIupdPagopa(), paymentPositionModel.getIupd());
    Assertions.assertEquals(toSync.getNav(), paymentPositionModel.getPaymentOption().getFirst().getNav());
    Assertions.assertEquals(Operation.CREATE, response.getLeft());
  }
}

