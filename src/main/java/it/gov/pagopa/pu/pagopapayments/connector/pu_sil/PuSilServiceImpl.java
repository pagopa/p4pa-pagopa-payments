package it.gov.pagopa.pu.pagopapayments.connector.pu_sil;

import it.gov.pagopa.pu.pagopapayments.config.CacheConfig;
import it.gov.pagopa.pu.pagopapayments.connector.pu_sil.client.PuSilClient;
import it.gov.pagopa.pu.pusil.dto.generated.ActualizationResultDTO;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class PuSilServiceImpl implements PuSilService {

  private final PuSilClient puSilClient;

  public PuSilServiceImpl(PuSilClient puSilClient) {
    this.puSilClient = puSilClient;
  }

  @Override
  @Cacheable(cacheNames = CacheConfig.Fields.notificationFee, key = "'sil-' + #orgSilServiceId + '-' + #nav", unless="#result == null")
  public ActualizationResultDTO actualize(Long orgSilServiceId, String nav, String accessToken) {
    return puSilClient.actualize(orgSilServiceId, nav, accessToken);
  }
}
