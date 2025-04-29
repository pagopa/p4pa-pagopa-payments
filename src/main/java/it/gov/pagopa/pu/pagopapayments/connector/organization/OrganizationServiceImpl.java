package it.gov.pagopa.pu.pagopapayments.connector.organization;

import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.organization.dto.generated.OrganizationApiKeyType;
import it.gov.pagopa.pu.pagopapayments.connector.organization.client.OrganizationClient;
import org.springframework.stereotype.Service;

@Service
public class OrganizationServiceImpl implements OrganizationService {

  private final OrganizationClient organizationClient;

  public OrganizationServiceImpl(OrganizationClient organizationClient) {
    this.organizationClient = organizationClient;
  }

  @Override
  public Organization getOrganizationById(Long organizationId, String accessToken) {
    return organizationClient.getOrganizationById(organizationId, accessToken);
  }

  @Override
  public Organization getOrganizationByFiscalCode(String organizationFiscalCode, String accessToken) {
    return organizationClient.getOrganizationByFiscalCode(organizationFiscalCode, accessToken);
  }

  @Override
  public String getOrganizationApiKey(Long organizationId, OrganizationApiKeyType organizationApiKeyType, String accessToken) {
    return organizationClient.getOrganizationApiKey(organizationId, organizationApiKeyType, accessToken);
  }
}
