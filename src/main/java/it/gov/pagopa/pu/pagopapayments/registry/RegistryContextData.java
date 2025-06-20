package it.gov.pagopa.pu.pagopapayments.registry;

import it.gov.pagopa.pu.pagopapayments.enums.RegistryEventType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class RegistryContextData {
  private String orgFiscalCode;
  private String brokerStationId;
  private String pspId;
  private String pspChannelId;
  private String paymentMethod;
  private String ccp;
  private RegistryEventType eventType;
  private String iuv;
}
