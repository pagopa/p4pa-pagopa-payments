package it.gov.pagopa.pu.pagopapayments.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
abstract public class PaForNodeDTO {
  private String idPA;
  private String idBrokerPA;
  private String idStation;
  private String fiscalCode;
  private String noticeNumber;
}
