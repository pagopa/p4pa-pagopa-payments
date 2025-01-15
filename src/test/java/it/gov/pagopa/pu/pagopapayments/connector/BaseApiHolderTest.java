package it.gov.pagopa.pu.pagopapayments.connector;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.IntStream;

public abstract class BaseApiHolderTest {

  @Mock
  protected RestTemplate restTemplateMock;

  protected <T> void assertAuthenticationShouldBeSetInThreadSafeMode(Function<String, T> apiInvoke, Class<T> apiReturnedType, Runnable apiUnloader) throws InterruptedException {
    // Configuring useCases in a single thread
    List<Pair<String, T>> useCases = IntStream.rangeClosed(0, 100)
      .mapToObj(i -> {
        try {
          String accessToken = "accessToken" + i;
          T expectedResult = apiReturnedType.getConstructor().newInstance();

          Mockito.doReturn(ResponseEntity.ok(expectedResult))
            .when(restTemplateMock)
            .exchange(
              Mockito.argThat(req ->
                req.getHeaders().getOrDefault(HttpHeaders.AUTHORIZATION, Collections.emptyList()).getFirst()
                  .equals("Bearer " + accessToken)),
              Mockito.eq(apiReturnedType));
          return Pair.of(accessToken, expectedResult);
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
        .toList());
    }

    apiUnloader.run();

    Mockito.verify(restTemplateMock, Mockito.times(useCases.size()))
      .exchange(Mockito.any(), Mockito.<ParameterizedTypeReference<?>>any());
  }
}
