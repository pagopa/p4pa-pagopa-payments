package it.gov.pagopa.pu.pagopapayments.connector.pu_sil;

import it.gov.pagopa.pu.pusil.dto.generated.AmountUpdatesDTO;

public interface PuSilService {
  AmountUpdatesDTO getAmountUpdates(Long orgSilServiceId, String nav, String accessToken);
}
