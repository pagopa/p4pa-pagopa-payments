package it.gov.pagopa.pu.pagopapayments.connector;

import it.gov.pagopa.pu.organization.dto.generated.Broker;
import it.gov.pagopa.pu.organization.dto.generated.BrokerApiKeys;
import it.gov.pagopa.pu.organization.dto.generated.Organization;

public interface OrganizationClient {

  BrokerApiKeys getApiKeyByBrokerId(Long brokerId, String accessToken);

  Broker getBrokerById(Long brokerId, String accessToken);

  Organization getOrganizationById(Long organizationId, String accessToken);

  Organization getOrganizationByFiscalCode(String organizationFiscalCode, String accessToken);
}
