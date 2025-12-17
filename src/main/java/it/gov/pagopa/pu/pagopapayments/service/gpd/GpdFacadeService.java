package it.gov.pagopa.pu.pagopapayments.service.gpd;

import it.gov.pagopa.pu.organization.dto.generated.BrokerApiKeys;
import it.gov.pagopa.pu.pagopapayments.connector.pagopa.gpd.GpdService;
import it.gov.pagopa.pu.pagopapayments.mapper.GpdDebtPositionMapper;
import it.gov.pagopa.pu.pagopapayments.service.aca_gpd.AbstractPaymentPositionFacadeService;
import it.gov.pagopa.pu.pagopapayments.service.broker.BrokerRetrieverService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class GpdFacadeService extends AbstractPaymentPositionFacadeService {

  public GpdFacadeService(
    GpdService gpdService,
    GpdDebtPositionMapper gpdDebtPositionMapper,
    BrokerRetrieverService brokerRetrieverService
  ) {
    super(gpdService, gpdDebtPositionMapper, brokerRetrieverService, "GPD");
  }

  @Override
  protected String getApiKey(BrokerApiKeys keys) {
    return keys.getGpdKey();
  }
}
