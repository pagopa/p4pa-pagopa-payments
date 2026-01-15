package it.gov.pagopa.pu.pagopapayments.connector.workflow.service;

import it.gov.pagopa.pu.pagopapayments.connector.workflow.client.WorkflowApiClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WorkflowServiceTest {

  @Mock
  private WorkflowApiClient workflowApiClientMock;

  private WorkflowService workflowService;

  @BeforeEach
  void init() {
    workflowService = new WorkflowServiceImpl(
      workflowApiClientMock
    );
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      workflowApiClientMock
    );
  }

  @Test
  void whenSyncDebtPositionThenOk() {
    // Given
    String accessToken = "ACCESSTOKEN";
    String workflowId = "1234";
    String expectedState = "WORFLOW_OK";

    Mockito.when(workflowApiClientMock.waitWorkflowCompletion(Mockito.same(workflowId), Mockito.any(), Mockito.any(), Mockito.same(accessToken)))
      .thenReturn(expectedState);

    // When
    String result = workflowService.waitWorkflowCompletion(workflowId, 1, 1000, accessToken);

    // Then
    Assertions.assertSame(expectedState, result);
  }

}
