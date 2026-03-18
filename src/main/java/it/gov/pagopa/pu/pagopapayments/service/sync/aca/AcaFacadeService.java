package it.gov.pagopa.pu.pagopapayments.service.sync.aca;

import it.gov.pagopa.pu.aca.gpd.v1.dto.generated.PaymentPositionModel;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.organization.dto.generated.BrokerApiKeys;
import it.gov.pagopa.pu.pagopapayments.connector.pagopa.aca.AcaService;
import it.gov.pagopa.pu.pagopapayments.enums.Operation;
import it.gov.pagopa.pu.pagopapayments.mapper.AcaDebtPositionMapper;
import it.gov.pagopa.pu.pagopapayments.service.broker.BrokerRetrieverService;
import it.gov.pagopa.pu.pagopapayments.service.sync.BaseSyncOperationService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import static it.gov.pagopa.pu.pagopapayments.util.DebtPositionUtils.ACA_EXCLUDED_ORIGINS;

@Service
@Slf4j
public class AcaFacadeService extends BaseSyncOperationService<PaymentPositionModel> {

  private final AcaService acaService;
  private final AcaDebtPositionMapper acaDebtPositionMapper;

  public AcaFacadeService(AcaService acaService, AcaDebtPositionMapper acaDebtPositionMapper,
                          BrokerRetrieverService brokerRetrieverService) {
    super(brokerRetrieverService);
    this.acaService = acaService;
    this.acaDebtPositionMapper = acaDebtPositionMapper;
  }

  @Override
  public void sync(String iud, DebtPositionDTO debtPosition, String accessToken) {
    if (ACA_EXCLUDED_ORIGINS.contains(debtPosition.getDebtPositionOrigin())) {
      log.info("Skipping ACA sync for debtPosition [{}] having origin [{}]",
        debtPosition.getDebtPositionId(), debtPosition.getDebtPositionOrigin());
      return;
    }
    invokeCreatePositionImpl(iud, debtPosition, accessToken);
  }

  @Override protected String getServiceName() { return "ACA"; }

  @Override protected String getApiKey(BrokerApiKeys keys) { return keys.getAcaKey(); }

  @Override
  protected Pair<Operation, PaymentPositionModel> mapToPaymentPositionModel(String iud, DebtPositionDTO dto, String orgName) {
    return acaDebtPositionMapper.mapToNewPaymentPositionModel(iud, dto, orgName);
  }

  @Override
  protected String extractIuv(PaymentPositionModel model) {
    return model.getPaymentOption().getFirst().getIuv();
  }

  @Override
  protected void executeOperation(Operation operation, String apiKey, String orgFiscalCode, PaymentPositionModel model) {
    switch (operation) {
      case DELETE:
        acaService.paDeletePosition(apiKey, orgFiscalCode, model.getIupd(), model);
        break;
      case UPDATE:
        acaService.paUpdatePosition(apiKey, orgFiscalCode, model.getIupd(), model);
        break;
      case CREATE:
        acaService.paCreatePosition(apiKey, orgFiscalCode, model);
        break;
    }
  }
}
