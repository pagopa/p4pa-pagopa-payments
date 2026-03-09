package it.gov.pagopa.pu.pagopapayments.service.demandpaymentnotice;

import it.gov.pagopa.pagopa_api.pa.pafornode.PaDemandPaymentNoticeRequest;
import it.gov.pagopa.pu.cie.dto.generated.DebtPositionCieOriginAllowedEnum;
import it.gov.pagopa.pu.cie.dto.generated.DebtPositionCieRequestDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.pagopapayments.connector.auth.AuthnService;
import it.gov.pagopa.pu.pagopapayments.connector.cie.CieDebtPositionService;
import it.gov.pagopa.pu.pagopapayments.connector.debtpositions.DebtPositionService;
import it.gov.pagopa.pu.pagopapayments.connector.debtpositions.SpontaneousFormService;
import it.gov.pagopa.pu.pagopapayments.connector.organization.OrganizationService;
import it.gov.pagopa.pu.pagopapayments.connector.workflow.service.WorkflowService;
import it.gov.pagopa.pu.pagopapayments.enums.PagoPaNodeFaults;
import it.gov.pagopa.pu.pagopapayments.exception.PagoPaNodeFaultException;
import it.gov.pagopa.pu.pagopapayments.service.JAXBTransformService;
import it.gov.pagopa.pu.pagopapayments.util.Constants;
import it.gov.spcoop.puntoaccessopsp.pagamentocie.PagamentoCIE;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class DemandPaymentNoticeService {

  public static final String CIE_SEGREGATION_CODE = "99";
  public static final String SPONTANEOUS_FIELD_NAME = "sys_type";
  private final DebtPositionService debtPositionService;
  private final OrganizationService organizationService;
  private final AuthnService authnService;
  private final WorkflowService workflowService;
  private final JAXBTransformService  jaxbTransformService;
  private final CieDebtPositionService cieDebtPositionService;
  private final SpontaneousFormService spontaneousFormService;

  public DemandPaymentNoticeService(DebtPositionService debtPositionService, OrganizationService organizationService, AuthnService authnService, WorkflowService workflowService, JAXBTransformService jaxbTransformService, CieDebtPositionService cieDebtPositionService, SpontaneousFormService spontaneousFormService) {
    this.debtPositionService = debtPositionService;
    this.organizationService = organizationService;
    this.authnService = authnService;
    this.workflowService = workflowService;
    this.jaxbTransformService = jaxbTransformService;
    this.cieDebtPositionService = cieDebtPositionService;
    this.spontaneousFormService = spontaneousFormService;
  }

  public DebtPositionDTO handleRequest(PaDemandPaymentNoticeRequest request) {
    String accessToken = authnService.getAccessToken();
    Organization organization = organizationService.getOrganizationByFiscalCode(request.getIdPA(), accessToken);
    if(organization == null) {
      throw new PagoPaNodeFaultException(PagoPaNodeFaults.PAA_ID_DOMINIO_ERRATO, request.getIdPA());
    }
    Long orgId = organization.getOrganizationId();

    Pair<DebtPositionDTO, String> debtPositionWithWFId;
    if(CIE_SEGREGATION_CODE.equals(request.getIdServizio())){
      return handleCreateCieDebtPosition(request, orgId, organization, accessToken);
    }else {
      DebtPositionDTO debtPositionDTO = createDummyDebtPosition(orgId, accessToken);
      debtPositionDTO.setOrganizationId(orgId);
      debtPositionDTO.description("spontaneous psp for service " + request.getIdServizio());

      debtPositionWithWFId = debtPositionService.createDebtPosition(debtPositionDTO, accessToken);
    }
    return syncDebtPosition(accessToken, debtPositionWithWFId);
  }

  private DebtPositionDTO handleCreateCieDebtPosition(PaDemandPaymentNoticeRequest request, Long orgId, Organization organization, String accessToken) {
    DebtPositionCieRequestDTO debtPositionCieRequestDTO = buildDebtPositionCieRequestDTO(
      orgId,
      jaxbTransformService.unmarshalling(request.getDatiSpecificiServizioRequest(), PagamentoCIE.class),
      accessToken);
    Pair<DebtPositionDTO, String> debtPositionWithWFId = cieDebtPositionService.createDebtPositionCie(
      debtPositionCieRequestDTO,
      organization.getIpaCode());
    if(debtPositionWithWFId.getRight()!=null){
      return syncDebtPosition(accessToken, debtPositionWithWFId);
    }
    return debtPositionWithWFId.getLeft();
  }

  private  DebtPositionCieRequestDTO buildDebtPositionCieRequestDTO(Long organizationId, PagamentoCIE pagamentoCIE, String accessToken) {
    DebtPositionCieRequestDTO debtPositionCieRequestDTO = new DebtPositionCieRequestDTO();
    debtPositionCieRequestDTO.setOrigin(DebtPositionCieOriginAllowedEnum.SPONTANEOUS_PSP);
    DebtPositionTypeOrg debtPositionTypeOrg = getDebtPositionTypeOrg(organizationId, pagamentoCIE.getCodiceCausale(), accessToken);
    debtPositionCieRequestDTO.setDebtPositionTypeOrgCode(debtPositionTypeOrg.getCode());
    debtPositionCieRequestDTO.setOrgFiscalCode(pagamentoCIE.getCodiceFiscaleComune());
    debtPositionCieRequestDTO.setRemittanceInformation(buildRemittanceInformation(debtPositionTypeOrg,accessToken));
    debtPositionCieRequestDTO.setDebtor(
      PersonDTO.builder()
        .entityType(PersonEntityType.F)
        .fiscalCode(pagamentoCIE.getIntestatario().getCodiceFiscaleIntestatario())
        .fullName(pagamentoCIE.getIntestatario().getDenominazioneIntestatario())
        .build());
    return debtPositionCieRequestDTO;
  }

  private String buildRemittanceInformation(DebtPositionTypeOrg debtPositionTypeOrg, String accessToken) {
    if (debtPositionTypeOrg.getSpontaneousFormId() == null) {
      return handleFallbackRemittance(debtPositionTypeOrg);
    }

    SpontaneousForm spontaneousForm = spontaneousFormService.getSpontaneousForm(debtPositionTypeOrg.getSpontaneousFormId(), accessToken);
    if (spontaneousForm == null) {
      return handleFallbackRemittance(debtPositionTypeOrg);
    }

    return Optional.ofNullable(spontaneousForm.getStructure().getFields())
      .flatMap(fields -> fields.stream()
        .filter(f -> SPONTANEOUS_FIELD_NAME.equals(f.getName()))
        .findFirst())
      .map(SpontaneousFormField::getDefaultValue)
      .orElseGet(() -> handleFallbackRemittance(debtPositionTypeOrg));
  }

  private String handleFallbackRemittance(DebtPositionTypeOrg debtPositionTypeOrg) {
    log.warn("DebtPositionTypeOrg having debtPositionTypeOrgId {} and spontaneousFormId {} has no default remittance information", debtPositionTypeOrg.getDebtPositionTypeOrgId(), debtPositionTypeOrg.getSpontaneousFormId());
    return debtPositionTypeOrg.getCode() + " " + debtPositionTypeOrg.getDescription();
  }

  private DebtPositionDTO createDummyDebtPosition(Long organizationId, String accessToken){
    DebtPositionDTO dp = new DebtPositionDTO();
    dp.status(DebtPositionStatus.UNPAID);
    dp.debtPositionOrigin(DebtPositionOrigin.SPONTANEOUS_PSP);

    dp.flagPuPagoPaPayment(true);
    dp.multiDebtor(false);
    dp.setCreationDate(OffsetDateTime.now());
    DebtPositionTypeOrg debtPositionTypeOrg = getDebtPositionTypeOrg(organizationId, Constants.SPONTANEOUS_PSP_DP_TYPE_ORG_CODE, accessToken);
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

  private DebtPositionTypeOrg getDebtPositionTypeOrg(Long organizationId, String debtPositionTypeOrgCode, String accessToken) {
    DebtPositionTypeOrg debtPositionTypeOrg = debtPositionService.findDebtPositionTypeOrgByOrgIdAndCode(organizationId, debtPositionTypeOrgCode, accessToken);
    if(debtPositionTypeOrg == null) {
      throw new PagoPaNodeFaultException(PagoPaNodeFaults.PAA_SYSTEM_ERROR, "DebtPositionTypeOrg with code "+debtPositionTypeOrgCode+" not found");
    }
    return debtPositionTypeOrg;
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
