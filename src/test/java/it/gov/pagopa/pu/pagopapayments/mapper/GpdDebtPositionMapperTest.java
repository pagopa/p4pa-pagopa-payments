package it.gov.pagopa.pu.pagopapayments.mapper;

import it.gov.pagopa.nodo.gpd.dto.generated.PaymentPositionModel;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentSyncStatus;
import it.gov.pagopa.pu.debtpositions.dto.generated.PersonDTO;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
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
class GpdDebtPositionMapperTest {
  @InjectMocks
  private GpdDebtPositionMapper gpdDebtPositionMapper;

  private final PodamFactory podamFactory;

  private DebtPositionDTO debtPosition;
  private Organization organization;

  GpdDebtPositionMapperTest() {
    podamFactory = TestUtils.getPodamFactory();
    podamFactory.getStrategy().setDefaultNumberOfCollectionElements(3);
    podamFactory.getStrategy().addOrReplaceAttributeStrategy(InstallmentDTO.class, "amountCents",
      (AttributeStrategy<Long>) (attrType, attrAnnotations) -> RandomUtils.insecure().randomLong(1, 1000000));
  }

  @BeforeEach
  void init() {
    // generate random DebtPositionDTO
    debtPosition = podamFactory.manufacturePojo(DebtPositionDTO.class);
    // fix some field values
    debtPosition.getPaymentOptions().forEach(paymentOption ->
      paymentOption.getInstallments().forEach(installment -> {
        installment.getDebtor().setEntityType(PersonDTO.EntityTypeEnum.F);
        installment.setStatus(InstallmentDTO.StatusEnum.UNPAID);
        installment.setSyncStatus(null);
        installment.setDueDate(LocalDate.now().plusDays(10));
        installment.getTransfers().forEach(transfer ->
          transfer.setTransferIndex(1));
      }));

    organization = podamFactory.manufacturePojo(Organization.class);
  }

  private InstallmentDTO setSyncStatus(DebtPositionDTO debtPosition, int indexPaymentOption, int indexInstallment, InstallmentDTO.StatusEnum syncStatusFrom, InstallmentDTO.StatusEnum syncStatusTo) {
    InstallmentDTO installmentDTO = debtPosition.getPaymentOptions().get(indexPaymentOption).getInstallments().get(indexInstallment);
    installmentDTO.setStatus(InstallmentDTO.StatusEnum.TO_SYNC);
    installmentDTO.setSyncStatus(InstallmentSyncStatus.builder()
      .syncStatusFrom(InstallmentSyncStatus.SyncStatusFromEnum.valueOf(syncStatusFrom.name()))
      .syncStatusTo(InstallmentSyncStatus.SyncStatusToEnum.valueOf(syncStatusTo.name()))
      .build());
    installmentDTO.setTransfers(List.of(installmentDTO.getTransfers().getFirst()));
    return installmentDTO;
  }


  @Test
  void givenValidDebtPositionExpiringWhenMapToPaymentPositionModelThenOk() {
    //given
    InstallmentDTO toSync = setSyncStatus(debtPosition, 0, 0, InstallmentDTO.StatusEnum.DRAFT, InstallmentDTO.StatusEnum.UNPAID);

    //when
    Pair<GpdDebtPositionMapper.OPERATION, PaymentPositionModel> response = gpdDebtPositionMapper.mapToNewPaymentPositionModel(toSync.getIud(), debtPosition, organization);

    //verify
    Assertions.assertNotNull(response);
    PaymentPositionModel paymentPositionModel = response.getRight();
    Assertions.assertNotNull(paymentPositionModel);
    TestUtils.checkNotNullFields(paymentPositionModel, "payStandIn","streetName","civicNumber","postalCode","city","province","country","region","email","phone","officeName","validityDate","paymentDate","status");

    Assertions.assertEquals(toSync.getIupdPagopa(), paymentPositionModel.getIupd());
    Assertions.assertEquals(toSync.getNav(), paymentPositionModel.getPaymentOption().getFirst().getNav());
    Assertions.assertEquals(GpdDebtPositionMapper.OPERATION.CREATE, response.getLeft());
  }

  @Test
  void givenValidDebtPositionWithVariousOpsWhenMapToPaymentPositionModelThenOk() {
    //given
    List<Pair<InstallmentDTO, GpdDebtPositionMapper.OPERATION>> toSyncList = List.of(
      Pair.of(setSyncStatus(debtPosition, 0, 0, InstallmentDTO.StatusEnum.DRAFT, InstallmentDTO.StatusEnum.UNPAID), GpdDebtPositionMapper.OPERATION.CREATE),
      Pair.of(setSyncStatus(debtPosition, 0, 1, InstallmentDTO.StatusEnum.UNPAID, InstallmentDTO.StatusEnum.UNPAID), GpdDebtPositionMapper.OPERATION.UPDATE),
      Pair.of(setSyncStatus(debtPosition, 0, 2, InstallmentDTO.StatusEnum.EXPIRED, InstallmentDTO.StatusEnum.UNPAID), GpdDebtPositionMapper.OPERATION.UPDATE),
      Pair.of(setSyncStatus(debtPosition, 1, 0, InstallmentDTO.StatusEnum.UNPAID, InstallmentDTO.StatusEnum.CANCELLED), GpdDebtPositionMapper.OPERATION.DELETE),
      Pair.of(setSyncStatus(debtPosition, 1, 1, InstallmentDTO.StatusEnum.UNPAID, InstallmentDTO.StatusEnum.INVALID), GpdDebtPositionMapper.OPERATION.DELETE),
      Pair.of(setSyncStatus(debtPosition, 2, 0, InstallmentDTO.StatusEnum.EXPIRED, InstallmentDTO.StatusEnum.CANCELLED), GpdDebtPositionMapper.OPERATION.DELETE),
      Pair.of(setSyncStatus(debtPosition, 2, 1, InstallmentDTO.StatusEnum.EXPIRED, InstallmentDTO.StatusEnum.INVALID), GpdDebtPositionMapper.OPERATION.DELETE),
      Pair.of(setSyncStatus(debtPosition, 2, 2, InstallmentDTO.StatusEnum.UNPAID, InstallmentDTO.StatusEnum.EXPIRED), GpdDebtPositionMapper.OPERATION.DELETE)
    );

    toSyncList.forEach(pair -> {
      //when
      Pair<GpdDebtPositionMapper.OPERATION, PaymentPositionModel> response = gpdDebtPositionMapper.mapToNewPaymentPositionModel(pair.getLeft().getIud(), debtPosition, organization);

      Assertions.assertNotNull(response);
      PaymentPositionModel paymentPositionModel = response.getRight();
      Assertions.assertNotNull(paymentPositionModel);
      TestUtils.checkNotNullFields(paymentPositionModel, "payStandIn","streetName","civicNumber","postalCode","city","province","country","region","email","phone","officeName","validityDate","paymentDate","status");

      Assertions.assertEquals(ConversionUtils.atEndOfDay(pair.getLeft().getDueDate()), paymentPositionModel.getPaymentOption().getFirst().getDueDate());
      Assertions.assertEquals(pair.getLeft().getNav(), paymentPositionModel.getPaymentOption().getFirst().getNav());
      Assertions.assertEquals(pair.getRight(), response.getLeft());
    });
  }

  @Test
  void givenValidDebtPositionWithInvalidStatusWhenMapToPaymentPositionModelThenException() {
    //given
    InstallmentDTO installment = setSyncStatus(debtPosition, 1, 2, InstallmentDTO.StatusEnum.PAID, InstallmentDTO.StatusEnum.UNPAID);
    //others installments will be ignored

    //when
    String iud = installment.getIud();
    InvalidValueException response = Assertions.assertThrows(InvalidValueException.class, () -> gpdDebtPositionMapper.mapToNewPaymentPositionModel(iud, debtPosition, organization));

    //verify
    Assertions.assertNotNull(response);
    Assertions.assertEquals("Invalid sync status [%s->%s] for installment [%s]".formatted(
      installment.getSyncStatus().getSyncStatusFrom(), installment.getSyncStatus().getSyncStatusTo(), installment.getIud()), response.getMessage());
  }

}
