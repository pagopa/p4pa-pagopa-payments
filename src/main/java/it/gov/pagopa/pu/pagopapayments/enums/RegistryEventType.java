package it.gov.pagopa.pu.pagopapayments.enums;

public enum RegistryEventType {
  paVerifyPaymentNotice(true, true),
  paGetPaymentV2(true, true),
  paSendRTV2(true, true),
  newDebtPosition(false, false),
  createPosition(false, false),
  updatePosition(false, false),
  deletePosition(false, false),
  fetchPaymentReporting(false, true),
  paaSILChiediStatoImportFlusso(true, true),
  paaSILAutorizzaImportFlusso(true, true),
  paaSILImportaDovuto(true, true),
  paaSILInviaDovuti(true, true),
  paaSILInviaCarrelloDovuti(true, true),
  paaSILPrenotaExportFlusso(true, true),
  pivotSILChiediStatoImportFlussoTesoreria(true, true),
  pivotSILChiediStatoImportFlusso(true, true),
  pivotSILAutorizzaImportFlussoTesoreria(true, true),
  pivotSILAutorizzaImportFlusso(true, true);

  private final boolean exposedByPU;
  private final boolean isSOAP;

  RegistryEventType(boolean exposedByPU, boolean isSOAP) {
    this.exposedByPU = exposedByPU;
    this.isSOAP = isSOAP;
  }

  public boolean isExposedByPU() {
    return exposedByPU;
  }
}
