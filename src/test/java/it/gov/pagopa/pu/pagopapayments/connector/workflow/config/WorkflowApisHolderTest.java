package it.gov.pagopa.pu.pagopapayments.connector.workflow.config;

import it.gov.pagopa.pu.pagopapayments.connector.BaseApiHolderTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.util.DefaultUriBuilderFactory;

@ExtendWith(MockitoExtension.class)
class WorkflowApisHolderTest extends BaseApiHolderTest {
  @Mock
  private RestTemplateBuilder restTemplateBuilderMock;

  private WorkflowApisHolder workflowApisHolder;
  private WorkflowApiClientConfig apiClientConfig;

  @BeforeEach
  void setUp() {
    Mockito.when(restTemplateBuilderMock.build()).thenReturn(restTemplateMock);
    Mockito.when(restTemplateMock.getUriTemplateHandler()).thenReturn(new DefaultUriBuilderFactory());

    apiClientConfig = WorkflowApiClientConfig.builder()
      .baseUrl("http://example.com")
      .maxAttempts(3)
      .build();

    workflowApisHolder = new WorkflowApisHolder(apiClientConfig, restTemplateBuilderMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      restTemplateBuilderMock,
      restTemplateMock
    );
  }

  @Test
  void whenDebtPositionApiThenAuthenticationShouldBeSetInThreadSafeMode() throws InterruptedException {
    assertAuthenticationShouldBeSetInThreadSafeMode(
      accessToken -> workflowApisHolder.getWorkflowApi(accessToken)
        .waitWorkflowCompletion("1234", 1, 1000),
      new ParameterizedTypeReference<>() {
      },
      workflowApisHolder::unload);
  }

  @Test
  void testRetryConfiguration() {
    assertRetry(apiClientConfig,
      accessToken -> workflowApisHolder.getWorkflowApi(accessToken)
        .waitWorkflowCompletion("1234", 1, 1000),
      new ParameterizedTypeReference<>() {
      }
    );
  }

}
