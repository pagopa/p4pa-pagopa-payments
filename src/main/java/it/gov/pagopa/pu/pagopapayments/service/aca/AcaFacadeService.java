package it.gov.pagopa.pu.pagopapayments.service.aca;

import it.gov.pagopa.nodo.pacreateposition.dto.generated.NewDebtPositionRequest;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionOrigin;
import it.gov.pagopa.pu.organization.dto.generated.BrokerApiKeys;
import it.gov.pagopa.pu.pagopapayments.connector.pagopa.aca.AcaService;
import it.gov.pagopa.pu.pagopapayments.mapper.AcaDebtPositionMapper;
import it.gov.pagopa.pu.pagopapayments.service.broker.BrokerRetrieverService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@Slf4j
public class AcaFacadeService {

  private final AcaService acaService;
  private final AcaDebtPositionMapper acaDebtPositionMapper;
  private final BrokerRetrieverService brokerRetrieverService;
  private static final Set<DebtPositionOrigin> ACA_EXCLUDED_ORIGINS = Set.of(
    DebtPositionOrigin.SPONTANEOUS,
    DebtPositionOrigin.SPONTANEOUS_SIL,
    DebtPositionOrigin.SPONTANEOUS_MIXED);

  public AcaFacadeService(
    AcaService acaService,
    AcaDebtPositionMapper acaDebtPositionMapper,
    BrokerRetrieverService brokerRetrieverService
  ) {
    this.acaService = acaService;
    this.acaDebtPositionMapper = acaDebtPositionMapper;
    this.brokerRetrieverService = brokerRetrieverService;
  }

  public void sync(String iud, DebtPositionDTO debtPosition, String accessToken) {
    if (ACA_EXCLUDED_ORIGINS.contains(debtPosition.getDebtPositionOrigin())) {
      log.info("Skipping ACA sync for debtPosition [{}] having origin [{}]",
        debtPosition.getDebtPositionId(),
        debtPosition.getDebtPositionOrigin());
      return;
    }

    invokePaCreatePositionImpl(iud, debtPosition, accessToken);
  }

  private void invokePaCreatePositionImpl(String iud, DebtPositionDTO debtPosition, String accessToken) {
    Pair<AcaDebtPositionMapper.OPERATION, NewDebtPositionRequest> debtPositionToSendACA = acaDebtPositionMapper.mapToNewDebtPositionRequest(iud, debtPosition);
    Pair<BrokerApiKeys, String> brokerData = brokerRetrieverService.getBrokerApiKeyAndSegregationCodesByOrganizationId(debtPosition.getOrganizationId(), accessToken);

    NewDebtPositionRequest newDebtPositionRequest = debtPositionToSendACA.getRight();
    AcaDebtPositionMapper.OPERATION operation = debtPositionToSendACA.getLeft();
    if (operation == AcaDebtPositionMapper.OPERATION.DELETE) {
      //delete is defined calling the same paCreatePosition api, but having set amount=0
      newDebtPositionRequest.amount(0);
    }
    log.info("invoking ACA paCreatePosition for installment[{}/{}], operation[{}]",
      newDebtPositionRequest.getEntityFiscalCode(), newDebtPositionRequest.getIuv(), operation);
    acaService.paCreatePosition(newDebtPositionRequest, brokerData.getLeft().getAcaKey(), brokerData.getRight());
  }

}
