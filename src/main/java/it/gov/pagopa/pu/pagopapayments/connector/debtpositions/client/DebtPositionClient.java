package it.gov.pagopa.pu.pagopapayments.connector.debtpositions.client;

import it.gov.pagopa.pu.debtpositions.dto.generated.ActualizeAmountRequestDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionOrigin;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.pagopapayments.connector.debtpositions.config.DebtPositionsApisHolder;
import it.gov.pagopa.pu.pagopapayments.enums.PagoPaNodeFaults;
import it.gov.pagopa.pu.pagopapayments.exception.PagoPaNodeFaultException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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

  public InstallmentDTO updateInstallmentNotificationFee(ActualizeAmountRequestDTO request, String accessToken) {
    try {
      return debtPositionsApisHolder.getDebtPositionApi(accessToken).updateInstallmentNotificationFee(request);
    } catch (HttpClientErrorException.NotFound e) {
      throw new PagoPaNodeFaultException(PagoPaNodeFaults.PAA_PAGAMENTO_SCONOSCIUTO, request.getNav());
    } catch (HttpClientErrorException.Conflict e) {
      throw new PagoPaNodeFaultException(PagoPaNodeFaults.PAA_PAGAMENTO_DUPLICATO, request.getNav());
    } catch (HttpClientErrorException ex) {
      if(ex.getStatusCode() == HttpStatus.PRECONDITION_FAILED) {
        throw new PagoPaNodeFaultException(PagoPaNodeFaults.PAA_PAGAMENTO_SCADUTO, request.getNav());
      }
      throw new PagoPaNodeFaultException(PagoPaNodeFaults.PAA_SYSTEM_ERROR, request.getNav());
    }
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
