package it.gov.pagopa.pu.pagopapayments.service.gpd;

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
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class GpdFacadeService {
  private final GpdService gpdService;
  private final GpdDebtPositionMapper gpdDebtPositionMapper;
  private final BrokerRetrieverService brokerRetrieverService;

  private static final String SERVICE_NAME = "GPD";

  public GpdFacadeService(GpdService gpdService, GpdDebtPositionMapper gpdDebtPositionMapper, BrokerRetrieverService brokerRetrieverService) {
    this.gpdService = gpdService;
    this.gpdDebtPositionMapper = gpdDebtPositionMapper;
    this.brokerRetrieverService = brokerRetrieverService;
  }

  public void sync(String iud, DebtPositionDTO debtPosition, String accessToken) {
    invokeCreatePositionImpl(iud, debtPosition, accessToken);
  }

  private void invokeCreatePositionImpl(String iud, DebtPositionDTO debtPositionDTO, String accessToken) {
    BrokerForNodoPaDTO brokerForNodoPaDTO =
      brokerRetrieverService.getBrokerForNodoPaDTOByOrganizationId(debtPositionDTO.getOrganizationId(), accessToken);

    Organization organization = brokerForNodoPaDTO.getOrganization();

    Pair<Operation, PaymentPositionModelV3> debtPositionToSendGPD =
      gpdDebtPositionMapper.mapToNewPaymentPositionModel(iud, debtPositionDTO, organization);

    PaymentPositionModelV3 newPaymentPositionModel = debtPositionToSendGPD.getRight();
    Operation operation = debtPositionToSendGPD.getLeft();

    log.info("invoking {} with operation [{}] for installment[{}/{}]",
      SERVICE_NAME,
      operation.name(),
      newPaymentPositionModel.getPaymentOption().getFirst().getInstallments().getFirst().getIuv(),
      iud
    );

    String apiKey = getApiKey(brokerForNodoPaDTO.getBrokerApiKeys());
    String orgFiscalCode = organization.getOrgFiscalCode();

    switch (operation) {
      case DELETE:
        gpdService.paDeletePosition(apiKey, orgFiscalCode, newPaymentPositionModel.getIupd(), newPaymentPositionModel);
        break;
      case UPDATE:
        gpdService.paUpdatePosition(apiKey, orgFiscalCode, newPaymentPositionModel.getIupd(), newPaymentPositionModel);
        break;
      case CREATE:
        gpdService.paCreatePosition(apiKey, orgFiscalCode, newPaymentPositionModel);
        break;
    }
  }

  private String getApiKey(BrokerApiKeys keys) {
    return keys.getGpdKey();
  }
}
