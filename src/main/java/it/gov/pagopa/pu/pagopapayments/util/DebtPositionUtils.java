package it.gov.pagopa.pu.pagopapayments.util;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionOrigin;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.debtpositions.dto.generated.PaymentOptionStatus;

import java.util.List;
import java.util.Set;

public class DebtPositionUtils {

  private DebtPositionUtils() {
  }

  public static final List<DebtPositionOrigin> ORDINARY_DEBT_POSITION_ORIGINS = List.of(
    DebtPositionOrigin.ORDINARY,
    DebtPositionOrigin.ORDINARY_SIL,
    DebtPositionOrigin.SPONTANEOUS,
    DebtPositionOrigin.SPONTANEOUS_SIL,
    DebtPositionOrigin.SPONTANEOUS_PSP);

  public static final Set<InstallmentStatus> PAID_INSTALLMENT_STATUSES = Set.of(
    InstallmentStatus.PAID,
    InstallmentStatus.REPORTED);

  public static final Set<InstallmentStatus> PAYABLE_INSTALLMENT_STATUSES = Set.of(
    InstallmentStatus.UNPAID);

  public static final Set<PaymentOptionStatus> PAYABLE_PAYMENT_OPTION_STATUSES = Set.of(
    PaymentOptionStatus.TO_SYNC,
    PaymentOptionStatus.UNPAID,
    PaymentOptionStatus.PARTIALLY_PAID);

  public static final Set<DebtPositionOrigin> ACA_EXCLUDED_ORIGINS = Set.of(
    DebtPositionOrigin.SPONTANEOUS,
    DebtPositionOrigin.SPONTANEOUS_SIL,
    DebtPositionOrigin.SPONTANEOUS_MIXED,
    DebtPositionOrigin.SPONTANEOUS_PSP
  );

  public static boolean isPayableInstallment(InstallmentDTO installment) {
    if (installment == null || installment.getStatus() == null) {
      return false;
    }

    InstallmentStatus status = InstallmentStatus.TO_SYNC.equals(installment.getStatus())
      ? (installment.getSyncStatus() != null ? installment.getSyncStatus().getSyncStatusTo() : null)
      : installment.getStatus();

    return status != null && PAYABLE_INSTALLMENT_STATUSES.contains(status);
  }
}
