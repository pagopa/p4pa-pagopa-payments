package it.gov.pagopa.pu.pagopapayments.enums;

public enum GenerateNoticeTemplates {

  TEMPLATE_SINGLE_INSTALMENT("TemplateSingleInstalment", "Template per un avviso di pagamento contenente un pagamento unico, senza bollettino postale"),
  TEMPLATE_SINGLE_INSTALMENT_POSTE("TemplateSingleInstalmentPoste", "Template per un avviso di pagamento contenente un pagamento unico, contenente la sezione relativa al bollettino postale"),
  TEMPLATE_INSTALMENTS("TemplateInstalments", "Template per un avviso di pagamento contenente un pagamento con rate, in numero inferiore o uguale a 9, senza bollettino postale"),
  TEMPLATE_INSTALMENTS_POSTE("TemplateInstalmentsPoste", "Template per un avviso di pagamento contenente un pagamento con rate, in numero inferiore o uguale a 9, contenente la sezione relativa al bollettino postale. In questo caso devono essere disponibili per l'ente di riferimento gli estremi per il bollettino postale (numero di c/c, autorizzazione alla stampa)"),
  TEMPLATE_MANY_INSTALMENTS("TemplateManyInstalments", "Template per un avviso di pagamento contenente un pagamento con rate, in numero superiore o uguale a 9");

  private final String templateId;
  private final String description;

  GenerateNoticeTemplates(String templateId, String description) {
    this.templateId = templateId;
    this.description = description;
  }

  public String description() {
    return this.description;
  }

  public String templateId() {
    return this.templateId;
  }

  public String code() {
    return this.name();
  }
}
