package it.gov.pagopa.pu.pagopapayments.registry;

import lombok.Getter;

@Getter
@SuppressWarnings("java:S115") // Suppressing constant naming warning: this is required to match with the api name
public enum RegistryEventType {
  paVerifyPaymentNotice(true, true),
  paGetPaymentV2(true, true),
  paSendRTV2(true, true),
  newDebtPosition(false, false),
  createPosition(false, false),
  updatePosition(false, false),
  deletePosition(false, false),
  fetchPaymentReporting(false, true);

  private final boolean exposedByPU;
  private final boolean isSOAP;

  RegistryEventType(boolean exposedByPU, boolean isSOAP) {
    this.exposedByPU = exposedByPU;
    this.isSOAP = isSOAP;
  }
}
