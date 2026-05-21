package it.gov.pagopa.pu.pagopapayments.connector.fileshare.config;

import it.gov.pagopa.pu.fileshare.dto.generated.FileOrigin;
import it.gov.pagopa.pu.fileshare.dto.generated.IngestionFlowFileType;
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
import org.springframework.core.io.Resource;
import org.springframework.web.util.DefaultUriBuilderFactory;

@ExtendWith(MockitoExtension.class)
class FileShareApisHolderTest extends BaseApiHolderTest {
    @Mock
    private RestTemplateBuilder restTemplateBuilderMock;

    private FileShareApisHolder apisHolder;
    private FileShareApiClientConfig apiClientConfig;

    @BeforeEach
    void setUp() {
        Mockito.when(restTemplateBuilderMock.build()).thenReturn(restTemplateMock);
        Mockito.when(restTemplateMock.getUriTemplateHandler()).thenReturn(new DefaultUriBuilderFactory());

        apiClientConfig = FileShareApiClientConfig.builder()
          .baseUrl("http://example.com")
          .maxAttempts(3)
          .build();
        apisHolder = new FileShareApisHolder(apiClientConfig, restTemplateBuilderMock);
    }

    @AfterEach
    void verifyNoMoreInteractions() {
        Mockito.verifyNoMoreInteractions(
                restTemplateBuilderMock,
                restTemplateMock
        );
    }

    @Test
    void whenGetIngestionFlowFileApiThenAuthenticationShouldBeSetInThreadSafeMode() throws InterruptedException {
        assertAuthenticationShouldBeSetInThreadSafeMode(
                accessToken -> apisHolder.getIngestionFlowFileApi(accessToken)
                        .uploadIngestionFlowFile(1L, IngestionFlowFileType.PAYMENTS_REPORTING, FileOrigin.PORTAL, "FILENAME", null, Mockito.mock(Resource.class), null),
                new ParameterizedTypeReference<>() {},
                apisHolder::unload);
    }

  @Test
  void testRetryConfiguration() {
    assertRetry(apiClientConfig,
      accessToken -> apisHolder.getIngestionFlowFileApi(accessToken)
        .uploadIngestionFlowFile(1L, IngestionFlowFileType.PAYMENTS_REPORTING, FileOrigin.PORTAL, "FILENAME", null, Mockito.mock(Resource.class), null),
      new ParameterizedTypeReference<>() {
      }
    );
  }

}
