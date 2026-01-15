package it.gov.pagopa.pu.pagopapayments.connector.workflow.client;

import it.gov.pagopa.pu.pagopapayments.connector.workflow.config.WorkflowApisHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

@Service
@Slf4j
public class WorkflowApiClient {

  private final WorkflowApisHolder workflowApisHolder;

  public WorkflowApiClient(WorkflowApisHolder workflowApisHolder) {
    this.workflowApisHolder = workflowApisHolder;
  }


  public String waitWorkflowCompletion(String workflowId, Integer maxAttempts, Integer retryDelayMs, String accessToken) {
    try {
      return workflowApisHolder.getWorkflowApi(accessToken).waitWorkflowCompletion(workflowId, maxAttempts, retryDelayMs).getStatus();
    } catch (HttpClientErrorException e) {
      return "WORKFLOW_" + (e.getStatusCode() instanceof HttpStatus status
        ? status.name()
        : String.valueOf(e.getStatusCode().value()));
    }
  }
}
