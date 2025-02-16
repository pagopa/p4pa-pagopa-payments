package it.gov.pagopa.pu.pagopapayments.connector.debtpositions.client;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.pagopapayments.connector.debtpositions.config.DebtPositionsApisHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;

@Service
@Slf4j
public class DebtPositionClient {

  private final DebtPositionsApisHolder debtPositionsApisHolder;

  public DebtPositionClient(DebtPositionsApisHolder debtPositionsApisHolder) {
    this.debtPositionsApisHolder = debtPositionsApisHolder;
  }

  public DebtPositionTypeOrg getDebtPositionTypeOrgById(Long debtPositionTypeOrgId, String accessToken) {
    try{
      return debtPositionsApisHolder
        .getDebtPositionTypeOrgEntityControllerApi(accessToken)
        .crudGetDebtpositiontypeorg(String.valueOf(debtPositionTypeOrgId));
    } catch (HttpClientErrorException.NotFound e){
      log.info("Cannot find DeptPositionTypeOrg having id {}", debtPositionTypeOrgId);
      return null;
    }
  }

  public List<InstallmentDTO> getDebtPositionsByOrganizationIdAndNav(Long organizationId, String nav, String accessToken) {
    return List.of(); //TODO blocked by P4ADEV-1779
  }
}
