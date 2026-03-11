package it.gov.pagopa.pu.pagopapayments.connector.cie.client;

import it.gov.pagopa.pu.cie.dto.generated.DebtPositionCieRequestDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.pagopapayments.connector.cie.config.CieApisHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CieDebtPositionClient {

  private final CieApisHolder cieApisHolder;

  public CieDebtPositionClient(CieApisHolder cieApisHolder) {
    this.cieApisHolder = cieApisHolder;
  }

  public ResponseEntity<DebtPositionDTO> createDebtPositionCie(DebtPositionCieRequestDTO debtPositionCieRequestDTO, String accessToken) {
    return cieApisHolder.getDebtPositionCieApi(accessToken).createDebtPositionCieWithHttpInfo(debtPositionCieRequestDTO);
  }
}
