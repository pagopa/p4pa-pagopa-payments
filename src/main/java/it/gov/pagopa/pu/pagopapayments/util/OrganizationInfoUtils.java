package it.gov.pagopa.pu.pagopapayments.util;

import it.gov.pagopa.pu.debtpositions.dto.generated.TransferDTO;
import it.gov.pagopa.pu.organization.dto.generated.Broker;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public final class OrganizationInfoUtils {

  private OrganizationInfoUtils() {
  }

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
