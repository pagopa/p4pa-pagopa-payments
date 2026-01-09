package it.gov.pagopa.pu.pagopapayments.connector.workflow.client;

import it.gov.pagopa.pu.pagopapayments.connector.workflow.config.WorkflowApisHolder;
import it.gov.pagopa.pu.workflowhub.controller.generated.WorkflowApi;
import it.gov.pagopa.pu.workflowhub.dto.generated.WorkflowStatusDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

@ExtendWith(MockitoExtension.class)
class WorflowApiClientTest {
  @Mock
  private WorkflowApisHolder workflowApisHolderMock;
  @Mock
  private WorkflowApi workflowApiMock;

  private WorkflowApiClient workflowApiClient;

  @BeforeEach
  void setUp() {
    workflowApiClient = new WorkflowApiClient(workflowApisHolderMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      workflowApisHolderMock
    );
  }

  @Test
  void givenValidIdWhenWaitWorkflowCompletionThenOk() {
    // Given
    String accessToken = "ACCESSTOKEN";
    String workflowId = "1234";
    String expectedState = "WORFLOW_OK";

    Mockito.when(workflowApisHolderMock.getWorkflowApi(accessToken)).thenReturn(workflowApiMock);
    Mockito.when(workflowApiMock.waitWorkflowCompletion(workflowId, 1, 1000)).thenReturn(new WorkflowStatusDTO().status(expectedState));

    // When
    String result = workflowApiClient.waitWorkflowCompletion(workflowId, 1, 1000, accessToken);

    // Then
    Assertions.assertSame(expectedState, result);
  }

  @Test
  void givenInvalidStateWhenWaitWorkflowCompletionThenErrorStatus() {
    // Given
    String accessToken = "ACCESSTOKEN";
    String workflowId = "1234";

    Mockito.when(workflowApisHolderMock.getWorkflowApi(accessToken)).thenReturn(workflowApiMock);
    HttpClientErrorException expectedException = HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not found", null, null, null);
    String expectedState = "WORKFLOW_"+HttpStatus.NOT_FOUND.name();
    Mockito.when(workflowApiMock.waitWorkflowCompletion(workflowId, 1, 1000)).thenThrow(expectedException);

    // When
    String result = workflowApiClient.waitWorkflowCompletion(workflowId, 1, 1000, accessToken);

    // Then
    Assertions.assertEquals(expectedState, result);
  }

}
