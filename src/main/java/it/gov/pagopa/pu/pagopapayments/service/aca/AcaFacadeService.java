package it.gov.pagopa.pu.pagopapayments.service.aca;

import it.gov.pagopa.pu.organization.dto.generated.BrokerApiKeys;
import it.gov.pagopa.pu.pagopapayments.connector.pagopa.gpd.GpdService;
import it.gov.pagopa.pu.pagopapayments.mapper.GpdDebtPositionMapper;
import it.gov.pagopa.pu.pagopapayments.service.aca_gpd.AbstractPaymentPositionFacadeService;
import it.gov.pagopa.pu.pagopapayments.service.broker.BrokerRetrieverService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AcaFacadeService extends AbstractPaymentPositionFacadeService {

  public AcaFacadeService(
    @Qualifier("acaServiceWrapper") GpdService acaServiceWrapper,
    GpdDebtPositionMapper gpdDebtPositionMapper,
    BrokerRetrieverService brokerRetrieverService
  ) {
    super(acaServiceWrapper, gpdDebtPositionMapper, brokerRetrieverService, "ACA");
  }

  @Override
  protected String getApiKey(BrokerApiKeys keys) {
    return keys.getAcaKey();
  }
}
