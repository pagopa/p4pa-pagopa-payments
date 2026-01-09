package it.gov.pagopa.pu.pagopapayments.service.demandpaymentnotice;

import it.gov.pagopa.pagopa_api.pa.pafornode.PaDemandPaymentNoticeRequest;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.pagopapayments.connector.auth.AuthnService;
import it.gov.pagopa.pu.pagopapayments.connector.debtpositions.DebtPositionService;
import it.gov.pagopa.pu.pagopapayments.connector.organization.OrganizationService;
import it.gov.pagopa.pu.pagopapayments.connector.workflow.service.WorkflowService;
import it.gov.pagopa.pu.pagopapayments.enums.PagoPaNodeFaults;
import it.gov.pagopa.pu.pagopapayments.exception.PagoPaNodeFaultException;
import it.gov.pagopa.pu.pagopapayments.util.Constants;
import it.gov.pagopa.pu.pagopapayments.util.TestUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.jemos.podam.api.PodamFactory;

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
  @InjectMocks
  private DemandPaymentNoticeService demandPaymentNoticeService;

  private final PodamFactory podamFactory;

  private static final String ACCESS_TOKEN = "access-token";

  public DemandPaymentNoticeServiceTest() {
    podamFactory = TestUtils.getPodamFactory();
  }

  @Test
  void givenValidRequestWhenHandleRequestThenSuccess() {
    // Given
    PaDemandPaymentNoticeRequest request = podamFactory.manufacturePojo(PaDemandPaymentNoticeRequest.class);
    Organization organization = podamFactory.manufacturePojo(Organization.class);
    DebtPositionDTO createdDebtPosition = podamFactory.manufacturePojo(DebtPositionDTO.class);
    String workflowId = "wf-123";

    when(authnServiceMock.getAccessToken()).thenReturn(ACCESS_TOKEN);
    when(organizationServiceMock.getOrganizationByFiscalCode(request.getIdPA(), ACCESS_TOKEN))
      .thenReturn(organization);

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
  void givenWorkflowSyncErrorWhenHandleRequestThenThrowException() {
    // Given
    PaDemandPaymentNoticeRequest request = podamFactory.manufacturePojo(PaDemandPaymentNoticeRequest.class);
    Organization organization = podamFactory.manufacturePojo(Organization.class);
    DebtPositionDTO createdDebtPosition = podamFactory.manufacturePojo(DebtPositionDTO.class);
    String workflowId = "wf-failed";

    when(authnServiceMock.getAccessToken()).thenReturn(ACCESS_TOKEN);
    when(organizationServiceMock.getOrganizationByFiscalCode(request.getIdPA(), ACCESS_TOKEN))
      .thenReturn(organization);

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
