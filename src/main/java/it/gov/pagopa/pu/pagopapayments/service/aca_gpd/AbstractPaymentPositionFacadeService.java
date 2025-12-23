package it.gov.pagopa.pu.pagopapayments.service.aca_gpd;

import it.gov.pagopa.nodo.gpd.dto.generated.PaymentPositionModelV3;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.organization.dto.generated.BrokerApiKeys;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.pagopapayments.connector.pagopa.gpd.GpdService;
import it.gov.pagopa.pu.pagopapayments.dto.BrokerForNodoPaDTO;
import it.gov.pagopa.pu.pagopapayments.enums.Operation;
import it.gov.pagopa.pu.pagopapayments.mapper.GpdDebtPositionMapper;
import it.gov.pagopa.pu.pagopapayments.service.broker.BrokerRetrieverService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

@Slf4j
public abstract class AbstractPaymentPositionFacadeService {

  protected final GpdService gpdService;
  protected final GpdDebtPositionMapper gpdDebtPositionMapper;
  protected final BrokerRetrieverService brokerRetrieverService;
  private final String serviceName;

  protected AbstractPaymentPositionFacadeService(
    GpdService gpdService,
    GpdDebtPositionMapper gpdDebtPositionMapper,
    BrokerRetrieverService brokerRetrieverService,
    String serviceName
  ) {
    this.gpdService = gpdService;
    this.gpdDebtPositionMapper = gpdDebtPositionMapper;
    this.brokerRetrieverService = brokerRetrieverService;
    this.serviceName = serviceName;
  }

  protected abstract String getApiKey(BrokerApiKeys keys);

  public void sync(String iud, DebtPositionDTO debtPosition, String accessToken) {
    invokeCreatePositionImpl(iud, debtPosition, accessToken);
  }

  private void invokeCreatePositionImpl(String iud, DebtPositionDTO debtPositionDTO, String accessToken) {
    BrokerForNodoPaDTO brokerForNodoPaDTO = brokerRetrieverService.getBrokerForNodoPaDTOByOrganizationId(debtPositionDTO.getOrganizationId(), accessToken);
    Organization organization = brokerForNodoPaDTO.getOrganization();

    Pair<Operation, PaymentPositionModelV3> debtPositionToSendGPD = gpdDebtPositionMapper.mapToNewPaymentPositionModel(iud, debtPositionDTO, organization);

    PaymentPositionModelV3 newPaymentPositionModel = debtPositionToSendGPD.getRight();
    Operation operation = debtPositionToSendGPD.getLeft();

    log.info("invoking {} with operation [{}] for installment[{}/{}]",
      serviceName, operation.name(), newPaymentPositionModel.getPaymentOption().getFirst().getInstallments().getFirst().getIuv(), iud);

    String apiKey = getApiKey(brokerForNodoPaDTO.getBrokerApiKeys());
    String orgFiscalCode = organization.getOrgFiscalCode();

    switch (operation) {
      case Operation.DELETE:
        gpdService.paDeletePosition(apiKey, orgFiscalCode, newPaymentPositionModel.getIupd(), newPaymentPositionModel);
        break;
      case Operation.UPDATE:
        gpdService.paUpdatePosition(apiKey, orgFiscalCode, newPaymentPositionModel.getIupd(), newPaymentPositionModel);
        break;
      case Operation.CREATE:
        gpdService.paCreatePosition(apiKey, orgFiscalCode, newPaymentPositionModel);
        break;
    }
  }
}
