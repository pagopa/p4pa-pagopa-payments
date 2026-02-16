package it.gov.pagopa.pu.pagopapayments.util;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionOrigin;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;

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
}
