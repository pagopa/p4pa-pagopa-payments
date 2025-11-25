package it.gov.pagopa.pu.pagopapayments.mapper;

import it.gov.pagopa.nodo.pacreateposition.dto.generated.NewDebtPositionRequest;
import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import it.gov.pagopa.pu.pagopapayments.exception.InvalidValueException;
import it.gov.pagopa.pu.pagopapayments.util.ConversionUtils;
import it.gov.pagopa.pu.pagopapayments.util.Utilities;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;

@Component
@Slf4j
public class AcaDebtPositionMapper {

  public static final Set<InstallmentStatus> STATUS_TO_SEND_ACA = Set.of(InstallmentStatus.TO_SYNC);
  private static final Set<InstallmentStatus> SYNC_STATUS_TO_DELETE = Set.of(InstallmentStatus.CANCELLED, InstallmentStatus.INVALID, InstallmentStatus.EXPIRED);
  private static final Set<InstallmentStatus> SYNC_STATUS_FROM_UPDATE_OR_DELETE = Set.of(InstallmentStatus.UNPAID, InstallmentStatus.EXPIRED);
  private static final Set<InstallmentStatus> SYNC_STATUS_FROM_INSERT = Set.of(InstallmentStatus.DRAFT, InstallmentStatus.UNPAYABLE);

  private boolean installment2sendAca(InstallmentDTO installment, Long organizationId) {
    if (!STATUS_TO_SEND_ACA.contains(installment.getStatus())) {
      //skip installment whose status is not in the filterInstallmentStatus
      return false;
    }

    if (installment.getTransfers().size() != 1) {
      //if installment has multiple transfer ("multibeneficiario"), is not supported on ACA due to ACA API limitations: ignore it
      log.warn("ACA mapToNewDebtPositionRequest: ignoring installment [{}/{}] because has multiple transfer[{}]",
        organizationId, installment.getIuv(), installment.getTransfers().size());
      return false;
    }

    return true;
  }

  public Pair<OPERATION, NewDebtPositionRequest> mapToNewDebtPositionRequest(String iud, DebtPositionDTO debtPosition) {
    return debtPosition.getPaymentOptions().stream()
      .flatMap(paymentOption -> paymentOption.getInstallments().stream())
      .filter(installment -> iud.equals(installment.getIud()))
      .filter(installment -> installment2sendAca(installment, debtPosition.getOrganizationId()))
      .map(installment -> {
        OPERATION operation = getOperation(installment);
        TransferDTO transfer = installment.getTransfers().getFirst();
        PersonDTO debtor = installment.getDebtor();
        return Pair.of(operation, new NewDebtPositionRequest()
          .nav(installment.getNav())
          .iuv(installment.getIuv())
          .paFiscalCode(transfer.getOrgFiscalCode())
          .iban(transfer.getIban())
          .postalIban(transfer.getPostalIban())
          .entityType(NewDebtPositionRequest.EntityTypeEnum.valueOf(debtor.getEntityType().getValue()))
          .entityFiscalCode(debtor.getFiscalCode())
          .entityFullName(Utilities.truncateFullName(debtor.getFullName()))
          .description(Utilities.truncateRemittanceInformation(installment.getRemittanceInformation()))
          .amount(installment.getAmountCents().intValue())
          .expirationDate(ConversionUtils.localDate2RomeMaxTime(installment.getDueDate()))
          .switchToExpired(Optional.ofNullable(installment.getSwitchToExpired()).orElse(false))
          .payStandIn(true));
      }).findAny().orElseThrow(() -> new InvalidValueException("Installment with IUD[%s] on debtPosition[%s] not found or with invalid sync state".formatted(iud, debtPosition.getDebtPositionId())));
  }

  private OPERATION getOperation(InstallmentDTO installment) {
    OPERATION operation;
    InstallmentSyncStatus syncStatus = installment.getSyncStatus();

    if(syncStatus==null){
      throw new InvalidValueException("Sync status is null for installment [%s]".formatted(installment.getIud()));
    }

    if(SYNC_STATUS_FROM_UPDATE_OR_DELETE.contains(syncStatus.getSyncStatusFrom()) &&
      SYNC_STATUS_TO_DELETE.contains(syncStatus.getSyncStatusTo())){
      operation = OPERATION.DELETE;
    } else if(SYNC_STATUS_FROM_UPDATE_OR_DELETE.contains(syncStatus.getSyncStatusFrom()) &&
      syncStatus.getSyncStatusTo().equals(InstallmentStatus.UNPAID)){
      operation = OPERATION.UPDATE;
    } else if(SYNC_STATUS_FROM_INSERT.contains(syncStatus.getSyncStatusFrom()) &&
      syncStatus.getSyncStatusTo().equals(InstallmentStatus.UNPAID)){
      operation = OPERATION.CREATE;
    } else {
      throw new InvalidValueException("Invalid sync status [%s->%s] for installment [%s]".formatted(
        syncStatus.getSyncStatusFrom(), syncStatus.getSyncStatusTo(), installment.getIud()));
    }
    return operation;
  }

  public enum OPERATION { CREATE, UPDATE, DELETE }
}
