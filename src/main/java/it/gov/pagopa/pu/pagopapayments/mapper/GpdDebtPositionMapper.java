package it.gov.pagopa.pu.pagopapayments.mapper;

import it.gov.pagopa.nodo.gpd.dto.generated.*;
import it.gov.pagopa.nodo.gpd.dto.generated.Stamp;
import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import it.gov.pagopa.pu.pagopapayments.enums.Operation;
import it.gov.pagopa.pu.pagopapayments.exception.InvalidValueException;
import it.gov.pagopa.pu.pagopapayments.util.ConversionUtils;
import it.gov.pagopa.pu.pagopapayments.util.ErrorCodeConstants;
import it.gov.pagopa.pu.pagopapayments.util.Utilities;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
@Slf4j
public class GpdDebtPositionMapper {
  public static final Set<InstallmentStatus> STATUS_TO_SEND_GPD = Set.of(InstallmentStatus.TO_SYNC);
  private static final Set<InstallmentStatus> SYNC_STATUS_TO_DELETE =
    Set.of(InstallmentStatus.CANCELLED, InstallmentStatus.INVALID, InstallmentStatus.EXPIRED);
  private static final Set<InstallmentStatus> SYNC_STATUS_FROM_UPDATE_OR_DELETE =
    Set.of(InstallmentStatus.UNPAID, InstallmentStatus.EXPIRED);
  private static final Set<InstallmentStatus> SYNC_STATUS_FROM_INSERT =
    Set.of(InstallmentStatus.DRAFT, InstallmentStatus.UNPAYABLE);


  public Pair<Operation, PaymentPositionModelV3> mapToNewPaymentPositionModel(String iud, DebtPositionDTO debtPosition, String orgName) {
    return debtPosition.getPaymentOptions().stream()
      .flatMap(paymentOption -> paymentOption.getInstallments().stream())
      .filter(installment -> iud.equals(installment.getIud()))
      .filter(this::installment2sendGpd)
      .map(installment -> {
        Operation operation = getOperation(installment);

        PaymentPositionModelV3 model = new PaymentPositionModelV3()
          .iupd(installment.getIupdPagopa())
          .companyName(orgName)
          .paymentOption(List.of(getPaymentOption(installment, debtPosition)));

        return Pair.of(operation, model);
      })
      .findAny()
      .orElseThrow(() -> new InvalidValueException(ErrorCodeConstants.ERROR_CODE_INVALID_INSTALLMENT,
        "Installment with IUD[%s] on debtPosition[%s] not found or with invalid sync state"
          .formatted(iud, debtPosition.getDebtPositionId())
      ));
  }

  private boolean installment2sendGpd(InstallmentDTO installment) {
    return STATUS_TO_SEND_GPD.contains(installment.getStatus());
  }

  private Operation getOperation(InstallmentDTO installment) {
    InstallmentSyncStatus syncStatus = installment.getSyncStatus();

    if (syncStatus == null) {
      throw new InvalidValueException(ErrorCodeConstants.ERROR_CODE_INVALID_SYNC_STATUS, "Sync status is null for installment [%s]".formatted(installment.getIud()));
    }

    if (SYNC_STATUS_FROM_UPDATE_OR_DELETE.contains(syncStatus.getSyncStatusFrom()) &&
      SYNC_STATUS_TO_DELETE.contains(syncStatus.getSyncStatusTo())) {
      return Operation.DELETE;

    } else if (SYNC_STATUS_FROM_UPDATE_OR_DELETE.contains(syncStatus.getSyncStatusFrom()) &&
      syncStatus.getSyncStatusTo().equals(InstallmentStatus.UNPAID)) {
      return Operation.UPDATE;

    } else if (SYNC_STATUS_FROM_INSERT.contains(syncStatus.getSyncStatusFrom()) &&
      syncStatus.getSyncStatusTo().equals(InstallmentStatus.UNPAID)) {
      return Operation.CREATE;
    }

    throw new InvalidValueException(ErrorCodeConstants.ERROR_CODE_INVALID_SYNC_STATUS, "Invalid sync status [%s->%s] for installment [%s]"
      .formatted(syncStatus.getSyncStatusFrom(), syncStatus.getSyncStatusTo(), installment.getIud()));
  }

  private PaymentOptionModelV3 getPaymentOption(InstallmentDTO installment, DebtPositionDTO debtPosition) {
    return PaymentOptionModelV3.builder()
      .debtor(mapDebtor(installment.getDebtor()))
      .switchToExpired(Optional.ofNullable(installment.getSwitchToExpired()).orElse(false))
      .installments(List.of(mapInstallment(installment)))
      .description(Utilities.truncateRemittanceInformation(installment.getRemittanceInformation()))
      .validityDate(
        debtPosition.getValidityDate() != null
          ? debtPosition.getValidityDate().atStartOfDay().toString()
          : null
      )
      .retentionDate(null)
      .build();
  }

  private InstallmentModel mapInstallment(InstallmentDTO installment) {
    InstallmentModel model = new InstallmentModel()
      .nav(installment.getNav())
      .iuv(installment.getIuv())
      .amount(installment.getAmountCents())
      .description(Utilities.truncateRemittanceInformation(installment.getRemittanceInformation()))
      .dueDate(
        installment.getDueDate() != null
          ? ConversionUtils.atEndOfDay(installment.getDueDate()).toString()
          : ConversionUtils.MAX_EXPIRATION_DATE.toString()
      )
      .transfer(
        installment.getTransfers().stream()
          .filter(transferDTO -> transferDTO.getAmountCents() != 0)
          .map(this::getTransfer)
          .toList()
      );

    if (installment.getLegacyPaymentMetadata() != null) {
      model.installmentMetadata(
        List.of(
          new InstallmentMetadataModel()
            .key("datiSpecificiRiscossione")
            .value(installment.getLegacyPaymentMetadata())
        )
      );
    }

    return model;
  }

  private DebtorModel mapDebtor(PersonDTO debtor) {

    DebtorModel model = new DebtorModel();

    model.setFiscalCode(debtor.getFiscalCode());
    model.setFullName(Utilities.truncateFullName(debtor.getFullName()));
    model.setStreetName(debtor.getAddress());
    model.setCivicNumber(debtor.getCivic());
    model.setPostalCode(debtor.getPostalCode());
    model.setCity(debtor.getLocation());
    model.setProvince(debtor.getProvince());
    model.setCountry(debtor.getNation());
    model.setEmail(debtor.getEmail());

    String type = debtor.getEntityType().getValue();
    if ("F".equals(type)) {
      model.setType(DebtorModel.TypeEnum.F);
    } else if ("G".equals(type)) {
      model.setType(DebtorModel.TypeEnum.G);
    } else {
      throw new InvalidValueException(ErrorCodeConstants.ERROR_CODE_INVALID_DEBTOR, "Unsupported debtor entity type [%s]".formatted(type));
    }

    return model;
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
