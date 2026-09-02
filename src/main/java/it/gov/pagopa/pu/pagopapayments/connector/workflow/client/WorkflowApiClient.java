package it.gov.pagopa.pu.pagopapayments.connector.workflow.client;

import it.gov.pagopa.pu.pagopapayments.connector.workflow.config.WorkflowApisHolder;
import it.gov.pagopa.pu.pagopapayments.exception.common.BaseBusinessException;
import it.gov.pagopa.pu.pagopapayments.exception.common.RestInvokeHttpClientException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
    } catch (BaseBusinessException e) {
      if(e instanceof RestInvokeHttpClientException) {
        return e.getCode();
      } else {
        throw e;
      }
    }
  }
}
