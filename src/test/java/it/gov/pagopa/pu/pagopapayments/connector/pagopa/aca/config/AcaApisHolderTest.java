package it.gov.pagopa.pu.pagopapayments.connector.pagopa.aca.config;

import it.gov.pagopa.pu.aca.gpd.v1.dto.generated.PaymentPositionModel;
import it.gov.pagopa.pu.pagopapayments.config.json.JsonConfig;
import it.gov.pagopa.pu.pagopapayments.config.rest.HttpClientErrorHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.util.Pair;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.IntStream;

@ExtendWith(MockitoExtension.class)
class AcaApisHolderTest {

  enum AUTH_TYPE {
    API_KEY,
    BEARER
  }

  @Mock
  private RestTemplateBuilder restTemplateBuilderMock;

  @Mock
  private RestTemplate restTemplateMock;

  @Mock
  private Void voidMock;

  private AcaApisHolder acaApisHolder;

  private static final String ORG_FISCAL_CODE = "12345678901";
  private static final String API_KEY_HEADER = "Ocp-Apim-Subscription-Key";

  @BeforeEach
  void setUp() {
    Mockito.when(restTemplateBuilderMock.build()).thenReturn(restTemplateMock);
    Mockito.when(restTemplateMock.getUriTemplateHandler()).thenReturn(new DefaultUriBuilderFactory());

    AcaApiClientConfig apiClient = new AcaApiClientConfig();
    apiClient.setBaseUrl("http://example.com");

    acaApisHolder = new AcaApisHolder(apiClient, restTemplateBuilderMock, new JsonConfig().objectMapperJackson3());
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verify(restTemplateMock)
      .setErrorHandler(Mockito.any(HttpClientErrorHandler.class));

    Mockito.verifyNoMoreInteractions(
      restTemplateBuilderMock,
      restTemplateMock
    );
  }

  @Test
  void whenGetOrganizationEntityControllerApiThenAuthenticationShouldBeSetInThreadSafeMode() throws InterruptedException {
    assertAuthenticationShouldBeSetInThreadSafeMode(
      apiKey -> acaApisHolder.getApiClientByApiKey(apiKey)
        .createPosition(ORG_FISCAL_CODE, new PaymentPositionModel(), null, true),
      new ParameterizedTypeReference<>() {
      },
      () -> {
      },
      AUTH_TYPE.API_KEY,
      API_KEY_HEADER
    );
  }

  @SuppressWarnings("unchecked")
  private <T> void assertAuthenticationShouldBeSetInThreadSafeMode(
    Function<String, T> apiInvoke,
    ParameterizedTypeReference<T> apiReturnedType,
    Runnable apiUnloader,
    AUTH_TYPE authType,
    String authHeader
  ) throws InterruptedException {

    List<Pair<String, T>> useCases = IntStream.rangeClosed(0, 100)
      .mapToObj(i -> {
        try {
          String auth = "auth" + i;
          String authPrefix = AUTH_TYPE.BEARER.equals(authType) ? "Bearer " : "";

          T expectedResult =
            String.class.equals(apiReturnedType.getType()) ? (T) "RESULT"
              : Integer.class.equals(apiReturnedType.getType()) ? (T) Integer.valueOf(0)
              : Long.class.equals(apiReturnedType.getType()) ? (T) Long.valueOf(0L)
              : apiReturnedType.getType().getTypeName().startsWith(List.class.getName()) ? (T) List.of()
              : Void.class.equals(apiReturnedType.getType()) ? (T) voidMock
              : "byte[]".equals(apiReturnedType.getType().getTypeName()) ? (T) new byte[0]
              : (T) Mockito.mock(Class.forName(apiReturnedType.getType().getTypeName()));

          Mockito.doReturn(ResponseEntity.ok(expectedResult))
            .when(restTemplateMock)
            .exchange(
              Mockito.argThat(req -> req.getHeaders()
                .getOrDefault(authHeader, Collections.emptyList())
                .getFirst()
                .equals(authPrefix + auth)),
              Mockito.eq(apiReturnedType)
            );

          return Pair.of(auth, expectedResult);
        } catch (Exception e) {
          throw new IllegalStateException(e);
        }
      })
      .toList();

    try (ExecutorService executorService = Executors.newFixedThreadPool(10)) {
      executorService.invokeAll(
          useCases.stream()
            .map(p -> (Callable<?>) () -> {
              String accessToken = p.getFirst();
              T expectedResult = p.getSecond();

              T result = apiInvoke.apply(accessToken);

              Assertions.assertSame(expectedResult, result);
              return true;
            })
            .toList()
        )
        .forEach(future -> {
          try {
            future.get();
          } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
          }
        });
    }

    apiUnloader.run();

    Mockito.verify(restTemplateMock, Mockito.times(useCases.size()))
      .exchange(Mockito.any(), Mockito.<ParameterizedTypeReference<?>>any());
  }
}

