package it.gov.pagopa.pu.pagopapayments.service.aca;

import it.gov.pagopa.nodo.paCreatePosition.dto.generated.NewDebtPositionRequest;
import it.gov.pagopa.pu.p4pa_organization.dto.generated.BrokerApiKeys;
import it.gov.pagopa.pu.pagopapayments.connector.AcaClient;
import it.gov.pagopa.pu.pagopapayments.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.pagopapayments.mapper.AcaDebtPositionMapper;
import it.gov.pagopa.pu.pagopapayments.service.broker.BrokerService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class AcaService {

  private enum OPERATION { CREATE, UPDATE, DELETE }

  public static final String STATUS_INSTALLMENT_TO_SYNCH = "TO_SYNCH";
  public static final String STATUS_INSTALLMENT_UNPAID = "UNPAID";

  public static final Set<String> STATUS_TO_SEND_ACA = Set.of(STATUS_INSTALLMENT_TO_SYNCH);

  private final AcaClient acaClient;
  private final AcaDebtPositionMapper acaDebtPositionMapper;
  private final BrokerService brokerService;

  public AcaService(AcaClient acaClient, AcaDebtPositionMapper acaDebtPositionMapper, BrokerService brokerService) {
    this.acaClient = acaClient;
    this.acaDebtPositionMapper = acaDebtPositionMapper;
    this.brokerService = brokerService;
  }

  public void create(DebtPositionDTO debtPosition) {
    invokePaCreatePositionImpl(debtPosition, OPERATION.CREATE);
  }

  public void update(DebtPositionDTO debtPosition) {
    invokePaCreatePositionImpl(debtPosition, OPERATION.UPDATE);
  }

  public void delete(DebtPositionDTO debtPosition) {
    invokePaCreatePositionImpl(debtPosition, OPERATION.DELETE);
  }

  private void invokePaCreatePositionImpl(DebtPositionDTO debtPosition, OPERATION operation) {
    List<NewDebtPositionRequest> debtPostionToSendACA = acaDebtPositionMapper.mapToNewDebtPositionRequest(debtPosition, STATUS_TO_SEND_ACA);
    Pair<BrokerApiKeys, String> brokerData = brokerService.getBrokerApiKeyAndSegregationCodesByOrganizationId(debtPosition.getOrganizationId());
    debtPostionToSendACA.forEach(newDebtPositionRequest -> {
      if (operation == OPERATION.DELETE) {
        //delete is defined calling the same paCreatePosition api, but having set amount=0
        newDebtPositionRequest.amount(0);
      }
      log.info("invoking ACA paCreatePosition for installment[{}/{}], operation[{}]",
        newDebtPositionRequest.getEntityFiscalCode(), newDebtPositionRequest.getIuv(), operation);
      acaClient.paCreatePosition(newDebtPositionRequest, brokerData.getLeft().getAcaKey(), brokerData.getRight());
    });
  }

}
