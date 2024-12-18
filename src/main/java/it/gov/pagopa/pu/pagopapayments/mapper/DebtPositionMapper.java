package it.gov.pagopa.pu.pagopapayments.mapper;

import it.gov.pagopa.nodo.paCreatePosition.dto.generated.NewDebtPositionRequest;
import it.gov.pagopa.pu.pagopapayments.removeme.DebtPosition;
import org.springframework.stereotype.Component;

@Component
public class DebtPositionMapper {

  public NewDebtPositionRequest mapToNewDebtPositionRequest(DebtPosition debtPosition) {
    return new NewDebtPositionRequest()
      .nav(debtPosition.getNav())
      .amount(debtPosition.getAmount());
  }
}
