package it.gov.pagopa.pu.pagopapayments.connector.pu_sil;

import it.gov.pagopa.pu.pagopapayments.connector.pu_sil.client.PuSilClient;
import it.gov.pagopa.pu.pusil.dto.generated.AmountUpdatesDTO;
import org.springframework.stereotype.Service;

@Service
public class PuSilServiceImpl implements PuSilService {

  private final PuSilClient puSilClient;

  public PuSilServiceImpl(PuSilClient puSilClient) {
    this.puSilClient = puSilClient;
  }

  @Override
  public AmountUpdatesDTO getAmountUpdates(Long orgSilServiceId, String nav, String accessToken) {
    return puSilClient.getAmountUpdates(orgSilServiceId, nav, accessToken);
  }
}
