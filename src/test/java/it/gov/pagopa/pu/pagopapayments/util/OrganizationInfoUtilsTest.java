package it.gov.pagopa.pu.pagopapayments.util;

import it.gov.pagopa.pu.debtpositions.dto.generated.TransferDTO;
import it.gov.pagopa.pu.organization.dto.generated.Broker;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.pagopapayments.domain.OrganizationInfo;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OrganizationInfoUtilsTest {

  @Test
  void whenBrokerIsNullReturnOrganizationData() {
    Organization organization = mock(Organization.class);
    when(organization.getOrgFiscalCode()).thenReturn("ORG_FISCAL");
    when(organization.getOrgName()).thenReturn("ORG_NAME");

    OrganizationInfo info = OrganizationInfoUtils.resolveOrganizationInfo(organization, null, List.of());

    assertEquals("ORG_FISCAL", info.fiscalCodePA());
    assertEquals("ORG_NAME", info.companyName());
  }

  @Test
  void whenBrokerNotDelegatedReturnOrganizationData() {
    Organization organization = mock(Organization.class);
    when(organization.getOrgFiscalCode()).thenReturn("ORG_FISCAL");
    when(organization.getOrgName()).thenReturn("ORG_NAME");

    Broker broker = mock(Broker.class);
    when(broker.getFlagDelegate()).thenReturn(false);

    OrganizationInfo info = OrganizationInfoUtils.resolveOrganizationInfo(organization, broker, List.of());

    assertEquals("ORG_FISCAL", info.fiscalCodePA());
    assertEquals("ORG_NAME", info.companyName());
  }

  @Test
  void whenDelegatedButNoOwnerTransferReturnOrganizationData() {
    Organization organization = mock(Organization.class);
    when(organization.getOrgFiscalCode()).thenReturn("ORG_FISCAL");
    when(organization.getOrgName()).thenReturn("ORG_NAME");

    Broker broker = mock(Broker.class);
    when(broker.getFlagDelegate()).thenReturn(true);

    TransferDTO t1 = mock(TransferDTO.class);
    when(t1.getFlagOwner()).thenReturn(false);

    TransferDTO t2 = mock(TransferDTO.class);
    when(t2.getFlagOwner()).thenReturn(null);

    OrganizationInfo info = OrganizationInfoUtils.resolveOrganizationInfo(organization, broker, List.of(t1, t2));

    assertEquals("ORG_FISCAL", info.fiscalCodePA());
    assertEquals("ORG_NAME", info.companyName());
  }

  @Test
  void whenDelegatedAndOwnerHasBothValuesUsesOwnerData() {
    Organization organization = mock(Organization.class);
    when(organization.getOrgFiscalCode()).thenReturn("ORG_FISCAL");
    when(organization.getOrgName()).thenReturn("ORG_NAME");

    Broker broker = mock(Broker.class);
    when(broker.getFlagDelegate()).thenReturn(true);

    TransferDTO owner = mock(TransferDTO.class);
    when(owner.getFlagOwner()).thenReturn(true);
    when(owner.getOrgFiscalCode()).thenReturn("OWNER_FISCAL");
    when(owner.getOrgName()).thenReturn("OWNER_NAME");

    OrganizationInfo info = OrganizationInfoUtils.resolveOrganizationInfo(organization, broker, List.of(owner));

    assertEquals("OWNER_FISCAL", info.fiscalCodePA());
    assertEquals("OWNER_NAME", info.companyName());
  }

  @Test
  void whenDelegatedAndOwnerFiscalBlankUpdatesOnlyCompanyName() {
    Organization organization = mock(Organization.class);
    when(organization.getOrgFiscalCode()).thenReturn("ORG_FISCAL");
    when(organization.getOrgName()).thenReturn("ORG_NAME");

    Broker broker = mock(Broker.class);
    when(broker.getFlagDelegate()).thenReturn(true);

    TransferDTO owner = mock(TransferDTO.class);
    when(owner.getFlagOwner()).thenReturn(true);
    when(owner.getOrgFiscalCode()).thenReturn("   ");
    when(owner.getOrgName()).thenReturn("OWNER_NAME");

    OrganizationInfo info = OrganizationInfoUtils.resolveOrganizationInfo(
      organization, broker, List.of(owner)
    );

    assertEquals("ORG_FISCAL", info.fiscalCodePA());
    assertEquals("OWNER_NAME", info.companyName());
  }

  @Test
  void whenDelegatedAndOwnerNameBlankUpdatesOnlyFiscalCode() {
    Organization organization = mock(Organization.class);
    when(organization.getOrgFiscalCode()).thenReturn("ORG_FISCAL");
    when(organization.getOrgName()).thenReturn("ORG_NAME");

    Broker broker = mock(Broker.class);
    when(broker.getFlagDelegate()).thenReturn(true);

    TransferDTO owner = mock(TransferDTO.class);
    when(owner.getFlagOwner()).thenReturn(true);
    when(owner.getOrgFiscalCode()).thenReturn("OWNER_FISCAL");
    when(owner.getOrgName()).thenReturn("");

    OrganizationInfo info = OrganizationInfoUtils.resolveOrganizationInfo(organization, broker, List.of(owner));

    assertEquals("OWNER_FISCAL", info.fiscalCodePA());
    assertEquals("ORG_NAME", info.companyName());
  }

  @Test
  void whenTransfersIsNullThrowsNullPointerException() {
    Organization organization = mock(Organization.class);
    when(organization.getOrgFiscalCode()).thenReturn("ORG_FISCAL");
    when(organization.getOrgName()).thenReturn("ORG_NAME");

    Broker broker = mock(Broker.class);
    when(broker.getFlagDelegate()).thenReturn(true);

    assertThrows(NullPointerException.class, () ->
      OrganizationInfoUtils.resolveOrganizationInfo(organization, broker, null)
    );
  }
}
