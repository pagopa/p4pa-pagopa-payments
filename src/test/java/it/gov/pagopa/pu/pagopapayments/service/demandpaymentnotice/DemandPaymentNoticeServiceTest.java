package it.gov.pagopa.pu.pagopapayments.service.demandpaymentnotice;

import it.gov.pagopa.pagopa_api.pa.pafornode.PaDemandPaymentNoticeRequest;
import it.gov.pagopa.pu.cie.dto.generated.DebtPositionCieOriginAllowedEnum;
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
import it.gov.pagopa.pu.pagopapayments.util.TestUtils;
import it.gov.spcoop.puntoaccessopsp.pagamentocie.PagamentoCIE;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.jemos.podam.api.PodamFactory;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DemandPaymentNoticeServiceTest {

  @Mock
  private DebtPositionService debtPositionServiceMock;
  @Mock
  private OrganizationService organizationServiceMock;
  @Mock
  private AuthnService authnServiceMock;
  @Mock
  private WorkflowService workflowServiceMock;
  @Mock
  private JAXBTransformService jaxbTransformServiceMock;
  @Mock
  private CieDebtPositionService cieDebtPositionServiceMock;
  @Mock
  private SpontaneousFormService spontaneousFormServiceMock;
  @InjectMocks
  private DemandPaymentNoticeService demandPaymentNoticeService;

  private final PodamFactory podamFactory;

  private static final String ACCESS_TOKEN = "access-token";

  public DemandPaymentNoticeServiceTest() {
    podamFactory = TestUtils.getPodamFactory();
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      debtPositionServiceMock,
      organizationServiceMock,
      authnServiceMock,
      workflowServiceMock,
      jaxbTransformServiceMock,
      cieDebtPositionServiceMock,
      spontaneousFormServiceMock
    );
  }

  @Test
  void givenValidRequestWhenHandleRequestThenSuccess() {
    // Given
    PaDemandPaymentNoticeRequest request = podamFactory.manufacturePojo(PaDemandPaymentNoticeRequest.class);
    Organization organization = podamFactory.manufacturePojo(Organization.class);
    DebtPositionDTO createdDebtPosition = podamFactory.manufacturePojo(DebtPositionDTO.class);
    DebtPositionTypeOrg debtPositionTypeOrg = podamFactory.manufacturePojo(DebtPositionTypeOrg.class);
    debtPositionTypeOrg.setCode(Constants.SPONTANEOUS_PSP_DP_TYPE_ORG_CODE);
    String workflowId = "wf-123";

    when(authnServiceMock.getAccessToken()).thenReturn(ACCESS_TOKEN);
    when(organizationServiceMock.getOrganizationByFiscalCode(request.getIdPA(), ACCESS_TOKEN))
      .thenReturn(organization);

    when(debtPositionServiceMock.findDebtPositionTypeOrgByOrgIdAndCode(organization.getOrganizationId(),
      Constants.SPONTANEOUS_PSP_DP_TYPE_ORG_CODE, ACCESS_TOKEN))
      .thenReturn(debtPositionTypeOrg);

    when(debtPositionServiceMock.createDebtPosition(any(DebtPositionDTO.class), eq(ACCESS_TOKEN)))
      .thenReturn(Pair.of(createdDebtPosition, workflowId));

    when(workflowServiceMock.waitWorkflowCompletion(workflowId, 10, 1000, ACCESS_TOKEN))
      .thenReturn(Constants.WORKFLOW_STATUS_COMPLETED_VALUE);

    // When
    DebtPositionDTO result = demandPaymentNoticeService.handleRequest(request);

    // Then
    assertNotNull(result);
    assertEquals(createdDebtPosition.getDebtPositionId(), result.getDebtPositionId());

    verify(authnServiceMock).getAccessToken();
    verify(organizationServiceMock).getOrganizationByFiscalCode(request.getIdPA(), ACCESS_TOKEN);
    verify(debtPositionServiceMock).createDebtPosition(any(DebtPositionDTO.class), eq(ACCESS_TOKEN));
    verify(workflowServiceMock).waitWorkflowCompletion(workflowId, 10, 1000, ACCESS_TOKEN);
  }

  @Test
  void givenCieServiceIdWhenHandleRequestThenSuccess() {
    // Given
    PaDemandPaymentNoticeRequest request = podamFactory.manufacturePojo(PaDemandPaymentNoticeRequest.class);
    request.setIdServizio(DemandPaymentNoticeService.CIE_SEGREGATION_CODE);
    Organization organization = podamFactory.manufacturePojo(Organization.class);
    DebtPositionDTO createdDebtPosition = podamFactory.manufacturePojo(DebtPositionDTO.class);
    DebtPositionTypeOrg debtPositionTypeOrg = podamFactory.manufacturePojo(DebtPositionTypeOrg.class);
    debtPositionTypeOrg.setCode(Constants.SPONTANEOUS_PSP_DP_TYPE_ORG_CODE);
    PagamentoCIE pagamentoCIE = podamFactory.manufacturePojo(PagamentoCIE.class);
    SpontaneousForm spontaneousForm = podamFactory.manufacturePojo(SpontaneousForm.class);
    SpontaneousFormField spontaneousFormField = podamFactory.manufacturePojo(SpontaneousFormField.class);
    spontaneousFormField.setName("sys_type");
    spontaneousForm.getStructure().setFields(List.of(spontaneousFormField));
    String workflowId = "wf-123";

    when(authnServiceMock.getAccessToken()).thenReturn(ACCESS_TOKEN);
    when(organizationServiceMock.getOrganizationByFiscalCode(request.getIdPA(), ACCESS_TOKEN))
      .thenReturn(organization);

    when(jaxbTransformServiceMock.unmarshalling(request.getDatiSpecificiServizioRequest(), PagamentoCIE.class))
      .thenReturn(pagamentoCIE);
    when(debtPositionServiceMock.findDebtPositionTypeOrgByOrgIdAndCode(organization.getOrganizationId(),
      pagamentoCIE.getCodiceCausale(), ACCESS_TOKEN))
      .thenReturn(debtPositionTypeOrg);
    when(spontaneousFormServiceMock.getSpontaneousForm(debtPositionTypeOrg.getSpontaneousFormId(),ACCESS_TOKEN))
      .thenReturn(spontaneousForm);

    when(cieDebtPositionServiceMock.createDebtPositionCie(argThat(dp->
        DebtPositionCieOriginAllowedEnum.SPONTANEOUS_PSP.equals(dp.getOrigin())
        && debtPositionTypeOrg.getCode().equals(dp.getDebtPositionTypeOrgCode())
        && pagamentoCIE.getCodiceFiscaleComune().equals(dp.getOrgFiscalCode())
        && spontaneousFormField.getDefaultValue().equals(dp.getRemittanceInformation())
        && PersonEntityType.F.equals(dp.getDebtor().getEntityType())
        && pagamentoCIE.getIntestatario().getCodiceFiscaleIntestatario().equals(dp.getDebtor().getFiscalCode())
        && pagamentoCIE.getIntestatario().getDenominazioneIntestatario().equals(dp.getDebtor().getFullName())
      ), eq(organization.getIpaCode())))
      .thenReturn(Pair.of(createdDebtPosition, workflowId));

    when(workflowServiceMock.waitWorkflowCompletion(workflowId, 10, 1000, ACCESS_TOKEN))
      .thenReturn(Constants.WORKFLOW_STATUS_COMPLETED_VALUE);

    // When
    DebtPositionDTO result = demandPaymentNoticeService.handleRequest(request);

    // Then
    assertNotNull(result);
    assertEquals(createdDebtPosition.getDebtPositionId(), result.getDebtPositionId());
  }

  @Test
  void givenNoMatchingFieldNameWhenHandleRequestThenFallbackRemittanceInformation() {
    // Given
    PaDemandPaymentNoticeRequest request = podamFactory.manufacturePojo(PaDemandPaymentNoticeRequest.class);
    request.setIdServizio(DemandPaymentNoticeService.CIE_SEGREGATION_CODE);
    Organization organization = podamFactory.manufacturePojo(Organization.class);
    DebtPositionDTO createdDebtPosition = podamFactory.manufacturePojo(DebtPositionDTO.class);
    DebtPositionTypeOrg debtPositionTypeOrg = podamFactory.manufacturePojo(DebtPositionTypeOrg.class);
    debtPositionTypeOrg.setCode(Constants.SPONTANEOUS_PSP_DP_TYPE_ORG_CODE);
    PagamentoCIE pagamentoCIE = podamFactory.manufacturePojo(PagamentoCIE.class);
    SpontaneousForm spontaneousForm = podamFactory.manufacturePojo(SpontaneousForm.class);
    SpontaneousFormField spontaneousFormField = podamFactory.manufacturePojo(SpontaneousFormField.class);
    spontaneousFormField.setName("wrong_sys_type");
    spontaneousForm.getStructure().setFields(List.of(spontaneousFormField));
    String workflowId = "wf-123";

    when(authnServiceMock.getAccessToken()).thenReturn(ACCESS_TOKEN);
    when(organizationServiceMock.getOrganizationByFiscalCode(request.getIdPA(), ACCESS_TOKEN))
      .thenReturn(organization);

    when(jaxbTransformServiceMock.unmarshalling(request.getDatiSpecificiServizioRequest(), PagamentoCIE.class))
      .thenReturn(pagamentoCIE);
    when(debtPositionServiceMock.findDebtPositionTypeOrgByOrgIdAndCode(organization.getOrganizationId(),
      pagamentoCIE.getCodiceCausale(), ACCESS_TOKEN))
      .thenReturn(debtPositionTypeOrg);
    when(spontaneousFormServiceMock.getSpontaneousForm(debtPositionTypeOrg.getSpontaneousFormId(),ACCESS_TOKEN))
      .thenReturn(spontaneousForm);

    when(cieDebtPositionServiceMock.createDebtPositionCie(argThat(dp->
        DebtPositionCieOriginAllowedEnum.SPONTANEOUS_PSP.equals(dp.getOrigin())
        && debtPositionTypeOrg.getCode().equals(dp.getDebtPositionTypeOrgCode())
        && pagamentoCIE.getCodiceFiscaleComune().equals(dp.getOrgFiscalCode())
        && buildFallbackRemittanceInformation(debtPositionTypeOrg).equals(dp.getRemittanceInformation())
        && PersonEntityType.F.equals(dp.getDebtor().getEntityType())
        && pagamentoCIE.getIntestatario().getCodiceFiscaleIntestatario().equals(dp.getDebtor().getFiscalCode())
        && pagamentoCIE.getIntestatario().getDenominazioneIntestatario().equals(dp.getDebtor().getFullName())
      ), eq(organization.getIpaCode())))
      .thenReturn(Pair.of(createdDebtPosition, workflowId));

    when(workflowServiceMock.waitWorkflowCompletion(workflowId, 10, 1000, ACCESS_TOKEN))
      .thenReturn(Constants.WORKFLOW_STATUS_COMPLETED_VALUE);

    // When
    DebtPositionDTO result = demandPaymentNoticeService.handleRequest(request);

    // Then
    assertNotNull(result);
    assertEquals(createdDebtPosition.getDebtPositionId(), result.getDebtPositionId());
  }

  @Test
  void givenNoSpontaneousFormWhenHandleRequestThenFallbackRemittanceInformation() {
    // Given
    PaDemandPaymentNoticeRequest request = podamFactory.manufacturePojo(PaDemandPaymentNoticeRequest.class);
    request.setIdServizio(DemandPaymentNoticeService.CIE_SEGREGATION_CODE);
    Organization organization = podamFactory.manufacturePojo(Organization.class);
    DebtPositionDTO createdDebtPosition = podamFactory.manufacturePojo(DebtPositionDTO.class);
    DebtPositionTypeOrg debtPositionTypeOrg = podamFactory.manufacturePojo(DebtPositionTypeOrg.class);
    debtPositionTypeOrg.setCode(Constants.SPONTANEOUS_PSP_DP_TYPE_ORG_CODE);
    PagamentoCIE pagamentoCIE = podamFactory.manufacturePojo(PagamentoCIE.class);
    String workflowId = "wf-123";

    when(authnServiceMock.getAccessToken()).thenReturn(ACCESS_TOKEN);
    when(organizationServiceMock.getOrganizationByFiscalCode(request.getIdPA(), ACCESS_TOKEN))
      .thenReturn(organization);

    when(jaxbTransformServiceMock.unmarshalling(request.getDatiSpecificiServizioRequest(), PagamentoCIE.class))
      .thenReturn(pagamentoCIE);
    when(debtPositionServiceMock.findDebtPositionTypeOrgByOrgIdAndCode(organization.getOrganizationId(),
      pagamentoCIE.getCodiceCausale(), ACCESS_TOKEN))
      .thenReturn(debtPositionTypeOrg);
    when(spontaneousFormServiceMock.getSpontaneousForm(debtPositionTypeOrg.getSpontaneousFormId(),ACCESS_TOKEN))
      .thenReturn(null);

    when(cieDebtPositionServiceMock.createDebtPositionCie(argThat(dp->
        DebtPositionCieOriginAllowedEnum.SPONTANEOUS_PSP.equals(dp.getOrigin())
        && debtPositionTypeOrg.getCode().equals(dp.getDebtPositionTypeOrgCode())
        && pagamentoCIE.getCodiceFiscaleComune().equals(dp.getOrgFiscalCode())
        && buildFallbackRemittanceInformation(debtPositionTypeOrg).equals(dp.getRemittanceInformation())
        && PersonEntityType.F.equals(dp.getDebtor().getEntityType())
        && pagamentoCIE.getIntestatario().getCodiceFiscaleIntestatario().equals(dp.getDebtor().getFiscalCode())
        && pagamentoCIE.getIntestatario().getDenominazioneIntestatario().equals(dp.getDebtor().getFullName())
      ), eq(organization.getIpaCode())))
      .thenReturn(Pair.of(createdDebtPosition, workflowId));

    when(workflowServiceMock.waitWorkflowCompletion(workflowId, 10, 1000, ACCESS_TOKEN))
      .thenReturn(Constants.WORKFLOW_STATUS_COMPLETED_VALUE);

    // When
    DebtPositionDTO result = demandPaymentNoticeService.handleRequest(request);

    // Then
    assertNotNull(result);
    assertEquals(createdDebtPosition.getDebtPositionId(), result.getDebtPositionId());
  }

  @Test
  void givenNoSpontaneousFormIdWhenHandleRequestThenFallbackRemittanceInformation() {
    // Given
    PaDemandPaymentNoticeRequest request = podamFactory.manufacturePojo(PaDemandPaymentNoticeRequest.class);
    request.setIdServizio(DemandPaymentNoticeService.CIE_SEGREGATION_CODE);
    Organization organization = podamFactory.manufacturePojo(Organization.class);
    DebtPositionDTO createdDebtPosition = podamFactory.manufacturePojo(DebtPositionDTO.class);
    DebtPositionTypeOrg debtPositionTypeOrg = podamFactory.manufacturePojo(DebtPositionTypeOrg.class);
    debtPositionTypeOrg.setCode(Constants.SPONTANEOUS_PSP_DP_TYPE_ORG_CODE);
    debtPositionTypeOrg.setSpontaneousFormId(null);
    PagamentoCIE pagamentoCIE = podamFactory.manufacturePojo(PagamentoCIE.class);
    String workflowId = "wf-123";

    when(authnServiceMock.getAccessToken()).thenReturn(ACCESS_TOKEN);
    when(organizationServiceMock.getOrganizationByFiscalCode(request.getIdPA(), ACCESS_TOKEN))
      .thenReturn(organization);

    when(jaxbTransformServiceMock.unmarshalling(request.getDatiSpecificiServizioRequest(), PagamentoCIE.class))
      .thenReturn(pagamentoCIE);
    when(debtPositionServiceMock.findDebtPositionTypeOrgByOrgIdAndCode(organization.getOrganizationId(),
      pagamentoCIE.getCodiceCausale(), ACCESS_TOKEN))
      .thenReturn(debtPositionTypeOrg);

    when(cieDebtPositionServiceMock.createDebtPositionCie(argThat(dp->
        DebtPositionCieOriginAllowedEnum.SPONTANEOUS_PSP.equals(dp.getOrigin())
        && debtPositionTypeOrg.getCode().equals(dp.getDebtPositionTypeOrgCode())
        && pagamentoCIE.getCodiceFiscaleComune().equals(dp.getOrgFiscalCode())
        && buildFallbackRemittanceInformation(debtPositionTypeOrg).equals(dp.getRemittanceInformation())
        && PersonEntityType.F.equals(dp.getDebtor().getEntityType())
        && pagamentoCIE.getIntestatario().getCodiceFiscaleIntestatario().equals(dp.getDebtor().getFiscalCode())
        && pagamentoCIE.getIntestatario().getDenominazioneIntestatario().equals(dp.getDebtor().getFullName())
      ), eq(organization.getIpaCode())))
      .thenReturn(Pair.of(createdDebtPosition, workflowId));

    when(workflowServiceMock.waitWorkflowCompletion(workflowId, 10, 1000, ACCESS_TOKEN))
      .thenReturn(Constants.WORKFLOW_STATUS_COMPLETED_VALUE);

    // When
    DebtPositionDTO result = demandPaymentNoticeService.handleRequest(request);

    // Then
    assertNotNull(result);
    assertEquals(createdDebtPosition.getDebtPositionId(), result.getDebtPositionId());
  }

  private String buildFallbackRemittanceInformation(DebtPositionTypeOrg debtPositionTypeOrg) {
    return debtPositionTypeOrg.getCode() + " " + debtPositionTypeOrg.getDescription();
  }

  @Test
  void givenNonExistentOrganizationWhenHandleRequestThenThrowException() {
    // Given
    PaDemandPaymentNoticeRequest request = podamFactory.manufacturePojo(PaDemandPaymentNoticeRequest.class);

    when(authnServiceMock.getAccessToken()).thenReturn(ACCESS_TOKEN);
    when(organizationServiceMock.getOrganizationByFiscalCode(request.getIdPA(), ACCESS_TOKEN))
      .thenReturn(null);

    // When & Then
    PagoPaNodeFaultException exception = assertThrows(PagoPaNodeFaultException.class,
      () -> demandPaymentNoticeService.handleRequest(request));

    assertEquals(PagoPaNodeFaults.PAA_ID_DOMINIO_ERRATO, exception.getErrorCode());
  }

  @Test
  void givenNonExistentDebtPositionTypeOrgWhenHandleRequestThenThrowException() {
    // Given
    PaDemandPaymentNoticeRequest request = podamFactory.manufacturePojo(PaDemandPaymentNoticeRequest.class);
    Organization organization = podamFactory.manufacturePojo(Organization.class);

    when(authnServiceMock.getAccessToken()).thenReturn(ACCESS_TOKEN);
    when(organizationServiceMock.getOrganizationByFiscalCode(request.getIdPA(), ACCESS_TOKEN))
      .thenReturn(organization);

    when(debtPositionServiceMock.findDebtPositionTypeOrgByOrgIdAndCode(organization.getOrganizationId(),
      Constants.SPONTANEOUS_PSP_DP_TYPE_ORG_CODE, ACCESS_TOKEN))
      .thenReturn(null);

    // When & Then
    PagoPaNodeFaultException exception = assertThrows(PagoPaNodeFaultException.class,
      () -> demandPaymentNoticeService.handleRequest(request));

    assertEquals(PagoPaNodeFaults.PAA_SYSTEM_ERROR, exception.getErrorCode());
  }

  @Test
  void givenWorkflowSyncErrorWhenHandleRequestThenThrowException() {
    // Given
    PaDemandPaymentNoticeRequest request = podamFactory.manufacturePojo(PaDemandPaymentNoticeRequest.class);
    Organization organization = podamFactory.manufacturePojo(Organization.class);
    DebtPositionDTO createdDebtPosition = podamFactory.manufacturePojo(DebtPositionDTO.class);
    DebtPositionTypeOrg debtPositionTypeOrg = podamFactory.manufacturePojo(DebtPositionTypeOrg.class);
    debtPositionTypeOrg.setCode(Constants.SPONTANEOUS_PSP_DP_TYPE_ORG_CODE);
    String workflowId = "wf-failed";

    when(authnServiceMock.getAccessToken()).thenReturn(ACCESS_TOKEN);
    when(organizationServiceMock.getOrganizationByFiscalCode(request.getIdPA(), ACCESS_TOKEN))
      .thenReturn(organization);
    when(debtPositionServiceMock.findDebtPositionTypeOrgByOrgIdAndCode(organization.getOrganizationId(),
      Constants.SPONTANEOUS_PSP_DP_TYPE_ORG_CODE, ACCESS_TOKEN))
      .thenReturn(debtPositionTypeOrg);
    when(debtPositionServiceMock.createDebtPosition(any(DebtPositionDTO.class), eq(ACCESS_TOKEN)))
      .thenReturn(Pair.of(createdDebtPosition, workflowId));

    when(workflowServiceMock.waitWorkflowCompletion(workflowId, 10, 1000, ACCESS_TOKEN))
      .thenReturn("FAILED");

    // When & Then
    PagoPaNodeFaultException exception = assertThrows(PagoPaNodeFaultException.class,
      () -> demandPaymentNoticeService.handleRequest(request));

    assertEquals(PagoPaNodeFaults.PAA_SYSTEM_ERROR, exception.getErrorCode());
    assertTrue(exception.getErrorEmitter().contains("Synchronization error"));
  }
}
