package it.gov.pagopa.pu.pagopapayments.mapper;

import it.gov.pagopa.nodo.pacreateposition.dto.generated.NewDebtPositionRequest;
import it.gov.pagopa.pu.pagopapayments.dto.generated.*;
import it.gov.pagopa.pu.pagopapayments.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
@Slf4j
public class AcaDebtPositionMapper {

  public static final Set<InstallmentStatus> STATUS_TO_SEND_ACA = Set.of(InstallmentStatus.TO_SYNC);

  private boolean installment2sendAca(InstallmentDTO installment, Long organizationId) {
    if (!STATUS_TO_SEND_ACA.contains(installment.getStatus())) {
      //skip installment whose status is not in the filterInstallmentStatus
      return false;
    }

    if (installment.getTransfers().size() != 1) {
      //if installment has multiple transfer ("multibeneficiario"), is not supported on ACA due to ACA API limitations: ignore it
      log.warn("ACA mapToNewDebtPositionRequest: ignoring installment [{}/{}] beacuse has multiple transfer[{}]",
        organizationId, installment.getIuv(), installment.getTransfers().size());
      return false;
    }

    return true;
  }

  public List<Pair<String,NewDebtPositionRequest>> mapToNewDebtPositionRequest(DebtPositionDTO debtPosition) {

    return debtPosition.getPaymentOptions().stream()
      .flatMap(paymentOption -> paymentOption.getInstallments().stream())
      .filter(installment -> installment2sendAca(installment, debtPosition.getOrganizationId()))
      .map(installment -> {
        TransferDTO transfer = installment.getTransfers().getFirst();
        PersonDTO debtor = installment.getDebtor();
        return Pair.of(installment.getIud() ,new NewDebtPositionRequest()
          .nav(installment.getNav())
          .iuv(installment.getIuv())
          .paFiscalCode(transfer.getOrgFiscalCode())
          .iban(transfer.getIban())
          .postalIban(transfer.getPostalIban())
          .entityType(NewDebtPositionRequest.EntityTypeEnum.valueOf(debtor.getEntityType()))
          .entityFiscalCode(debtor.getFiscalCode())
          .entityFullName(debtor.getFullName())
          .description(installment.getRemittanceInformation())
          .amount(installment.getAmountCents().intValue())
          .expirationDate(Optional.ofNullable(installment.getDueDate()).orElse(Constants.MAX_EXPIRATION_DATE))
          .switchToExpired(installment.getDueDate()!=null)
          .payStandIn(true));
      }).toList();
  }
}
