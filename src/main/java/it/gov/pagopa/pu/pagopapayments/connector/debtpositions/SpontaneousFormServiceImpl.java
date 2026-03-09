package it.gov.pagopa.pu.pagopapayments.connector.debtpositions;

import it.gov.pagopa.pu.debtpositions.dto.generated.SpontaneousForm;
import it.gov.pagopa.pu.pagopapayments.connector.debtpositions.client.SpontaneousFormClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SpontaneousFormServiceImpl implements SpontaneousFormService {

  private final SpontaneousFormClient spontaneousFormClient;

  public SpontaneousFormServiceImpl(SpontaneousFormClient spontaneousFormClient) {
    this.spontaneousFormClient = spontaneousFormClient;
  }

  @Override
  public SpontaneousForm getSpontaneousForm(Long spontaneousFormId, String accessToken){
    return spontaneousFormClient.getSpontaneousForm(spontaneousFormId, accessToken);
  }
}
