package it.gov.pagopa.pu.pagopapayments.service.acagpdsync.gpd;

import it.gov.pagopa.nodo.gpd.dto.generated.PaymentPositionModelV3;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.organization.dto.generated.BrokerApiKeys;
import it.gov.pagopa.pu.pagopapayments.connector.pagopa.gpd.GpdService;
import it.gov.pagopa.pu.pagopapayments.enums.Operation;
import it.gov.pagopa.pu.pagopapayments.mapper.GpdDebtPositionMapper;
import it.gov.pagopa.pu.pagopapayments.service.acagpdsync.BaseSyncOperationService;
import it.gov.pagopa.pu.pagopapayments.service.broker.BrokerRetrieverService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class GpdFacadeService extends BaseSyncOperationService<PaymentPositionModelV3> {

  private final GpdService gpdService;
  private final GpdDebtPositionMapper gpdDebtPositionMapper;

  public GpdFacadeService(GpdService gpdService, GpdDebtPositionMapper gpdDebtPositionMapper,
                          BrokerRetrieverService brokerRetrieverService) {
    super(brokerRetrieverService);
    this.gpdService = gpdService;
    this.gpdDebtPositionMapper = gpdDebtPositionMapper;
  }

  @Override
  public void sync(String iud, DebtPositionDTO debtPosition, String accessToken) {
    invokeCreatePositionImpl(iud, debtPosition, accessToken);
  }

  @Override protected String getServiceName() { return "GPD"; }

  @Override protected String getApiKey(BrokerApiKeys keys) { return keys.getGpdKey(); }

  @Override
  protected Pair<Operation, PaymentPositionModelV3> mapToPaymentPositionModel(String iud, DebtPositionDTO dto, String orgName) {
    return gpdDebtPositionMapper.mapToNewPaymentPositionModel(iud, dto, orgName);
  }

  @Override
  protected String extractIuv(PaymentPositionModelV3 model) {
    return model.getPaymentOption().getFirst().getInstallments().getFirst().getIuv();
  }

  @Override
  protected void executeOperation(Operation operation, String apiKey, String orgFiscalCode, PaymentPositionModelV3 model) {
    switch (operation) {
      case DELETE:
        gpdService.paDeletePosition(apiKey, orgFiscalCode, model.getIupd(), model);
        break;
      case UPDATE:
        gpdService.paUpdatePosition(apiKey, orgFiscalCode, model.getIupd(), model);
        break;
      case CREATE:
        gpdService.paCreatePosition(apiKey, orgFiscalCode, model);
        break;
    }
  }
}
