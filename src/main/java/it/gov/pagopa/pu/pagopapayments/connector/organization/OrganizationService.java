package it.gov.pagopa.pu.pagopapayments.connector.organization;

import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.organization.dto.generated.OrganizationApiKeyType;

public interface OrganizationService {
  Organization getOrganizationById(Long organizationId, String accessToken);
  Organization getOrganizationByFiscalCode(String organizationFiscalCode, String accessToken);
  String getOrganizationApiKey(Long organizationId, OrganizationApiKeyType organizationApiKeyType, String accessToken);
}
