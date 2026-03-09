package it.gov.pagopa.pu.pagopapayments.connector.debtpositions;

import it.gov.pagopa.pu.debtpositions.dto.generated.SpontaneousForm;

public interface SpontaneousFormService {
  SpontaneousForm getSpontaneousForm(Long spontaneousFormId, String accessToken);
}
