package it.gov.pagopa.pu.pagopapayments.connector.debtpositions.client;

import it.gov.pagopa.pu.debtpositions.dto.generated.SpontaneousForm;
import it.gov.pagopa.pu.pagopapayments.connector.debtpositions.config.DebtPositionsApisHolder;
import it.gov.pagopa.pu.pagopapayments.exception.common.RestInvokeNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SpontaneousFormClient {

  private final DebtPositionsApisHolder debtPositionsApisHolder;

  public SpontaneousFormClient(DebtPositionsApisHolder debtPositionsApisHolder) {
    this.debtPositionsApisHolder = debtPositionsApisHolder;
  }

  public SpontaneousForm getSpontaneousForm(Long spontaneousFormId, String accessToken) {
    try {
      return debtPositionsApisHolder
        .getSpontaneousFormEntityControllerApi(accessToken)
        .crudGetSpontaneousform(String.valueOf(spontaneousFormId));
    } catch (RestInvokeNotFoundException e) {
      log.info("Cannot find SpontaneousForm having id {}", spontaneousFormId);
      return null;
    }
  }
}
