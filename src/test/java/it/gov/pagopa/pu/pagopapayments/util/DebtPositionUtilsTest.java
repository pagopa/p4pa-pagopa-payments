package it.gov.pagopa.pu.pagopapayments.util;

import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentSyncStatus;
import it.gov.pagopa.pu.debtpositions.dto.generated.TransferDTO;
import it.gov.pagopa.pu.organization.dto.generated.Broker;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.jemos.podam.api.PodamFactory;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DebtPositionUtilsTest {
  private final PodamFactory podamFactory = TestUtils.getPodamFactory();

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

  @Test
  void givenOwnerTransferWhenResolveOrganizationInfoThenReturnTransferInfo() {
    Organization organization = podamFactory.manufacturePojo(Organization.class);
    Broker broker = podamFactory.manufacturePojo(Broker.class);
    TransferDTO transfer = podamFactory.manufacturePojo( TransferDTO.class);
    transfer.setFlagOwner(true);

    Pair<String, String> resultPair = DebtPositionUtils.resolveOrganizationInfo(organization, broker, List.of(transfer));

    Assertions.assertEquals(transfer.getOrgFiscalCode(),resultPair.getLeft());
    Assertions.assertEquals(transfer.getOrgName(),resultPair.getRight());
  }

  @Test
  void givenOwnerTransferWithNoOrgNameWhenResolveOrganizationInfoThenReturnTransferInfo() {
    Organization organization = podamFactory.manufacturePojo(Organization.class);
    Broker broker = podamFactory.manufacturePojo(Broker.class);
    TransferDTO transfer = podamFactory.manufacturePojo( TransferDTO.class);
    transfer.setFlagOwner(true);
    transfer.setOrgName("");

    Pair<String, String> resultPair = DebtPositionUtils.resolveOrganizationInfo(organization, broker, List.of(transfer));

    Assertions.assertEquals(transfer.getOrgFiscalCode(),resultPair.getLeft());
    Assertions.assertEquals(organization.getOrgName(),resultPair.getRight());
  }

  @Test
  void givenOwnerTransferWithNoOrgFiscalCodeWhenResolveOrganizationInfoThenReturnTransferInfo() {
    Organization organization = podamFactory.manufacturePojo(Organization.class);
    Broker broker = podamFactory.manufacturePojo(Broker.class);
    TransferDTO transfer = podamFactory.manufacturePojo( TransferDTO.class);
    transfer.setFlagOwner(true);
    transfer.setOrgFiscalCode("");

    Pair<String, String> resultPair = DebtPositionUtils.resolveOrganizationInfo(organization, broker, List.of(transfer));

    Assertions.assertEquals(organization.getOrgFiscalCode(),resultPair.getLeft());
    Assertions.assertEquals(transfer.getOrgName(),resultPair.getRight());
  }

  @Test
  void givenNoOwnerTransferWhenResolveOrganizationInfoThenReturnOrganizationInfo() {
    Organization organization = podamFactory.manufacturePojo(Organization.class);
    Broker broker = podamFactory.manufacturePojo(Broker.class);
    TransferDTO transfer = podamFactory.manufacturePojo( TransferDTO.class);
    transfer.setFlagOwner(false);

    Pair<String, String> resultPair = DebtPositionUtils.resolveOrganizationInfo(organization, broker, List.of(transfer));

    Assertions.assertEquals(organization.getOrgFiscalCode(),resultPair.getLeft());
    Assertions.assertEquals(organization.getOrgName(),resultPair.getRight());
  }
}
