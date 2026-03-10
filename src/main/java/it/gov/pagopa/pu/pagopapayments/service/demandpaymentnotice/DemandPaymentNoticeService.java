package it.gov.pagopa.pu.pagopapayments.service.demandpaymentnotice;

import it.gov.pagopa.pagopa_api.pa.pafornode.PaDemandPaymentNoticeRequest;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.pagopapayments.connector.auth.AuthnService;
import it.gov.pagopa.pu.pagopapayments.connector.organization.OrganizationService;
import it.gov.pagopa.pu.pagopapayments.connector.workflow.service.WorkflowService;
import it.gov.pagopa.pu.pagopapayments.enums.PagoPaNodeFaults;
import it.gov.pagopa.pu.pagopapayments.exception.PagoPaNodeFaultException;
import it.gov.pagopa.pu.pagopapayments.service.debtposition.cie.CieDebtPositionFacadeService;
import it.gov.pagopa.pu.pagopapayments.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DemandPaymentNoticeService {

  public static final String CIE_SEGREGATION_CODE = "99";
  private final OrganizationService organizationService;
  private final AuthnService authnService;
  private final WorkflowService workflowService;
  private final CieDebtPositionFacadeService cieDebtPositionFacadeService;

  public DemandPaymentNoticeService(OrganizationService organizationService, AuthnService authnService, WorkflowService workflowService, CieDebtPositionFacadeService cieDebtPositionFacadeService) {
    this.organizationService = organizationService;
    this.authnService = authnService;
    this.workflowService = workflowService;
    this.cieDebtPositionFacadeService = cieDebtPositionFacadeService;
  }

  public DebtPositionDTO handleRequest(PaDemandPaymentNoticeRequest request) {
    String accessToken = authnService.getAccessToken();
    Organization organization = organizationService.getOrganizationByFiscalCode(request.getIdPA(), accessToken);
    if(organization == null) {
      throw new PagoPaNodeFaultException(PagoPaNodeFaults.PAA_ID_DOMINIO_ERRATO, request.getIdPA());
    }

    return switch (request.getIdServizio()) {
      case CIE_SEGREGATION_CODE -> handleCreateCieDebtPosition(request, organization, accessToken);
      default -> throw new PagoPaNodeFaultException(PagoPaNodeFaults.PAA_SYSTEM_ERROR, request.getIdServizio());
    };
  }

  private DebtPositionDTO handleCreateCieDebtPosition(PaDemandPaymentNoticeRequest request, Organization organization, String accessToken) {
    Pair<DebtPositionDTO, String> debtPositionWithWFId = cieDebtPositionFacadeService.createCieDebtPosition(request.getDatiSpecificiServizioRequest(), organization, accessToken);
    if(debtPositionWithWFId.getRight()!=null){
      return syncDebtPosition(accessToken, debtPositionWithWFId);
    }
    return debtPositionWithWFId.getLeft();
  }

  private DebtPositionDTO syncDebtPosition(String accessToken, Pair<DebtPositionDTO, String> debtPosition) {
    DebtPositionDTO dp = debtPosition.getLeft();
    String workflowId = debtPosition.getRight();

    log.debug("Waiting for workflow completion for debt position [{}] with workflowId: [{}]",
      dp.getDebtPositionId(), workflowId);

    //wait for the debt positions to be synced
    String result = workflowService.waitWorkflowCompletion(workflowId, 10, 1000, accessToken);

    //if debt positions failed to sync, return a fault response
    if (!Constants.WORKFLOW_STATUS_COMPLETED_VALUE.equals(result)) {
      log.error("Error syncing debt position: DebtPositionId: {}, WorkflowId: {}, Result: {}",
        dp.getDebtPositionId(), workflowId, result);

      throw new PagoPaNodeFaultException(PagoPaNodeFaults.PAA_SYSTEM_ERROR, "Synchronization error for debt position");
    }

    log.info("Workflow completed for debt position [{}] with workflowId: [{}] with result: [{}]",
      dp.getDebtPositionId(), workflowId, result);

    return dp;
  }
}
