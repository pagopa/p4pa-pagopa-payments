package it.gov.pagopa.pu.pagopapayments.connector.pu_sil.client;

import it.gov.pagopa.pu.pagopapayments.connector.pu_sil.config.PuSilApisHolder;
import it.gov.pagopa.pu.pagopapayments.exception.NotPayableSilActualizedAmountException;
import it.gov.pagopa.pu.pagopapayments.exception.common.RestInvokeConflictException;
import it.gov.pagopa.pu.pusil.dto.generated.ActualizationResultDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PuSilClient {

  private final PuSilApisHolder apisHolder;

  public PuSilClient(PuSilApisHolder apisHolder) {
    this.apisHolder = apisHolder;
  }

  public ActualizationResultDTO actualize(Long orgSilServiceId, String nav, String accessToken) {
    try {
      return apisHolder.getActualizationApi(accessToken).actualize(orgSilServiceId, nav);
    } catch (RestInvokeConflictException e) {
      throw new NotPayableSilActualizedAmountException("Error when invoke actualize", e);
    }
  }
}
