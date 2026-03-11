package it.gov.pagopa.pu.pagopapayments.connector.cie;

import it.gov.pagopa.pu.cie.dto.generated.DebtPositionCieRequestDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.pagopapayments.connector.auth.AuthnService;
import it.gov.pagopa.pu.pagopapayments.connector.cie.client.CieDebtPositionClient;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class CieDebtPositionServiceImpl implements CieDebtPositionService {
  public static final String HEADER_X_WORKFLOW_ID = "x-workflow-id";
  private final CieDebtPositionClient cieDebtPositionClient;
  private final AuthnService authnService;

  public CieDebtPositionServiceImpl(CieDebtPositionClient cieDebtPositionClient, AuthnService authnService) {
    this.cieDebtPositionClient = cieDebtPositionClient;
    this.authnService = authnService;
  }

  @Override
  public Pair<DebtPositionDTO, String> createDebtPositionCie(DebtPositionCieRequestDTO debtPositionCieRequestDTO, String orgIpaCode) {
    ResponseEntity<DebtPositionDTO> responseEntity = cieDebtPositionClient.createDebtPositionCie(debtPositionCieRequestDTO, authnService.getAccessToken(orgIpaCode));
    return Pair.of(responseEntity.getBody(), responseEntity.getHeaders().getFirst(HEADER_X_WORKFLOW_ID));
  }
}
