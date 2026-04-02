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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DemandPaymentNoticeService {

  private final OrganizationService organizationService;
  private final AuthnService authnService;
  private final WorkflowService workflowService;
  private final CieDebtPositionFacadeService cieDebtPositionFacadeService;
  private final String cieServiceId;

  public DemandPaymentNoticeService(OrganizationService organizationService, AuthnService authnService, WorkflowService workflowService,
                                    CieDebtPositionFacadeService cieDebtPositionFacadeService, @Value("${cie.service-id}") String cieServiceId) {
    this.organizationService = organizationService;
    this.authnService = authnService;
    this.workflowService = workflowService;
    this.cieDebtPositionFacadeService = cieDebtPositionFacadeService;
    this.cieServiceId = cieServiceId;
  }

  public DebtPositionDTO handleRequest(PaDemandPaymentNoticeRequest request) {
    String accessToken = authnService.getAccessToken();
    Organization organization = organizationService.getOrganizationByFiscalCode(request.getIdPA(), accessToken);
    if(organization == null) {
      throw new PagoPaNodeFaultException(PagoPaNodeFaults.PAA_ID_DOMINIO_ERRATO, request.getIdPA());
    }

    String serviceId = request.getIdServizio();
    if (cieServiceId.equals(serviceId)) {
      return handleCreateCieDebtPosition(request, organization, accessToken);
    } else {
      throw new PagoPaNodeFaultException(PagoPaNodeFaults.PAA_SYSTEM_ERROR, "There is no implementation for serviceId " + serviceId);
    }
  }

  private DebtPositionDTO handleCreateCieDebtPosition(PaDemandPaymentNoticeRequest request, Organization organization, String accessToken) {
    Pair<DebtPositionDTO, String> debtPositionWithWFId = cieDebtPositionFacadeService.createCieDebtPosition(request.getDatiSpecificiServizioRequest(), organization, accessToken);
    if(debtPositionWithWFId.getRight()!=null){
      return awaitSyncCompletion(accessToken, debtPositionWithWFId);
    }
    return debtPositionWithWFId.getLeft();
  }

  private DebtPositionDTO awaitSyncCompletion(String accessToken, Pair<DebtPositionDTO, String> debtPosition) {
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

    log.debug("Workflow completed for debt position [{}] with workflowId: [{}] with result: [{}]",
      dp.getDebtPositionId(), workflowId, result);

    return dp;
  }
}
