package it.gov.pagopa.pu.pagopapayments.util;

import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentSyncStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DebtPositionUtilsTest {

  @Test
  void testIsPayableInstallmentWhenInstallmentIsNullThenReturnFalse() {
    assertFalse(DebtPositionUtils.isPayableInstallment(null));
  }

  @Test
  void testIsPayableInstallmentWhenStatusIsNullThenReturnFalse() {
    InstallmentDTO inst = mock(InstallmentDTO.class);
    when(inst.getStatus()).thenReturn(null);

    assertFalse(DebtPositionUtils.isPayableInstallment(inst));
  }

  @Test
  void testIsPayableInstallmentWhenStatusIsPayableThenReturnTrue() {
    InstallmentDTO inst = mock(InstallmentDTO.class);
    when(inst.getStatus()).thenReturn(InstallmentStatus.UNPAID);

    assertTrue(DebtPositionUtils.isPayableInstallment(inst));
  }

  @Test
  void testIsPayableInstallmentWhenStatusIsNotPayableAndNotToSyncThenReturnFalse() {
    InstallmentDTO inst = mock(InstallmentDTO.class);
    when(inst.getStatus()).thenReturn(InstallmentStatus.PAID);

    assertFalse(DebtPositionUtils.isPayableInstallment(inst));
  }

  @Test
  void testIsPayableInstallmentWhenToSyncAndSyncStatusToIsPayableThenReturnTrue() {
    InstallmentSyncStatus sync = mock(InstallmentSyncStatus.class);
    when(sync.getSyncStatusTo()).thenReturn(InstallmentStatus.UNPAID);

    InstallmentDTO inst = mock(InstallmentDTO.class);
    when(inst.getStatus()).thenReturn(InstallmentStatus.TO_SYNC);
    when(inst.getSyncStatus()).thenReturn(sync);

    assertTrue(DebtPositionUtils.isPayableInstallment(inst));
  }

  @Test
  void testIsPayableInstallmentWhenToSyncAndSyncStatusIsNullThenReturnFalse() {
    InstallmentDTO inst = mock(InstallmentDTO.class);
    when(inst.getStatus()).thenReturn(InstallmentStatus.TO_SYNC);
    when(inst.getSyncStatus()).thenReturn(null);

    assertFalse(DebtPositionUtils.isPayableInstallment(inst));
  }

  @Test
  void testIsPayableInstallmentWhenToSyncAndSyncStatusToIsNullThenReturnFalse() {
    InstallmentSyncStatus sync = mock(InstallmentSyncStatus.class);
    when(sync.getSyncStatusTo()).thenReturn(null);

    InstallmentDTO inst = mock(InstallmentDTO.class);
    when(inst.getStatus()).thenReturn(InstallmentStatus.TO_SYNC);
    when(inst.getSyncStatus()).thenReturn(sync);

    assertFalse(DebtPositionUtils.isPayableInstallment(inst));
  }

  @Test
  void testIsPayableInstallmentWhenToSyncAndSyncStatusToIsNotPayableThenReturnFalse() {
    InstallmentSyncStatus sync = mock(InstallmentSyncStatus.class);
    when(sync.getSyncStatusTo()).thenReturn(InstallmentStatus.PAID);

    InstallmentDTO inst = mock(InstallmentDTO.class);
    when(inst.getStatus()).thenReturn(InstallmentStatus.TO_SYNC);
    when(inst.getSyncStatus()).thenReturn(sync);

    assertFalse(DebtPositionUtils.isPayableInstallment(inst));
  }
}
