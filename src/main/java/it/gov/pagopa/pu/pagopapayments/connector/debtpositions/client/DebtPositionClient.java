package it.gov.pagopa.pu.pagopapayments.connector.debtpositions.client;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionOrigin;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.UpdateInstallmentNotificationFeeRequest;
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

  public List<InstallmentDTO> getInstallmentsByOrganizationIdAndNav(
    Long organizationId, String nav, List<DebtPositionOrigin> debtPositionOriginList, String accessToken) {
    return debtPositionsApisHolder
      .getInstallmentApi(accessToken)
      .getInstallmentsByOrganizationIdAndNav(organizationId, nav, debtPositionOriginList);
  }

  public InstallmentDTO updateInstallmentNotificationFee(Long organizationId, String nav, Long newFeeCents, String accessToken) {
    UpdateInstallmentNotificationFeeRequest request = new UpdateInstallmentNotificationFeeRequest(organizationId, nav, newFeeCents);
    return debtPositionsApisHolder.getDebtPositionApi(accessToken).updateInstallmentNotificationFee(request);
  }

  public DebtPositionTypeOrg findDebtPositionTypeOrgByOrgIdAndNavAndOrigins(Long organizationId, String nav, List<DebtPositionOrigin> debtPositionOriginList, String accessToken) {
    try {
      return debtPositionsApisHolder.getDebtPositionTypeOrgSearchControllerApi(accessToken)
        .crudDebtPositionTypeOrgsFindDebtPositionTypeOrgByOrgIdAndNavAndOrigins(organizationId, nav, debtPositionOriginList);
    } catch (HttpClientErrorException.NotFound e) {
      log.info("Cannot find DeptPositionTypeOrg having orgId {}, nav {} and origins {}",
        organizationId, nav, debtPositionOriginList);
      return null;
    }
  }
}
