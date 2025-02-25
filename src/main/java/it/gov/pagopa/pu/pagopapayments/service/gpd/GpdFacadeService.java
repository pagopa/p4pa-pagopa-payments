package it.gov.pagopa.pu.pagopapayments.service.gpd;

import it.gov.pagopa.nodo.gpd.dto.generated.PaymentPositionModel;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.organization.dto.generated.BrokerApiKeys;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.pagopapayments.connector.pagopa.gpd.GpdService;
import it.gov.pagopa.pu.pagopapayments.dto.BrokerForNodoPaDTO;
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

  public GpdFacadeService(
    GpdService gpdService,
    GpdDebtPositionMapper gpdDebtPositionMapper,
    BrokerRetrieverService brokerRetrieverService
  ) {
    this.gpdService = gpdService;
    this.gpdDebtPositionMapper = gpdDebtPositionMapper;
    this.brokerRetrieverService = brokerRetrieverService;
  }

  public void sync(String iud, DebtPositionDTO debtPosition, String accessToken) {
    invokeCreatePositionImpl(iud, debtPosition, accessToken);
  }

  private void invokeCreatePositionImpl(String iud, DebtPositionDTO debtPositionDTO, String accessToken) {
    BrokerForNodoPaDTO brokerForNodoPaDTO = brokerRetrieverService.getBrokerForNodoPaDTOByOrganizationId(debtPositionDTO.getOrganizationId(), accessToken);
    Organization organization = brokerForNodoPaDTO.getOrganization();
    Pair<GpdDebtPositionMapper.OPERATION, PaymentPositionModel> debtPostionToSendGPD = gpdDebtPositionMapper.mapToNewPaymentPositionModel(iud, debtPositionDTO,organization);


    PaymentPositionModel newPaymentPositionModel = debtPostionToSendGPD.getRight();
    GpdDebtPositionMapper.OPERATION operation = debtPostionToSendGPD.getLeft();
    if (operation == GpdDebtPositionMapper.OPERATION.DELETE) {
      log.info("invoking GPD paDeletePosition for installment[{}/{}]",
        newPaymentPositionModel.getFiscalCode(), newPaymentPositionModel.getPaymentOption().getFirst().getIuv());
      gpdService.paDeletePosition(brokerForNodoPaDTO.getBrokerApiKeys().getGpdKey(), organization.getOrgFiscalCode(), newPaymentPositionModel.getIupd());
    }
    if (operation == GpdDebtPositionMapper.OPERATION.CREATE) {
      log.info("invoking GPD paCreatePosition for installment[{}/{}]",
        newPaymentPositionModel.getFiscalCode(), newPaymentPositionModel.getPaymentOption().getFirst().getIuv());
      gpdService.paCreatePosition(brokerForNodoPaDTO.getBrokerApiKeys().getGpdKey(), organization.getOrgFiscalCode(), newPaymentPositionModel);
    }
    if (operation == GpdDebtPositionMapper.OPERATION.UPDATE) {
      log.info("invoking GPD paUpdatePosition for installment[{}/{}]",
        newPaymentPositionModel.getFiscalCode(), newPaymentPositionModel.getPaymentOption().getFirst().getIuv());
      gpdService.paUpdatePosition(brokerForNodoPaDTO.getBrokerApiKeys().getGpdKey(), organization.getOrgFiscalCode(), newPaymentPositionModel.getIupd(),newPaymentPositionModel);
    }
  }

}
