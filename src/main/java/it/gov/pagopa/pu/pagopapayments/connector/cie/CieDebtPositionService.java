package it.gov.pagopa.pu.pagopapayments.connector.cie;

import it.gov.pagopa.pu.cie.dto.generated.DebtPositionCieRequestDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import org.apache.commons.lang3.tuple.Pair;

public interface CieDebtPositionService {
  Pair<DebtPositionDTO, String> createDebtPositionCie(DebtPositionCieRequestDTO debtPositionCieRequestDTO, String orgIpaCode);
}
