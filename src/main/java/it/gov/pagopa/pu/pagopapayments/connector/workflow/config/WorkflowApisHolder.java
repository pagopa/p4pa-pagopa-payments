package it.gov.pagopa.pu.pagopapayments.connector.workflow.config;

import it.gov.pagopa.pu.pagopapayments.config.rest.RestTemplateConfig;
import it.gov.pagopa.pu.workflowhub.controller.generated.WorkflowApi;
import it.gov.pagopa.pu.workflowhub.generated.ApiClient;
import it.gov.pagopa.pu.workflowhub.generated.BaseApi;
import jakarta.annotation.PreDestroy;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class WorkflowApisHolder {

  private final WorkflowApi workflowApi;

  private final ThreadLocal<String> bearerTokenHolder = new ThreadLocal<>();

  public WorkflowApisHolder(
    WorkflowApiClientConfig clientConfig,
    RestTemplateBuilder restTemplateBuilder
  ) {
    RestTemplate restTemplate = restTemplateBuilder.build();
    ApiClient apiClient = new ApiClient(restTemplate);
    apiClient.setBasePath(clientConfig.getBaseUrl());
    apiClient.setBearerToken(bearerTokenHolder::get);
    apiClient.setMaxAttemptsForRetry(Math.max(1, clientConfig.getMaxAttempts()));
    apiClient.setWaitTimeMillis(clientConfig.getWaitTimeMillis());
    if (clientConfig.isPrintBodyWhenError()) {
      restTemplate.setErrorHandler(RestTemplateConfig.bodyPrinterWhenError("WORKFLOW-HUB"));
    }

    this.workflowApi = new WorkflowApi(apiClient);
  }

  @PreDestroy
  public void unload() {
    bearerTokenHolder.remove();
  }

  public WorkflowApi getWorkflowApi(String accessToken) {
    return getApi(accessToken, workflowApi);
  }

  private <T extends BaseApi> T getApi(String accessToken, T api) {
    bearerTokenHolder.set(accessToken);
    return api;
  }
}
