package it.gov.pagopa.pu.pagopapayments.connector.workflow.service;

import it.gov.pagopa.pu.pagopapayments.connector.workflow.client.WorkflowApiClient;
import org.springframework.stereotype.Service;

@Service
public class WorkflowServiceImpl implements WorkflowService {
  private final WorkflowApiClient workflowApiClient;

  public WorkflowServiceImpl(WorkflowApiClient workflowApiClient) {
    this.workflowApiClient = workflowApiClient;
  }

  @Override
  public String waitWorkflowCompletion(String workflowId, Integer maxAttempts, Integer retryDelayMs, String accessToken) {
    return workflowApiClient.waitWorkflowCompletion(workflowId, maxAttempts, retryDelayMs, accessToken);
  }
}
