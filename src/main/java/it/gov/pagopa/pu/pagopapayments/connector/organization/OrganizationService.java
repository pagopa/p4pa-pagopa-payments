package it.gov.pagopa.pu.pagopapayments.connector.organization;

import it.gov.pagopa.pu.organization.dto.generated.Organization;

public interface OrganizationService {
  Organization getOrganizationById(Long organizationId, String accessToken);
  Organization getOrganizationByFiscalCode(String organizationFiscalCode, String accessToken);
  String getOrganizationApiKey(Long organizationId, String accessToken);
}
