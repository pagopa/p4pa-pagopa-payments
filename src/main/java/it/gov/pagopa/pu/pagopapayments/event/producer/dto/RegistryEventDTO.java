package it.gov.pagopa.pu.pagopapayments.event.producer.dto;

import it.gov.pagopa.pu.pagopapayments.enums.RegistryEventCategory;
import it.gov.pagopa.pu.pagopapayments.enums.RegistryEventOutcome;
import it.gov.pagopa.pu.pagopapayments.enums.RegistryEventSubType;
import it.gov.pagopa.pu.pagopapayments.enums.RegistryEventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistryEventDTO {
  private String registryId;
  private String registryOrigin;
  private String registryType;
  private OffsetDateTime dateTime;
  private String traceId;
  private String brokerStationId;
  private String orgFiscalCode;
  private String iuv;
  private String nav;
  private String ccp;
  private String pspId;
  private String pspChannelId;
  private String paymentMethod;
  private RegistryEventCategory eventCategory;
  private RegistryEventType eventType;
  private RegistryEventSubType eventSubType;
  private String requestorId;
  private String grantorId;
  private RegistryEventOutcome outcome;
  private String body;
}
