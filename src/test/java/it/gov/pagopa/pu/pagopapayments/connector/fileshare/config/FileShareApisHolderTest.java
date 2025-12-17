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

    @BeforeEach
    void setUp() {
        Mockito.when(restTemplateBuilderMock.build()).thenReturn(restTemplateMock);
        Mockito.when(restTemplateMock.getUriTemplateHandler()).thenReturn(new DefaultUriBuilderFactory());
        FileShareApiClientConfig clientConfig = FileShareApiClientConfig.builder()
          .baseUrl("http://example.com")
          .build();
        apisHolder = new FileShareApisHolder(clientConfig, restTemplateBuilderMock);
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
                        .uploadIngestionFlowFile(1L, IngestionFlowFileType.PAYMENTS_REPORTING, FileOrigin.PORTAL, Mockito.mock(Resource.class), "FILENAME", null),
                new ParameterizedTypeReference<>() {},
                apisHolder::unload);
    }

}
