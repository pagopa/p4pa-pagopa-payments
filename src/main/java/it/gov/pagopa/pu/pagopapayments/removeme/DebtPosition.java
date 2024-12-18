package it.gov.pagopa.pu.pagopapayments.removeme;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DebtPosition {
  private String nav;
  private Integer amount;
  private Long organizationId;
}
