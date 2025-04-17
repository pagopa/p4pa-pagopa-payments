package it.gov.pagopa.pu.pagopapayments.enums;

public enum GenerateNoticeTemplates {

  TEMPLATE_SINGLE_INSTALMENT("TemplateSingleInstalment", "Template per un avviso di pagamento contenente un pagamento unico, senza bollettino postale"),
  TEMPLATE_SINGLE_INSTALMENT_POSTE("TemplateSingleInstalmentPoste", "Template per un avviso di pagamento contenente un pagamento unico, contenente la sezione relativa al bollettino postale");

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
