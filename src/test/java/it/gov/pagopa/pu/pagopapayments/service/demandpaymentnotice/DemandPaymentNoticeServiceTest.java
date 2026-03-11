package it.gov.pagopa.pu.pagopapayments.service.demandpaymentnotice;

import it.gov.pagopa.pagopa_api.pa.pafornode.PaDemandPaymentNoticeRequest;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionTypeOrg;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.pagopapayments.connector.auth.AuthnService;
import it.gov.pagopa.pu.pagopapayments.connector.organization.OrganizationService;
import it.gov.pagopa.pu.pagopapayments.connector.workflow.service.WorkflowService;
import it.gov.pagopa.pu.pagopapayments.exception.PagoPaNodeFaultException;
import it.gov.pagopa.pu.pagopapayments.service.debtposition.cie.CieDebtPositionFacadeService;
import it.gov.pagopa.pu.pagopapayments.util.Constants;
import it.gov.pagopa.pu.pagopapayments.util.TestUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.jemos.podam.api.PodamFactory;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DemandPaymentNoticeServiceTest {

  @Mock
  private OrganizationService organizationServiceMock;
  @Mock
  private AuthnService authnServiceMock;
  @Mock
  private WorkflowService workflowServiceMock;
  @Mock
  private CieDebtPositionFacadeService cieDebtPositionFacadeServiceMock;
  private DemandPaymentNoticeService demandPaymentNoticeService;

  private final PodamFactory podamFactory;
  private final String cieServiceId = "99";

  private static final String ACCESS_TOKEN = "access-token";

  public DemandPaymentNoticeServiceTest() {
    podamFactory = TestUtils.getPodamFactory();
  }

  @BeforeEach
  void setUp() {
    demandPaymentNoticeService = new DemandPaymentNoticeService(
      organizationServiceMock,
      authnServiceMock,
      workflowServiceMock,
      cieDebtPositionFacadeServiceMock,
      cieServiceId
    );
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      organizationServiceMock,
      authnServiceMock,
      workflowServiceMock,
      cieDebtPositionFacadeServiceMock
    );
  }

  @Test
  void givenCieServiceIdWhenHandleRequestThenSuccess() {
    // Given
    PaDemandPaymentNoticeRequest request = podamFactory.manufacturePojo(PaDemandPaymentNoticeRequest.class);
    request.setIdServizio(cieServiceId);
    Organization organization = podamFactory.manufacturePojo(Organization.class);
    DebtPositionDTO createdDebtPosition = podamFactory.manufacturePojo(DebtPositionDTO.class);
    DebtPositionTypeOrg debtPositionTypeOrg = podamFactory.manufacturePojo(DebtPositionTypeOrg.class);
    debtPositionTypeOrg.setCode(Constants.SPONTANEOUS_PSP_DP_TYPE_ORG_CODE);
    String workflowId = "wf-123";

    when(authnServiceMock.getAccessToken()).thenReturn(ACCESS_TOKEN);
    when(organizationServiceMock.getOrganizationByFiscalCode(request.getIdPA(), ACCESS_TOKEN))
      .thenReturn(organization);

    when(cieDebtPositionFacadeServiceMock.createCieDebtPosition(request.getDatiSpecificiServizioRequest(), organization, ACCESS_TOKEN))
      .thenReturn(Pair.of(createdDebtPosition,workflowId));

    when(workflowServiceMock.waitWorkflowCompletion(workflowId, 10, 1000, ACCESS_TOKEN))
      .thenReturn(Constants.WORKFLOW_STATUS_COMPLETED_VALUE);

    // When
    DebtPositionDTO result = demandPaymentNoticeService.handleRequest(request);

    // Then
    assertNotNull(result);
    assertEquals(createdDebtPosition, result);
  }

  @Test
  void givenCieServiceIdAndNoWorkflowIdWhenHandleRequestThenSuccess() {
    // Given
    PaDemandPaymentNoticeRequest request = podamFactory.manufacturePojo(PaDemandPaymentNoticeRequest.class);
    request.setIdServizio(cieServiceId);
    Organization organization = podamFactory.manufacturePojo(Organization.class);
    DebtPositionDTO createdDebtPosition = podamFactory.manufacturePojo(DebtPositionDTO.class);
    DebtPositionTypeOrg debtPositionTypeOrg = podamFactory.manufacturePojo(DebtPositionTypeOrg.class);
    debtPositionTypeOrg.setCode(Constants.SPONTANEOUS_PSP_DP_TYPE_ORG_CODE);

    when(authnServiceMock.getAccessToken()).thenReturn(ACCESS_TOKEN);
    when(organizationServiceMock.getOrganizationByFiscalCode(request.getIdPA(), ACCESS_TOKEN))
      .thenReturn(organization);

    when(cieDebtPositionFacadeServiceMock.createCieDebtPosition(request.getDatiSpecificiServizioRequest(), organization, ACCESS_TOKEN))
      .thenReturn(Pair.of(createdDebtPosition,null));

    // When
    DebtPositionDTO result = demandPaymentNoticeService.handleRequest(request);

    // Then
    assertNotNull(result);
    assertEquals(createdDebtPosition, result);
  }

  @Test
  void givenWrongServiceIdWhenHandleRequestThenThrowException() {
    // Given
    PaDemandPaymentNoticeRequest request = podamFactory.manufacturePojo(PaDemandPaymentNoticeRequest.class);
    request.setIdServizio("-1");
    Organization organization = podamFactory.manufacturePojo(Organization.class);

    when(authnServiceMock.getAccessToken()).thenReturn(ACCESS_TOKEN);
    when(organizationServiceMock.getOrganizationByFiscalCode(request.getIdPA(), ACCESS_TOKEN))
      .thenReturn(organization);

    // When
    PagoPaNodeFaultException serviceNotImplementedException = assertThrows(PagoPaNodeFaultException.class, () -> demandPaymentNoticeService.handleRequest(request));

    assertEquals("There is no implementation for serviceId " + request.getIdServizio(),serviceNotImplementedException.getErrorEmitter());
  }
}
