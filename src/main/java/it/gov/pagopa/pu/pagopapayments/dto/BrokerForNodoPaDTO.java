package it.gov.pagopa.pu.pagopapayments.dto;

import it.gov.pagopa.pu.organization.dto.generated.Broker;
import it.gov.pagopa.pu.organization.dto.generated.BrokerApiKeys;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class BrokerForNodoPaDTO {
  private Broker broker;
  private Organization organization;
  private BrokerApiKeys brokerApiKeys;
}
