package it.gov.pagopa.pu.pagopapayments.connector.pagopa.aca_gpd;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class BaseApisHolderTest {

  @Mock
  private RestTemplateBuilder restTemplateBuilderMock;
  @Mock
  private RestTemplate restTemplateMock;

  private BaseApisHolder baseApisHolder;

  @BeforeEach
  void setUp() {
    Mockito.when(restTemplateBuilderMock.build()).thenReturn(restTemplateMock);
  }

  @Test
  void givenConfigWithPrintBodyTrueWhenConstructorCalledThenErrorHandlerIsSet() {
    // Given
    boolean printBodyWhenError = true;
    String serviceName = "TEST_SERVICE";

    // When
    baseApisHolder = new BaseApisHolder(
      restTemplateBuilderMock,
      "http://base.url",
      3,
      1000L,
      printBodyWhenError,
      serviceName
    ) {};

    // Then
    Mockito.verify(restTemplateBuilderMock).build();
    Mockito.verify(restTemplateMock).setErrorHandler(Mockito.any());
  }

  @Test
  void givenConfigWithPrintBodyFalseWhenConstructorCalledThenErrorHandlerIsNotSet() {
    // Given
    boolean printBodyWhenError = false;
    String serviceName = "TEST_SERVICE";

    // When
    baseApisHolder = new BaseApisHolder(
      restTemplateBuilderMock,
      "http://base.url",
      3,
      1000L,
      printBodyWhenError,
      serviceName
    ) {};

    // Then
    Mockito.verify(restTemplateBuilderMock).build();
    Mockito.verify(restTemplateMock, Mockito.never()).setErrorHandler(Mockito.any());
  }

  @Test
  void givenSameApiKeyWhenGetApiClientByApiKeyCalledTwiceThenReturnCachedInstance() {
    // Given
    String apiKey = "test-api-key";
    baseApisHolder = new BaseApisHolder(
      restTemplateBuilderMock,
      "http://base.url",
      3,
      1000L,
      false,
      "TEST"
    ) {};

    // When
    var client1 = baseApisHolder.getApiClientByApiKey(apiKey);
    var client2 = baseApisHolder.getApiClientByApiKey(apiKey);

    // Then
    Assertions.assertSame(client1, client2);
  }

  @Test
  void givenDifferentApiKeyWhenGetApiClientByApiKeyCalledThenReturnNewInstance() {
    // Given
    String apiKey1 = "key-1";
    String apiKey2 = "key-2";
    baseApisHolder = new BaseApisHolder(
      restTemplateBuilderMock,
      "http://base.url",
      3,
      1000L,
      false,
      "TEST"
    ) {};

    // When
    var client1 = baseApisHolder.getApiClientByApiKey(apiKey1);
    var client2 = baseApisHolder.getApiClientByApiKey(apiKey2);

    // Then
    Assertions.assertNotSame(client1, client2);
  }
}
