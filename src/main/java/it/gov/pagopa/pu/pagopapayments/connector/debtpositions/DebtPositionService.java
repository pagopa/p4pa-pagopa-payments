package it.gov.pagopa.pu.pagopapayments.connector.debtpositions;

import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public interface DebtPositionService {
  DebtPositionTypeOrg getDebtPositionTypeOrgById(Long debtPositionTypeOrgId, String accessToken);
  List<InstallmentDTO> getInstallmentsByOrganizationIdAndNav(Long organizationId, String nav, List<DebtPositionOrigin> debtPositionOriginList, String accessToken);
  InstallmentDTO updateInstallmentNotificationFee(ActualizeAmountRequestDTO actualizeAmountRequestDTO, String accessToken);
  DebtPositionTypeOrg findDebtPositionTypeOrgByOrgIdAndNavAndOrigins(Long organizationId, String nav, List<DebtPositionOrigin> debtPositionOriginList, String accessToken);
  /**
   * Creates a new debt position.
   *
   * @param debtPositionDTO the DTO containing the details of the debt position to create
   * @param accessToken the access token for authentication
   * @return a pair containing the created DebtPositionDTO and the workflow ID from the response headers
   */
  Pair<DebtPositionDTO, String> createDebtPosition(DebtPositionDTO debtPositionDTO, String accessToken);
}
