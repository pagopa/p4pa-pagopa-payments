package it.gov.pagopa.pu.pagopapayments.connector.pu_sil.client;

import it.gov.pagopa.pu.pagopapayments.connector.pu_sil.config.PuSilApisHolder;
import it.gov.pagopa.pu.pusil.dto.generated.AmountUpdatesDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

@Service
@Slf4j
public class PuSilClient {

  private final PuSilApisHolder apisHolder;

  public PuSilClient(PuSilApisHolder apisHolder) {
    this.apisHolder = apisHolder;
  }

  public AmountUpdatesDTO getAmountUpdates(Long orgSilServiceId, String nav, String accessToken) {
    try {
      return apisHolder.getAmountUpdatesApi(accessToken).getAmountUpdates(orgSilServiceId, nav);
    } catch (HttpClientErrorException.NotFound e){
      log.info("Cannot find amount having orgSilServiceId {} and nav {}", orgSilServiceId, nav);
      return null;
    }
  }
}
