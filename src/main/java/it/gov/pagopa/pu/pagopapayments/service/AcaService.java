package it.gov.pagopa.pu.pagopapayments.service;

import it.gov.pagopa.nodo.paCreatePosition.dto.generated.NewDebtPositionRequest;
import it.gov.pagopa.pu.pagopapayments.connector.AcaClient;
import it.gov.pagopa.pu.pagopapayments.mapper.DebtPositionMapper;
import it.gov.pagopa.pu.pagopapayments.removeme.DebtPosition;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AcaService {

  private final AcaClient acaClient;

  private final DebtPositionMapper debtPositionMapper;

  public void create(DebtPosition debtPosition){
    NewDebtPositionRequest request = debtPositionMapper.mapToNewDebtPositionRequest(debtPosition);
    acaClient.paCreatePosition(debtPosition.getOrganizationId(), request);
  }

  public void update(DebtPosition debtPosition){
    //update makes use of the same paCreatePosition api (it internally implements an "upsert" logic)
    NewDebtPositionRequest request = debtPositionMapper.mapToNewDebtPositionRequest(debtPosition);
    acaClient.paCreatePosition(debtPosition.getOrganizationId(), request);
  }

  public void delete(DebtPosition debtPosition){
    //delete is defined calling the same paCreatePosition api, but having set amount=0
    debtPosition.setAmount(0);

    NewDebtPositionRequest request = debtPositionMapper.mapToNewDebtPositionRequest(debtPosition);
    acaClient.paCreatePosition(debtPosition.getOrganizationId(), request);
  }

}
