package it.gov.pagopa.pu.pagopapayments.mapper;

import it.gov.pagopa.nodo.pacreateposition.dto.generated.NewDebtPositionRequest;
import it.gov.pagopa.pu.pagopapayments.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.pagopapayments.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.pagopapayments.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.pagopapayments.util.Constants;
import it.gov.pagopa.pu.pagopapayments.util.TestUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.jemos.podam.api.PodamFactory;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class AcaDebtPositionMapperTest {
  @InjectMocks
  private AcaDebtPositionMapper acaDebtPositionMapper;

  private final PodamFactory podamFactory;

  AcaDebtPositionMapperTest() {
    podamFactory = TestUtils.getPodamFactory();
    //list object are created with 2 elements
    podamFactory.getStrategy().setDefaultNumberOfCollectionElements(2);
  }

  //region mapToNewDebtPositionRequest

  @Test
  void givenValidDebtPositionExpiringWhenMapToNewDebtPositionRequestThenOk() {
    //given

    // generate random DebtPositionDTO with 2 payment options, each with 2 installments, each with 2 transfer
    DebtPositionDTO debtPosition = podamFactory.manufacturePojo(DebtPositionDTO.class);
    // fix some field values
    debtPosition.getPaymentOptions().forEach(paymentOption ->
      paymentOption.getInstallments().forEach(installment -> {
        installment.getDebtor().setEntityType(NewDebtPositionRequest.EntityTypeEnum.F.name());
        installment.setStatus(InstallmentStatus.UNPAID);
      }));
    // select 2 installments to send to ACA
    List<InstallmentDTO> installmentToMap = List.of(
      debtPosition.getPaymentOptions().get(0).getInstallments().get(0),
      debtPosition.getPaymentOptions().get(1).getInstallments().get(1)
    );
    // fix some field values for the selected installments
    installmentToMap.forEach(installment -> {
      installment.setStatus(InstallmentStatus.TO_SYNC);
      installment.getTransfers().remove(1);
    });
    // fix some field values for the other installments
    debtPosition.getPaymentOptions().get(1).getInstallments().get(0).setStatus(InstallmentStatus.TO_SYNC);


    //when
    List<Pair<String,NewDebtPositionRequest>> response = acaDebtPositionMapper.mapToNewDebtPositionRequest(debtPosition);
    //verify
    Assertions.assertNotNull(response);
    Assertions.assertEquals(installmentToMap.size(), response.size());
    for(int idx = 0; idx < installmentToMap.size(); idx++) {
      Assertions.assertEquals(installmentToMap.get(idx).getDueDate(), response.get(idx).getRight().getExpirationDate());
    }
    response.forEach(TestUtils::checkNotNullFields);
  }

  @Test
  void givenValidDebtPositionNonExpiringWhenMapToNewDebtPositionRequestThenOk() {
    //given

    // generate random DebtPositionDTO with 2 payment options, each with 2 installments, each with 2 transfer
    DebtPositionDTO debtPosition = podamFactory.manufacturePojo(DebtPositionDTO.class);
    // fix some field values
    debtPosition.getPaymentOptions().forEach(paymentOption ->
      paymentOption.getInstallments().forEach(installment -> {
        installment.getDebtor().setEntityType(NewDebtPositionRequest.EntityTypeEnum.F.name());
        installment.setStatus(InstallmentStatus.UNPAID);
      }));
    // select 2 installments to send to ACA
    List<InstallmentDTO> installmentToMap = List.of(
      debtPosition.getPaymentOptions().get(0).getInstallments().get(0),
      debtPosition.getPaymentOptions().get(1).getInstallments().get(1)
    );
    // fix some field values for the selected installments
    installmentToMap.forEach(installment -> {
      installment.setStatus(InstallmentStatus.TO_SYNC);
      installment.getTransfers().remove(1);
      installment.setDueDate(null);
    });
    // fix some field values for the other installments
    debtPosition.getPaymentOptions().get(1).getInstallments().get(0).setStatus(InstallmentStatus.TO_SYNC);

    //when
    List<Pair<String,NewDebtPositionRequest>> response = acaDebtPositionMapper.mapToNewDebtPositionRequest(debtPosition);
    //verify
    Assertions.assertNotNull(response);
    Assertions.assertEquals(installmentToMap.size(), response.size());
    for(int idx = 0; idx < installmentToMap.size(); idx++) {
      Assertions.assertEquals(Constants.MAX_EXPIRATION_DATE, response.get(idx).getRight().getExpirationDate());
    }
    response.forEach(TestUtils::checkNotNullFields);
  }

  //endregion

}
