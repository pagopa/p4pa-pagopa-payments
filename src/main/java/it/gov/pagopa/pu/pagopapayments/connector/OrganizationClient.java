package it.gov.pagopa.pu.pagopapayments.connector;

import it.gov.pagopa.pu.p4pa_organization.dto.generated.EntityModelOrganization;

public interface OrganizationClient {

  String getAcaApiKeyByBrokerId(Long brokerId);

  EntityModelOrganization getOrganizationById(Long organizationId);
}
