package it.gov.pagopa.pu.pagopapayments.service.aca;

import it.gov.pagopa.nodo.pacreateposition.dto.generated.NewDebtPositionRequest;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.organization.dto.generated.BrokerApiKeys;
import it.gov.pagopa.pu.pagopapayments.connector.pagopa.aca.client.AcaClient;
import it.gov.pagopa.pu.pagopapayments.mapper.AcaDebtPositionMapper;
import it.gov.pagopa.pu.pagopapayments.service.broker.BrokerService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AcaService {

  private final AcaClient acaClient;
  private final AcaDebtPositionMapper acaDebtPositionMapper;
  private final BrokerService brokerService;

  public AcaService(AcaClient acaClient, AcaDebtPositionMapper acaDebtPositionMapper, BrokerService brokerService) {
    this.acaClient = acaClient;
    this.acaDebtPositionMapper = acaDebtPositionMapper;
    this.brokerService = brokerService;
  }

  public void sync(String iud, DebtPositionDTO debtPosition, String accessToken) {
    invokePaCreatePositionImpl(iud, debtPosition, accessToken);
  }

  private void invokePaCreatePositionImpl(String iud, DebtPositionDTO debtPosition, String accessToken) {
    Pair<AcaDebtPositionMapper.OPERATION, NewDebtPositionRequest> debtPostionToSendACA = acaDebtPositionMapper.mapToNewDebtPositionRequest(iud, debtPosition);
    Pair<BrokerApiKeys, String> brokerData = brokerService.getBrokerApiKeyAndSegregationCodesByOrganizationId(debtPosition.getOrganizationId(), accessToken);

    NewDebtPositionRequest newDebtPositionRequest = debtPostionToSendACA.getRight();
    AcaDebtPositionMapper.OPERATION operation = debtPostionToSendACA.getLeft();
    if (operation == AcaDebtPositionMapper.OPERATION.DELETE) {
      //delete is defined calling the same paCreatePosition api, but having set amount=0
      newDebtPositionRequest.amount(0);
    }
    log.info("invoking ACA paCreatePosition for installment[{}/{}], operation[{}]",
      newDebtPositionRequest.getEntityFiscalCode(), newDebtPositionRequest.getIuv(), operation);
    acaClient.paCreatePosition(newDebtPositionRequest, brokerData.getLeft().getAcaKey(), brokerData.getRight());
  }

}
