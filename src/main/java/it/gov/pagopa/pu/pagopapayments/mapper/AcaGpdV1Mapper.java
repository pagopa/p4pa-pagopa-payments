package it.gov.pagopa.pu.pagopapayments.mapper;

import it.gov.pagopa.pu.aca.gpd.v1.dto.generated.Stamp;
import it.gov.pagopa.pu.aca.gpd.v1.dto.generated.*;
import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.pagopapayments.enums.Operation;
import it.gov.pagopa.pu.pagopapayments.exception.InvalidValueException;
import it.gov.pagopa.pu.pagopapayments.util.ConversionUtils;
import it.gov.pagopa.pu.pagopapayments.util.Utilities;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
@Slf4j
public class AcaGpdV1Mapper {

  public static final Set<InstallmentStatus> STATUS_TO_SEND_GPD = Set.of(InstallmentStatus.TO_SYNC);
  private static final Set<InstallmentStatus> SYNC_STATUS_TO_DELETE = Set.of(InstallmentStatus.CANCELLED, InstallmentStatus.INVALID, InstallmentStatus.EXPIRED);
  private static final Set<InstallmentStatus> SYNC_STATUS_FROM_UPDATE_OR_DELETE = Set.of(InstallmentStatus.UNPAID, InstallmentStatus.EXPIRED);
  private static final Set<InstallmentStatus> SYNC_STATUS_FROM_INSERT = Set.of(InstallmentStatus.DRAFT, InstallmentStatus.UNPAYABLE);

  private boolean installment2sendGpd(InstallmentDTO installment) {
    //skip installment whose status is not in the filterInstallmentStatus
    return STATUS_TO_SEND_GPD.contains(installment.getStatus());
  }

  public Pair<Operation, PaymentPositionModel> mapToNewPaymentPositionModel(String iud, DebtPositionDTO debtPosition, Organization org) {
    return debtPosition.getPaymentOptions().stream()
      .flatMap(paymentOption -> paymentOption.getInstallments().stream())
      .filter(installment -> iud.equals(installment.getIud()))
      .filter(this::installment2sendGpd)
      .map(installment -> {
        Operation operation = getOperation(installment);
        PersonDTO debtor = installment.getDebtor();
        return Pair.of(operation, new PaymentPositionModel()
          .iupd(installment.getIupdPagopa())
          .type(PaymentPositionModel.TypeEnum.valueOf(debtor.getEntityType().getValue()))
          .fiscalCode(debtor.getFiscalCode())
          .fullName(Utilities.truncateFullName(debtor.getFullName()))
          .streetName(debtor.getAddress())
          .civicNumber(debtor.getCivic())
          .postalCode(debtor.getPostalCode())
          .city(debtor.getLocation())
          .province(debtor.getProvince())
          .country(debtor.getNation())
          .email(debtor.getEmail())
          .switchToExpired(Optional.ofNullable(installment.getSwitchToExpired()).orElse(false))
          .companyName(org.getOrgName())
          .paymentOption(List.of(getPaymentOption(installment)))
          .validityDate(debtPosition.getValidityDate() != null ? debtPosition.getValidityDate().atStartOfDay().toString() : null)
        );
      }).findAny().orElseThrow(() -> new InvalidValueException("Installment with IUD[%s] on debtPosition[%s] not found or with invalid sync state".formatted(iud, debtPosition.getDebtPositionId())));
  }

  private Operation getOperation(InstallmentDTO installment) {
    Operation operation;
    InstallmentSyncStatus syncStatus = installment.getSyncStatus();

    if (syncStatus == null) {
      throw new InvalidValueException("Sync status is null for installment [%s]".formatted(installment.getIud()));
    }

    if (SYNC_STATUS_FROM_UPDATE_OR_DELETE.contains(syncStatus.getSyncStatusFrom()) &&
      SYNC_STATUS_TO_DELETE.contains(syncStatus.getSyncStatusTo())) {
      operation = Operation.DELETE;
    } else if (SYNC_STATUS_FROM_UPDATE_OR_DELETE.contains(syncStatus.getSyncStatusFrom()) &&
      syncStatus.getSyncStatusTo().equals(InstallmentStatus.UNPAID)) {
      operation = Operation.UPDATE;
    } else if (SYNC_STATUS_FROM_INSERT.contains(syncStatus.getSyncStatusFrom()) &&
      syncStatus.getSyncStatusTo().equals(InstallmentStatus.UNPAID)) {
      operation = Operation.CREATE;
    } else {
      throw new InvalidValueException("Invalid sync status [%s->%s] for installment [%s]".formatted(
        syncStatus.getSyncStatusFrom(), syncStatus.getSyncStatusTo(), installment.getIud()));
    }
    return operation;
  }

  private PaymentOptionModel getPaymentOption(InstallmentDTO installment) {
    return PaymentOptionModel.builder()
      .nav(installment.getNav())
      .iuv(installment.getIuv())
      .amount(installment.getAmountCents())
      .description(Utilities.truncateRemittanceInformation(installment.getRemittanceInformation()))
      .isPartialPayment(false)
      .dueDate(installment.getDueDate() != null ? ConversionUtils.atEndOfDay(installment.getDueDate()).toString() : ConversionUtils.MAX_EXPIRATION_DATE.toString())
      .fee(0L)
      .notificationFee(0L)
      .paymentOptionMetadata(installment.getLegacyPaymentMetadata() != null ?
        List.of(PaymentOptionMetadataModel.builder()
          .key("datiSpecificiRiscossione")
          .value(installment.getLegacyPaymentMetadata())
          .build()) : null)
      .transfer(installment.getTransfers().stream()
        .map(this::getTransfer).toList())
      .build();
  }

  private TransferModel getTransfer(TransferDTO transfer) {
    boolean isStamp = transfer.getStampHashDocument() != null;
    Stamp stamp = null;
    if (isStamp) {
      stamp = Stamp.builder()
        .stampType(transfer.getStampType())
        .hashDocument(transfer.getStampHashDocument())
        .provincialResidence(transfer.getStampProvincialResidence())
        .build();
    }

    return TransferModel.builder()
      .idTransfer(TransferModel.IdTransferEnum.fromValue(transfer.getTransferIndex().toString()))
      .amount(transfer.getAmountCents())
      .organizationFiscalCode(transfer.getOrgFiscalCode())
      .remittanceInformation(Utilities.truncateRemittanceInformation(transfer.getRemittanceInformation()))
      .category(transfer.getCategory())
      .iban(transfer.getIban())
      .postalIban(transfer.getPostalIban())
      .companyName(transfer.getOrgName())
      .stamp(stamp)
      .build();
  }
}

