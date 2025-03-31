package it.gov.pagopa.pu.pagopapayments.enums;

public enum PagoPaNodeFaults {

  PAA_SYSTEM_ERROR("PAA_SYSTEM_ERROR"),
  PAA_ID_DOMINIO_ERRATO("PAA_ID_DOMINIO_ERRATO"),
  PAA_ID_INTERMEDIARIO_ERRATO("PAA_ID_INTERMEDIARIO_ERRATO"),
  PAA_STAZIONE_INT_ERRATA("PAA_STAZIONE_INT_ERRATA"),
  PAA_SEMANTICA("PAA_SEMANTICA"),
  PAA_PAGAMENTO_SCONOSCIUTO("PAA_PAGAMENTO_SCONOSCIUTO"),
  PAA_PAGAMENTO_DUPLICATO("PAA_PAGAMENTO_DUPLICATO"),
  PAA_PAGAMENTO_ANNULLATO("PAA_PAGAMENTO_ANNULLATO"),
  PAA_PAGAMENTO_SCADUTO("PAA_PAGAMENTO_SCADUTO"),
    ;


  private final String description;

  private PagoPaNodeFaults(String description) {
    this.description = description;
  }

  public String description() {
    return this.description;
  }

  public String code(){
    return this.name();
  }
}
