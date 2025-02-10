package it.gov.pagopa.pu.pagopapayments.mapper;

import it.gov.pagopa.nodo.pacreateposition.dto.generated.NewDebtPositionRequest;
import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import it.gov.pagopa.pu.pagopapayments.exception.InvalidValueException;
import it.gov.pagopa.pu.pagopapayments.util.Constants;
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

import java.util.List;

@ExtendWith(MockitoExtension.class)
class AcaDebtPositionMapperTest {
  @InjectMocks
  private AcaDebtPositionMapper acaDebtPositionMapper;

  private final PodamFactory podamFactory;

  private DebtPositionDTO debtPosition;

  AcaDebtPositionMapperTest() {
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
      }));
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

  //region mapToNewDebtPositionRequest

  @Test
  void givenValidDebtPositionExpiringWhenMapToNewDebtPositionRequestThenOk() {
    //given
    InstallmentDTO toSync = setSyncStatus(debtPosition, 0, 0, InstallmentDTO.StatusEnum.DRAFT, InstallmentDTO.StatusEnum.UNPAID);

    //when
    Pair<AcaDebtPositionMapper.OPERATION, NewDebtPositionRequest> response = acaDebtPositionMapper.mapToNewDebtPositionRequest(toSync.getIud(), debtPosition);

    //verify
    Assertions.assertNotNull(response);
    NewDebtPositionRequest newDebtPositionRequest = response.getRight();
    Assertions.assertNotNull(newDebtPositionRequest);
    TestUtils.checkNotNullFields(newDebtPositionRequest);

    Assertions.assertEquals(toSync.getDueDate(), newDebtPositionRequest.getExpirationDate());
    Assertions.assertEquals(toSync.getNav(), newDebtPositionRequest.getNav());
    Assertions.assertEquals(AcaDebtPositionMapper.OPERATION.CREATE, response.getLeft());
  }

  @Test
  void givenValidDebtPositionNonExpiringWhenMapToNewDebtPositionRequestThenOk() {
    //given
    InstallmentDTO toSync = setSyncStatus(debtPosition, 1, 1, InstallmentDTO.StatusEnum.DRAFT, InstallmentDTO.StatusEnum.UNPAID);
    toSync.setDueDate(null);

    //when
    Pair<AcaDebtPositionMapper.OPERATION, NewDebtPositionRequest> response = acaDebtPositionMapper.mapToNewDebtPositionRequest(toSync.getIud(), debtPosition);

    //verify
    Assertions.assertNotNull(response);
    NewDebtPositionRequest newDebtPositionRequest = response.getRight();
    Assertions.assertNotNull(newDebtPositionRequest);
    TestUtils.checkNotNullFields(newDebtPositionRequest);

    Assertions.assertEquals(Constants.MAX_EXPIRATION_DATE, newDebtPositionRequest.getExpirationDate());
    Assertions.assertEquals(toSync.getNav(), newDebtPositionRequest.getNav());
    Assertions.assertEquals(AcaDebtPositionMapper.OPERATION.CREATE, response.getLeft());
  }

  @Test
  void givenValidDebtPositionWithVariousOpsWhenMapToNewDebtPositionRequestThenOk() {
    //given
    List<Pair<InstallmentDTO, AcaDebtPositionMapper.OPERATION>> toSyncList = List.of(
      Pair.of(setSyncStatus(debtPosition, 0, 0, InstallmentDTO.StatusEnum.DRAFT, InstallmentDTO.StatusEnum.UNPAID), AcaDebtPositionMapper.OPERATION.CREATE),
      Pair.of(setSyncStatus(debtPosition, 0, 1, InstallmentDTO.StatusEnum.UNPAID, InstallmentDTO.StatusEnum.UNPAID), AcaDebtPositionMapper.OPERATION.UPDATE),
      Pair.of(setSyncStatus(debtPosition, 0, 2, InstallmentDTO.StatusEnum.EXPIRED, InstallmentDTO.StatusEnum.UNPAID), AcaDebtPositionMapper.OPERATION.UPDATE),
      Pair.of(setSyncStatus(debtPosition, 1, 0, InstallmentDTO.StatusEnum.UNPAID, InstallmentDTO.StatusEnum.CANCELLED), AcaDebtPositionMapper.OPERATION.DELETE),
      Pair.of(setSyncStatus(debtPosition, 1, 1, InstallmentDTO.StatusEnum.UNPAID, InstallmentDTO.StatusEnum.INVALID), AcaDebtPositionMapper.OPERATION.DELETE),
      Pair.of(setSyncStatus(debtPosition, 2, 0, InstallmentDTO.StatusEnum.EXPIRED, InstallmentDTO.StatusEnum.CANCELLED), AcaDebtPositionMapper.OPERATION.DELETE),
      Pair.of(setSyncStatus(debtPosition, 2, 1, InstallmentDTO.StatusEnum.EXPIRED, InstallmentDTO.StatusEnum.INVALID), AcaDebtPositionMapper.OPERATION.DELETE),
      Pair.of(setSyncStatus(debtPosition, 2, 2, InstallmentDTO.StatusEnum.UNPAID, InstallmentDTO.StatusEnum.EXPIRED), AcaDebtPositionMapper.OPERATION.DELETE)
    );
    //others installments will be ignored

    toSyncList.forEach(pair -> {
      //when
      Pair<AcaDebtPositionMapper.OPERATION, NewDebtPositionRequest> response = acaDebtPositionMapper.mapToNewDebtPositionRequest(pair.getLeft().getIud(), debtPosition);

      Assertions.assertNotNull(response);
      NewDebtPositionRequest newDebtPositionRequest = response.getRight();
      Assertions.assertNotNull(newDebtPositionRequest);
      TestUtils.checkNotNullFields(newDebtPositionRequest);

      Assertions.assertEquals(pair.getLeft().getDueDate(), newDebtPositionRequest.getExpirationDate());
      Assertions.assertEquals(pair.getLeft().getNav(), newDebtPositionRequest.getNav());
      Assertions.assertEquals(pair.getRight(), response.getLeft());
    });
  }

  @Test
  void givenValidDebtPositionWithInvalidStatusWhenMapToNewDebtPositionRequestThenException() {
    //given
    InstallmentDTO installment = setSyncStatus(debtPosition, 1, 2, InstallmentDTO.StatusEnum.PAID, InstallmentDTO.StatusEnum.UNPAID);
    //others installments will be ignored

    //when
    String iud = installment.getIud();
    InvalidValueException response = Assertions.assertThrows(InvalidValueException.class, () -> acaDebtPositionMapper.mapToNewDebtPositionRequest(iud, debtPosition));

    //verify
    Assertions.assertNotNull(response);
    Assertions.assertEquals("Invalid sync status [%s->%s] for installment [%s]".formatted(
      installment.getSyncStatus().getSyncStatusFrom(), installment.getSyncStatus().getSyncStatusTo(), installment.getIud()), response.getMessage());
  }

  @Test
  void givenValidDebtPositionWithMultipleTransferWhenMapToNewDebtPositionRequestThenException() {
    //given
    InstallmentDTO installment = setSyncStatus(debtPosition, 1, 2, InstallmentDTO.StatusEnum.PAID, InstallmentDTO.StatusEnum.UNPAID);
    installment.setTransfers(podamFactory.manufacturePojo(List.class, TransferDTO.class));
    //others installments will be ignored

    //when
    String iud = installment.getIud();
    InvalidValueException response = Assertions.assertThrows(InvalidValueException.class, () -> acaDebtPositionMapper.mapToNewDebtPositionRequest(iud, debtPosition));

    //verify
    Assertions.assertNotNull(response);
    Assertions.assertEquals("Installment with IUD[%s] on debtPosition[%s] not found or with invalid sync state".formatted(
      installment.getIud(), debtPosition.getDebtPositionId()), response.getMessage());
  }
  //endregion

}
