package it.gov.pagopa.pu.pagopapayments.connector.workflow.client;

import it.gov.pagopa.pu.pagopapayments.connector.workflow.config.WorkflowApisHolder;
import it.gov.pagopa.pu.pagopapayments.exception.common.RestInvokeNotFoundException;
import it.gov.pagopa.pu.workflowhub.client.generated.WorkflowApi;
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

import static org.mockito.Mockito.when;

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

    when(workflowApisHolderMock.getWorkflowApi(accessToken)).thenReturn(workflowApiMock);
    when(workflowApiMock.waitWorkflowCompletion(workflowId, 1, 1000)).thenReturn(new WorkflowStatusDTO().status(expectedState));

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

    when(workflowApisHolderMock.getWorkflowApi(accessToken)).thenReturn(workflowApiMock);
    when(workflowApiMock.waitWorkflowCompletion(workflowId, 1, 1000))
      .thenThrow(new RestInvokeNotFoundException("APPNAME", HttpStatus.NOT_FOUND, "ERROR", "ERRORCODE", "ERRORMESSAGE"));

    // When
    String result = workflowApiClient.waitWorkflowCompletion(workflowId, 1, 1000, accessToken);

    // Then
    Assertions.assertEquals("ERRORCODE", result);
  }

}
