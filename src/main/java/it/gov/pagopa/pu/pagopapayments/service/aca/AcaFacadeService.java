package it.gov.pagopa.pu.pagopapayments.service.aca;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionOrigin;
import it.gov.pagopa.pu.organization.dto.generated.BrokerApiKeys;
import it.gov.pagopa.pu.pagopapayments.connector.pagopa.gpd.GpdService;
import it.gov.pagopa.pu.pagopapayments.mapper.GpdDebtPositionMapper;
import it.gov.pagopa.pu.pagopapayments.service.aca_gpd.AbstractPaymentPositionFacadeService;
import it.gov.pagopa.pu.pagopapayments.service.broker.BrokerRetrieverService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@Slf4j
public class AcaFacadeService extends AbstractPaymentPositionFacadeService {

  private static final Set<DebtPositionOrigin> ACA_EXCLUDED_ORIGINS = Set.of(
    DebtPositionOrigin.SPONTANEOUS,
    DebtPositionOrigin.SPONTANEOUS_SIL,
    DebtPositionOrigin.SPONTANEOUS_MIXED
  );

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

  @Override
  public void sync(String iud, DebtPositionDTO debtPosition, String accessToken) {
    if (ACA_EXCLUDED_ORIGINS.contains(debtPosition.getDebtPositionOrigin())) {
      log.info("Skipping ACA sync for debtPosition [{}] having origin [{}]",
        debtPosition.getDebtPositionId(),
        debtPosition.getDebtPositionOrigin());
      return;
    }
    super.sync(iud, debtPosition, accessToken);
  }
}
