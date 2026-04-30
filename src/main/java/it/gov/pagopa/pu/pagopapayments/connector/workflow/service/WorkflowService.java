package it.gov.pagopa.pu.pagopapayments.connector.workflow.service;

public interface WorkflowService {

  String waitWorkflowCompletion(String workflowId, Integer maxAttempts, Integer retryDelayMs, String accessToken);
}
