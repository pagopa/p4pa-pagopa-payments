package it.gov.pagopa.pu.pagopapayments.mapper;

import it.gov.pagopa.nodo.gpd.dto.generated.Stamp;
import it.gov.pagopa.nodo.gpd.dto.generated.*;
import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.pagopapayments.exception.InvalidValueException;
import it.gov.pagopa.pu.pagopapayments.util.ConversionUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
@Slf4j
public class GpdDebtPositionMapper {

  public static final Set<InstallmentDTO.StatusEnum> STATUS_TO_SEND_GPD = Set.of(InstallmentDTO.StatusEnum.TO_SYNC);
  private static final Set<InstallmentDTO.StatusEnum> SYNC_STATUS_TO_DELETE = Set.of(InstallmentDTO.StatusEnum.CANCELLED, InstallmentDTO.StatusEnum.INVALID, InstallmentDTO.StatusEnum.EXPIRED);
  private static final Set<InstallmentDTO.StatusEnum> SYNC_STATUS_FROM_UPDATE_OR_DELETE = Set.of(InstallmentDTO.StatusEnum.UNPAID, InstallmentDTO.StatusEnum.EXPIRED);
  private static final Set<InstallmentDTO.StatusEnum> SYNC_STATUS_FROM_INSERT = Set.of(InstallmentDTO.StatusEnum.DRAFT);

  private boolean installment2sendGpd(InstallmentDTO installment) {
    //skip installment whose status is not in the filterInstallmentStatus
    return STATUS_TO_SEND_GPD.contains(installment.getStatus());
  }

  public Pair<OPERATION, PaymentPositionModel> mapToNewPaymentPositionModel(String iud, DebtPositionDTO debtPosition, Organization org) {
    return debtPosition.getPaymentOptions().stream()
      .flatMap(paymentOption -> paymentOption.getInstallments().stream())
      .filter(installment -> iud.equals(installment.getIud()))
      .filter(this::installment2sendGpd)
      .map(installment -> {
        OPERATION operation = getOperation(installment);
        PersonDTO debtor = installment.getDebtor();
        return Pair.of(operation, new PaymentPositionModel()
          .iupd(installment.getIupdPagopa())
          .type(PaymentPositionModel.TypeEnum.valueOf(debtor.getEntityType().getValue()))
          .fiscalCode(debtor.getFiscalCode())
          .fullName(debtor.getFullName())
          .streetName(debtor.getAddress())
          .civicNumber(debtor.getCivic())
          .postalCode(debtor.getPostalCode())
          .city(debtor.getLocation())
          .province(debtor.getProvince())
          .country(debtor.getNation())
          .email(debtor.getEmail())
          .switchToExpired(installment.getDueDate() != null)
          .companyName(org.getOrgName())
          .paymentOption(List.of(getPaymentOption(installment)))
          .validityDate(ConversionUtils.atEndOfDay(debtPosition.getValidityDate()))
        );
      }).findAny().orElseThrow(() -> new InvalidValueException("Installment with IUD[%s] on debtPosition[%s] not found or with invalid sync state".formatted(iud, debtPosition.getDebtPositionId())));
  }

  private OPERATION getOperation(InstallmentDTO installment) {
    OPERATION operation;
    InstallmentSyncStatus syncStatus = installment.getSyncStatus();

    if (syncStatus == null) {
      throw new InvalidValueException("Sync status is null for installment [%s]".formatted(installment.getIud()));
    }

    if (SYNC_STATUS_FROM_UPDATE_OR_DELETE.contains(InstallmentDTO.StatusEnum.valueOf(syncStatus.getSyncStatusFrom().name())) &&
      SYNC_STATUS_TO_DELETE.contains(InstallmentDTO.StatusEnum.valueOf(syncStatus.getSyncStatusTo().name()))) {
      operation = OPERATION.DELETE;
    } else if (SYNC_STATUS_FROM_UPDATE_OR_DELETE.contains(InstallmentDTO.StatusEnum.valueOf(syncStatus.getSyncStatusFrom().name())) &&
      syncStatus.getSyncStatusTo().name().equals(InstallmentDTO.StatusEnum.UNPAID.name())) {
      operation = OPERATION.UPDATE;
    } else if (SYNC_STATUS_FROM_INSERT.contains(InstallmentDTO.StatusEnum.valueOf(syncStatus.getSyncStatusFrom().name())) &&
      syncStatus.getSyncStatusTo().name().equals(InstallmentDTO.StatusEnum.UNPAID.name())) {
      operation = OPERATION.CREATE;
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
      .description(installment.getRemittanceInformation())
      .isPartialPayment(false)
      .dueDate(ConversionUtils.atEndOfDay(installment.getDueDate()))
      .fee(0L)
      .notificationFee(0L)
      .paymentOptionMetadata(List.of(PaymentOptionMetadataModel.builder()
        .key("datiSpecificiRiscossione")
        .value(installment.getLegacyPaymentMetadata())
        .build()))
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
      .remittanceInformation(transfer.getRemittanceInformation())
      .category(transfer.getCategory())
      .iban(transfer.getIban())
      .postalIban(transfer.getPostalIban())
      .companyName(transfer.getOrgName())
      .stamp(stamp)
      .build();

  }


  public enum OPERATION {CREATE, UPDATE, DELETE}
}
