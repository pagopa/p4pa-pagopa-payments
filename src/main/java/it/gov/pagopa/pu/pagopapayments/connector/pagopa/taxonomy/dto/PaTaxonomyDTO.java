package it.gov.pagopa.pu.pagopapayments.connector.pagopa.taxonomy.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import it.gov.pagopa.pu.pagopapayments.config.json.DateDeserializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.Date;

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
  @JsonDeserialize(using = DateDeserializer.class)
  private Date dataInizioValidita;
  @JsonProperty("DATA FINE VALIDITA")
  @JsonDeserialize(using = DateDeserializer.class)
  private Date dataFineValidita;

}
