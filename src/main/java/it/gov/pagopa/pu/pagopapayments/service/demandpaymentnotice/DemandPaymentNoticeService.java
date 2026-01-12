package it.gov.pagopa.pu.pagopapayments.service.demandpaymentnotice;

import it.gov.pagopa.pagopa_api.pa.pafornode.PaDemandPaymentNoticeRequest;
import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.pagopapayments.connector.auth.AuthnService;
import it.gov.pagopa.pu.pagopapayments.connector.debtpositions.DebtPositionService;
import it.gov.pagopa.pu.pagopapayments.connector.organization.OrganizationService;
import it.gov.pagopa.pu.pagopapayments.connector.workflow.service.WorkflowService;
import it.gov.pagopa.pu.pagopapayments.enums.PagoPaNodeFaults;
import it.gov.pagopa.pu.pagopapayments.exception.PagoPaNodeFaultException;
import it.gov.pagopa.pu.pagopapayments.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@Slf4j
public class DemandPaymentNoticeService {

  private final DebtPositionService debtPositionService;
  private final OrganizationService organizationService;
  private final AuthnService authnService;
  private final WorkflowService workflowService;

  public DemandPaymentNoticeService(DebtPositionService debtPositionService, OrganizationService organizationService, AuthnService authnService, WorkflowService workflowService) {
    this.debtPositionService = debtPositionService;
    this.organizationService = organizationService;
    this.authnService = authnService;
    this.workflowService = workflowService;
  }

  public DebtPositionDTO handleRequest(PaDemandPaymentNoticeRequest request) {
    String accessToken = authnService.getAccessToken();
    Organization organization = organizationService.getOrganizationByFiscalCode(request.getIdPA(), accessToken);
    if(organization == null) {
      throw new PagoPaNodeFaultException(PagoPaNodeFaults.PAA_ID_DOMINIO_ERRATO, request.getIdPA());
    }
    Long orgId = organization.getOrganizationId();

    DebtPositionDTO debtPositionDTO = createDummyDebtPosition(orgId, accessToken);
    debtPositionDTO.setOrganizationId(orgId);
    debtPositionDTO.description("spontaneous psp for service "+request.getIdServizio());

    Pair<DebtPositionDTO, String> debtPositionWithWFId = debtPositionService.createDebtPosition(debtPositionDTO, accessToken);
    return syncDebtPosition(accessToken, debtPositionWithWFId);
  }

  private DebtPositionDTO createDummyDebtPosition(Long organizationId, String accessToken){
    DebtPositionDTO dp = new DebtPositionDTO();
    dp.status(DebtPositionStatus.UNPAID);
    dp.debtPositionOrigin(DebtPositionOrigin.SPONTANEOUS_PSP);

    dp.flagPuPagoPaPayment(true);
    dp.multiDebtor(false);
    dp.setCreationDate(OffsetDateTime.now());
    DebtPositionTypeOrg debtPositionTypeOrg = debtPositionService.findDebtPositionTypeOrgByOrgIdAndCode(organizationId, Constants.SPONTANEOUS_PSP_DP_TYPE_ORG_CODE, accessToken);
    if(debtPositionTypeOrg == null) {
      throw new PagoPaNodeFaultException(PagoPaNodeFaults.PAA_SYSTEM_ERROR, "DebptPositionTypeOrg with code "+Constants.SPONTANEOUS_PSP_DP_TYPE_ORG_CODE+" not found");
    }
    dp.debtPositionTypeOrgId(debtPositionTypeOrg.getDebtPositionTypeOrgId()); // hardcoded sponstaneous psp

    PaymentOptionDTO paymentOption = new PaymentOptionDTO();
    paymentOption.paymentOptionType(PaymentOptionType.INSTALLMENTS);
    paymentOption.totalAmountCents(100L);
    paymentOption.setPaymentOptionIndex(1);

    InstallmentDTO installmentDTO = new InstallmentDTO();
    installmentDTO.setAmountCents(100L);
    installmentDTO.remittanceInformation("spontaneous psp debt position");

    PersonDTO personDTO = new PersonDTO();
    personDTO.setEntityType(PersonEntityType.F);
    personDTO.setFiscalCode("RSSMRA92A12B123A");
    personDTO.setFullName("Mario Rossi");
    installmentDTO.debtor(personDTO);

    paymentOption.setInstallments(List.of(installmentDTO));
    dp.setPaymentOptions(List.of(paymentOption));
    return dp;
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
