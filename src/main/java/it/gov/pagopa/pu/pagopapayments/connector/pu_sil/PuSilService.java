package it.gov.pagopa.pu.pagopapayments.connector.pu_sil;

import it.gov.pagopa.pu.pusil.dto.generated.ActualizationResultDTO;

public interface PuSilService {
  ActualizationResultDTO actualize(Long orgSilServiceId, String nav, String accessToken);
}
