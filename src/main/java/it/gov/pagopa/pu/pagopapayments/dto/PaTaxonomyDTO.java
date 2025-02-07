package it.gov.pagopa.pu.pagopapayments.dto;

import java.io.Serializable;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldNameConstants;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants(asEnum = true)
@JsonIgnoreProperties("id")
public class PaTaxonomyDTO implements Serializable {

  @FieldNameConstants.Exclude
  private Long id;
  @JsonProperty("CODICE TIPO ENTE CREDITORE")
  private String codiceTipoEnte;
  @JsonProperty("TIPO ENTE CREDITORE")
  private String descrizioneTipoEnte;
  @JsonProperty("PROGRESSIVO MACRO AREA PER ENTE CREDITORE")
  private String progressivoMacroArea;
  @JsonProperty("NOME MACRO AREA")
  private String nomeMacroArea;
  @JsonProperty("DESCRIZIONE MACRO AREA")
  private String descricioneMacroArea;
  @JsonProperty("CODICE TIPOLOGIA SERVIZIO")
  private String codiceTipoServizio;
  @JsonProperty("TIPO SERVIZIO")
  private String tipoServizio;
  @JsonProperty("MOTIVO GIURIDICO DELLA RISCOSSIONE")
  private String motivoRiscossione;
  @JsonProperty("DESCRIZIONE TIPO SERVIZIO")
  private String descrizioneTipoServizio;
  @JsonProperty("VERSIONE TASSONOMIA")
  private String versioneTassonomia;
  @JsonProperty("DATI SPECIFICI INCASSO")
  private String datiSpecificiIncasso;
  @JsonProperty("DATA INIZIO VALIDITA")
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
  private Date dataInizioValidita;
  @JsonProperty("DATA FINE VALIDITA")
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
  private Date dataFineValidita;

  public enum Fields {
    ;// This is necessary!
    public String fieldName() {
      return  name();
    }
  }
}
