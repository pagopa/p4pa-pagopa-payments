package it.gov.pagopa.pu.pagopapayments.mapper;

import it.gov.pagopa.nodo.pacreateposition.dto.generated.NewDebtPositionRequest;
import it.gov.pagopa.pu.pagopapayments.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.pagopapayments.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.pagopapayments.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.pagopapayments.dto.generated.InstallmentSyncStatus;
import it.gov.pagopa.pu.pagopapayments.exception.InvalidValueException;
import it.gov.pagopa.pu.pagopapayments.service.aca.AcaService;
import it.gov.pagopa.pu.pagopapayments.util.Constants;
import it.gov.pagopa.pu.pagopapayments.util.TestUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.common.AttributeStrategy;

import java.lang.annotation.Annotation;
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
    podamFactory.getStrategy().addOrReplaceAttributeStrategy(InstallmentDTO.class, "amountCents", new AttributeStrategy<Long>() {
      @Override
      public Long getValue(Class<?> attrType, List<Annotation> attrAnnotations) {
        return RandomUtils.insecure().randomLong(1, 1000000);
      }
    });
  }

  @BeforeEach
  void init(){
    // generate random DebtPositionDTO
    debtPosition = podamFactory.manufacturePojo(DebtPositionDTO.class);
    // fix some field values
    debtPosition.getPaymentOptions().forEach(paymentOption ->
      paymentOption.getInstallments().forEach(installment -> {
        installment.getDebtor().setEntityType(NewDebtPositionRequest.EntityTypeEnum.F.name());
        installment.setStatus(InstallmentStatus.UNPAID);
        installment.setSyncStatus(null);
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

  //region mapToNewDebtPositionRequest

  @Test
  void givenValidDebtPositionExpiringWhenMapToNewDebtPositionRequestThenOk() {
    //given
    // select 2 installments to send to ACA
    List<InstallmentDTO> toSync = List.of(
      setSyncStatus(debtPosition, 0, 0, InstallmentStatus.DRAFT, InstallmentStatus.UNPAID),
      setSyncStatus(debtPosition, 1, 1, InstallmentStatus.DRAFT, InstallmentStatus.UNPAID)
    );
    // fix some field values for the other installments
    // this will be discarded because it has multiple transfers
    debtPosition.getPaymentOptions().get(1).getInstallments().get(0).setStatus(InstallmentStatus.TO_SYNC);
    // this will be discarded because even with a single transfer, it has a status != TO_SYNC
    debtPosition.getPaymentOptions().get(0).getInstallments().get(1).getTransfers().remove(1);

    //when
    List<Triple<AcaService.OPERATION,String,NewDebtPositionRequest>> response = acaDebtPositionMapper.mapToNewDebtPositionRequest(debtPosition);

    //verify
    Assertions.assertNotNull(response);
    Assertions.assertEquals(toSync.size(), response.size());
    for(int idx = 0; idx < toSync.size(); idx++) {
      Assertions.assertEquals(toSync.get(idx).getDueDate(), response.get(idx).getRight().getExpirationDate());
      Assertions.assertEquals(toSync.get(idx).getNav(), response.get(idx).getRight().getNav());
      Assertions.assertEquals(toSync.get(idx).getIud(), response.get(idx).getMiddle());
      Assertions.assertEquals(AcaService.OPERATION.CREATE, response.get(idx).getLeft());
    }
    response.stream().map(Triple::getRight).forEach(TestUtils::checkNotNullFields);
  }

  @Test
  void givenValidDebtPositionNonExpiringWhenMapToNewDebtPositionRequestThenOk() {
    // select 2 installments to send to ACA
    List<InstallmentDTO> toSync = List.of(
      setSyncStatus(debtPosition, 0, 0, InstallmentStatus.DRAFT, InstallmentStatus.UNPAID),
      setSyncStatus(debtPosition, 1, 1, InstallmentStatus.DRAFT, InstallmentStatus.UNPAID)
    );
    toSync.forEach(installment -> installment.setDueDate(null));
    // fix some field values for the other installments
    // this will be discarded because it has multiple transfers
    debtPosition.getPaymentOptions().get(1).getInstallments().get(0).setStatus(InstallmentStatus.TO_SYNC);
    // this will be discarded because even with a single transfer, it has a status != TO_SYNC
    debtPosition.getPaymentOptions().get(0).getInstallments().get(1).getTransfers().remove(1);

    //when
    List<Triple<AcaService.OPERATION,String,NewDebtPositionRequest>> response = acaDebtPositionMapper.mapToNewDebtPositionRequest(debtPosition);

    //verify
    Assertions.assertNotNull(response);
    Assertions.assertEquals(toSync.size(), response.size());
    for(int idx = 0; idx < toSync.size(); idx++) {
      Assertions.assertEquals(Constants.MAX_EXPIRATION_DATE, response.get(idx).getRight().getExpirationDate());
      Assertions.assertEquals(toSync.get(idx).getNav(), response.get(idx).getRight().getNav());
      Assertions.assertEquals(toSync.get(idx).getIud(), response.get(idx).getMiddle());
      Assertions.assertEquals(AcaService.OPERATION.CREATE, response.get(idx).getLeft());
    }
    response.stream().map(Triple::getRight).forEach(TestUtils::checkNotNullFields);
  }

  @Test
  void givenValidDebtPositionWithVariousOpsWhenMapToNewDebtPositionRequestThenOk() {
    //given
    List<Pair<InstallmentDTO, AcaService.OPERATION>> toSyncList = List.of(
      Pair.of(setSyncStatus(debtPosition, 0, 0, InstallmentStatus.DRAFT, InstallmentStatus.UNPAID), AcaService.OPERATION.CREATE),
      Pair.of(setSyncStatus(debtPosition, 0, 1, InstallmentStatus.UNPAID, InstallmentStatus.UNPAID), AcaService.OPERATION.UPDATE),
      Pair.of(setSyncStatus(debtPosition, 0, 2, InstallmentStatus.EXPIRED, InstallmentStatus.UNPAID), AcaService.OPERATION.UPDATE),
      Pair.of(setSyncStatus(debtPosition, 1, 0, InstallmentStatus.UNPAID, InstallmentStatus.CANCELLED), AcaService.OPERATION.DELETE),
      Pair.of(setSyncStatus(debtPosition, 1, 1, InstallmentStatus.UNPAID, InstallmentStatus.INVALID), AcaService.OPERATION.DELETE),
      Pair.of(setSyncStatus(debtPosition, 2, 0, InstallmentStatus.EXPIRED, InstallmentStatus.CANCELLED), AcaService.OPERATION.DELETE),
      Pair.of(setSyncStatus(debtPosition, 2, 1, InstallmentStatus.EXPIRED, InstallmentStatus.INVALID), AcaService.OPERATION.DELETE),
      Pair.of(setSyncStatus(debtPosition, 2, 2, InstallmentStatus.UNPAID, InstallmentStatus.EXPIRED), AcaService.OPERATION.DELETE)
    );
    //others installments will be ignored

    //when
    List<Triple<AcaService.OPERATION,String,NewDebtPositionRequest>> response = acaDebtPositionMapper.mapToNewDebtPositionRequest(debtPosition);

    //verify
    Assertions.assertNotNull(response);
    Assertions.assertEquals(toSyncList.size(), response.size());
    for(int idx = 0; idx < toSyncList.size(); idx++) {
      InstallmentDTO expected = toSyncList.get(idx).getLeft();
      AcaService.OPERATION operation = toSyncList.get(idx).getRight();
      Assertions.assertEquals(expected.getDueDate(), response.get(idx).getRight().getExpirationDate());
      Assertions.assertEquals(expected.getAmountCents(), response.get(idx).getRight().getAmount().longValue());
      Assertions.assertEquals(expected.getNav(), response.get(idx).getRight().getNav());
      Assertions.assertEquals(expected.getIud(), response.get(idx).getMiddle());
      Assertions.assertEquals(operation, response.get(idx).getLeft());
    }
    response.stream().map(Triple::getRight).forEach(TestUtils::checkNotNullFields);
  }

  @Test
  void givenValidDebtPositionWithInvalidStatusWhenMapToNewDebtPositionRequestThenException() {
    //given
    InstallmentDTO installment = setSyncStatus(debtPosition, 1, 2, InstallmentStatus.PAID, InstallmentStatus.UNPAID);
    //others installments will be ignored

    //when
    InvalidValueException response = Assertions.assertThrows(InvalidValueException.class, () -> acaDebtPositionMapper.mapToNewDebtPositionRequest(debtPosition));

    //verify
    Assertions.assertNotNull(response);
    Assertions.assertEquals("Invalid sync status [%s->%s] for installment [%s]".formatted(
      installment.getSyncStatus().getSyncStatusFrom(), installment.getSyncStatus().getSyncStatusTo(), installment.getIud()), response.getMessage());
  }
  //endregion

}
