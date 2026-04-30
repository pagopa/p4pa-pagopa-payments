package it.gov.pagopa.pu.pagopapayments.util;

import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import it.gov.pagopa.pu.organization.dto.generated.Broker;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Set;

public class DebtPositionUtils {

  private DebtPositionUtils() {
  }

  //region DebtPositionOrigins filter
  public static final List<DebtPositionOrigin> ORDINARY_DEBT_POSITION_ORIGINS = List.of(
    DebtPositionOrigin.ORDINARY,
    DebtPositionOrigin.ORDINARY_SIL,
    DebtPositionOrigin.SPONTANEOUS,
    DebtPositionOrigin.SPONTANEOUS_SIL,
    DebtPositionOrigin.SPONTANEOUS_PSP);

  public static final Set<DebtPositionOrigin> ACA_EXCLUDED_ORIGINS = Set.of(
    DebtPositionOrigin.SPONTANEOUS,
    DebtPositionOrigin.SPONTANEOUS_SIL,
    DebtPositionOrigin.SPONTANEOUS_MIXED,
    DebtPositionOrigin.SPONTANEOUS_PSP
  );
  //endregion

  //region Statuses filter
  public static final Set<InstallmentStatus> PAID_INSTALLMENT_STATUSES = Set.of(
    InstallmentStatus.PAID,
    InstallmentStatus.REPORTED);

  public static final Set<InstallmentStatus> PAYABLE_INSTALLMENT_STATUSES = Set.of(
    InstallmentStatus.UNPAID);

  public static final Set<PaymentOptionStatus> PAYABLE_PAYMENT_OPTION_STATUSES = Set.of(
    PaymentOptionStatus.TO_SYNC,
    PaymentOptionStatus.UNPAID,
    PaymentOptionStatus.PARTIALLY_PAID);

  public static boolean isPayableInstallment(InstallmentDTO installment) {
    if (installment == null || installment.getStatus() == null) {
      return false;
    }

    InstallmentStatus status = InstallmentStatus.TO_SYNC.equals(installment.getStatus())
      ? (installment.getSyncStatus() != null ? installment.getSyncStatus().getSyncStatusTo() : null)
      : installment.getStatus();

    return status != null && PAYABLE_INSTALLMENT_STATUSES.contains(status);
  }
  //endregion

  public static Pair<String, String> resolveOrganizationInfo(Organization organization, Broker broker, List<TransferDTO> transfers) {
    String fiscalCodePA = organization.getOrgFiscalCode();
    String companyName = organization.getOrgName();

    if (broker != null && Boolean.TRUE.equals(broker.getFlagDelegate())) {
      TransferDTO owner = transfers.stream()
        .filter(t -> Boolean.TRUE.equals(t.getFlagOwner()))
        .findFirst()
        .orElse(null);

      if (owner != null) {
        if (StringUtils.isNotBlank(owner.getOrgFiscalCode())) {
          fiscalCodePA = owner.getOrgFiscalCode();
        }
        if (StringUtils.isNotBlank(owner.getOrgName())) {
          companyName = owner.getOrgName();
        }
      }
    }

    return Pair.of(fiscalCodePA, companyName);
  }
}
