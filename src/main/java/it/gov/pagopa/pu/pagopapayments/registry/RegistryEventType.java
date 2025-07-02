package it.gov.pagopa.pu.pagopapayments.registry;

import lombok.Getter;

@Getter
@SuppressWarnings("java:S115") // Suppressing constant naming warning: this is required to match with the api name
public enum RegistryEventType {
  PaForNode_paVerifyPaymentNotice(true),
  PaForNode_paGetPaymentV2(true),
  PaForNode_paSendRTV2(true),

  ACA_newDebtPosition(false),

  GPD_createPosition(false),
  GPD_updatePosition(false),
  GPD_deletePosition(false),

  NodeForPa_fetchPaymentReporting(false);

  private final boolean exposedByPU;

  RegistryEventType(boolean exposedByPU) {
    this.exposedByPU = exposedByPU;
  }
}
