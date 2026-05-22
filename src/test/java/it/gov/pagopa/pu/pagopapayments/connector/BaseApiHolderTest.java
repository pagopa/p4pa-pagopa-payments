package it.gov.pagopa.pu.pagopapayments.connector;

import it.gov.pagopa.pu.pagopapayments.config.rest.ApiClientConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.http.client.MockClientHttpResponse;
import org.springframework.web.client.*;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.IntStream;

@Slf4j
public abstract class BaseApiHolderTest {

  public enum AUTH_TYPE {
    API_KEY,
    BEARER
  }

  @Mock
  protected RestTemplate restTemplateMock;
  @Mock
  protected Void voidMock;

  protected <T> void assertAuthenticationShouldBeSetInThreadSafeMode(Function<String, T> apiInvoke, ParameterizedTypeReference<T> apiReturnedType, Runnable apiUnloader) throws InterruptedException {
    assertAuthenticationShouldBeSetInThreadSafeMode(apiInvoke, apiReturnedType, apiUnloader, AUTH_TYPE.BEARER, HttpHeaders.AUTHORIZATION);
  }

  @SuppressWarnings("unchecked")
  protected <T> void assertAuthenticationShouldBeSetInThreadSafeMode(Function<String, T> apiInvoke, ParameterizedTypeReference<T> apiReturnedType, Runnable apiUnloader, AUTH_TYPE authType, String authHeader) throws InterruptedException {
    // Configuring useCases in a single thread
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
              Mockito.argThat(req ->
                req.getHeaders().getOrDefault(authHeader, Collections.emptyList()).getFirst()
                  .equals(authPrefix + auth)),
              Mockito.eq(apiReturnedType));
          return Pair.of(auth, expectedResult);
        } catch (Exception e) {
          throw new IllegalStateException(e);
        }
      })
      .toList();

    try (ExecutorService executorService = Executors.newFixedThreadPool(10)) {
      executorService.invokeAll(useCases.stream()
          .map(p -> (Callable<?>) () -> {
            // Given
            String accessToken = p.getKey();
            T expectedResult = p.getValue();

            // When
            T result = apiInvoke.apply(accessToken);

            // Then
            Assertions.assertSame(expectedResult, result);
            return true;
          })
          .toList())
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

  /**
   * To assert if the ApiClient is working as expected. Set a test just once per *ApiHolder class (not for each exposed API)
   */
  protected <T> void assertRetry(ApiClientConfig apiClientConfig, Function<String, T> apiInvoke, ParameterizedTypeReference<T> apiReturnedType) {
    Assertions.assertTrue(apiClientConfig.getMaxAttempts() > 1, "Please set at least 2 max attempt");

    ResponseErrorHandler errorHandler = Mockito.mockingDetails(restTemplateMock)
      .getInvocations()
      .stream()
      .filter(i -> i.getMethod().getName().equals("setErrorHandler"))
      .map(i -> (ResponseErrorHandler) i.getArgument(0))
      .findFirst()
      .orElse(null);

    for (HttpStatus httpStatus : HttpStatus.values()) {
      if (httpStatus.is5xxServerError() || httpStatus.isSameCodeAs(HttpStatus.TOO_MANY_REQUESTS)) {
        HttpStatusCodeException exception = httpStatus.is5xxServerError()
          ? new HttpServerErrorException(httpStatus)
          : new HttpClientErrorException(httpStatus);

        Mockito.doAnswer(i -> {
            if (errorHandler != null) {
              errorHandler.handleError(URI.create("http://example.com"), HttpMethod.GET, new MockClientHttpResponse(new byte[0], httpStatus));
              return null;
            } else {
              throw exception;
            }
          })
          .when(restTemplateMock)
          .exchange(
            Mockito.any(),
            Mockito.eq(apiReturnedType));

        Assertions.assertThrows(RuntimeException.class, () -> apiInvoke.apply("accessToken"));

        try {
          Mockito.verify(restTemplateMock, Mockito.times(apiClientConfig.getMaxAttempts()))
            .exchange(Mockito.any(), Mockito.eq(apiReturnedType));
          Mockito.clearInvocations(restTemplateMock);
        } catch (Throwable e) {
          log.error("Error occurred verifying retry for httpStatus {}: {}", httpStatus, e.getMessage());
          throw e;
        }
      }
    }
  }
}
