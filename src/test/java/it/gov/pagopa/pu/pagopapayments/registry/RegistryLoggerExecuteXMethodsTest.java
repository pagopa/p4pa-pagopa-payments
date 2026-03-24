package it.gov.pagopa.pu.pagopapayments.registry;

import it.gov.pagopa.pu.registries.dto.generated.RegistryOutcome;
import org.apache.commons.lang3.function.TriFunction;
import org.apache.commons.lang3.tuple.Triple;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;

@ExtendWith(MockitoExtension.class)
class RegistryLoggerExecuteXMethodsTest {

  private RegistryLogger registryLoggerSpy;

  @BeforeEach
  void init(){
    registryLoggerSpy = Mockito.spy(new RegistryLogger(null, null, null));
  }

//region utility for executeX tests
  private <I> void configureRegistryLoggerPreExecute(I request, RegistryContextData contextData, Supplier<Map<String, Object>> registryBodyRequestExtraInfoRetriever) {
    doNothing()
      .when(registryLoggerSpy)
      .preExecute(same(contextData), same(request), same(registryBodyRequestExtraInfoRetriever));
  }

  private <I, O> void configureRegistryLoggerSpyNoExceptionExpected(I request, O expectedResult, RegistryContextData contextData, Supplier<Map<String, Object>> registryBodyRequestExtraInfoRetriever, Function<O, Map<String, Object>> registryBodyResponseExtraInfoExtractor, String expectedIuv, RegistryOutcome expectedOutcome) {
    configureRegistryLoggerPreExecute(request, contextData, registryBodyRequestExtraInfoRetriever);
    doNothing()
      .when(registryLoggerSpy)
      .postExecute(same(contextData), same(registryBodyResponseExtraInfoExtractor), eq(expectedIuv), eq(expectedResult), same(expectedOutcome), isNull());
  }

  private <I, O> void configureRegistryLoggerSpyExceptionExpected(I request, RegistryContextData contextData, Supplier<Map<String, Object>> registryBodyRequestExtraInfoRetriever, Function<O, Map<String, Object>> registryBodyResponseExtraInfoExtractor, RuntimeException expectedException) {
    configureRegistryLoggerPreExecute(request, contextData, registryBodyRequestExtraInfoRetriever);
    doNothing()
      .when(registryLoggerSpy)
      .postExecute(same(contextData), same(registryBodyResponseExtraInfoExtractor), isNull(), isNull(), eq(RegistryOutcome.KO), same(expectedException));

  }
//endregion

//region execute1
  @ParameterizedTest
  @CsvSource("request,response,ARG1")
  <I, O, A1> void whenExecute1ThenOk(I request, O expectedResult, A1 arg1) {
    // Given
    Function<A1, Triple<O, String, RegistryOutcome>> requestHandler = Mockito.mock();
    Function<Exception, O> exceptionHandler = Mockito.mock();
    Function<O, Map<String, Object>> registryBodyResponseExtraInfoExtractor = Mockito.mock();

    RegistryContextData contextData = new RegistryContextData();
    Supplier<Map<String, Object>> registryBodyRequestExtraInfoRetriever = Mockito.mock();

    String expectedIuv = "iuv";
    RegistryOutcome expectedOutcome = RegistryOutcome.OK;

    Mockito.when(requestHandler.apply(Mockito.same(arg1)))
      .thenReturn(Triple.of(expectedResult, expectedIuv, expectedOutcome));

    configureRegistryLoggerSpyNoExceptionExpected(request, expectedResult, contextData, registryBodyRequestExtraInfoRetriever, registryBodyResponseExtraInfoExtractor, expectedIuv, expectedOutcome);

    // When
    O result = registryLoggerSpy.execute1(
      contextData, request,
      requestHandler, exceptionHandler,
      registryBodyRequestExtraInfoRetriever, registryBodyResponseExtraInfoExtractor,
      arg1);

    // Then
    Assertions.assertSame(expectedResult, result);
  }

  @ParameterizedTest
  @CsvSource("request,ARG1")
  <I, O, A1> void givenNotHandledExceptionWhenExecute1ThenThrowException(I request, A1 arg1) {
    // Given
    Function<A1, Triple<O, String, RegistryOutcome>> requestHandler = Mockito.mock();
    Function<Exception, O> exceptionHandler = null;
    Function<O, Map<String, Object>> registryBodyResponseExtraInfoExtractor = Mockito.mock();

    RegistryContextData contextData = new RegistryContextData();
    Supplier<Map<String, Object>> registryBodyRequestExtraInfoRetriever = Mockito.mock();

    RuntimeException expectedException = new RuntimeException("simulated exception");
    Mockito.when(requestHandler.apply(Mockito.same(arg1)))
      .thenThrow(expectedException);

    configureRegistryLoggerSpyExceptionExpected(request, contextData, registryBodyRequestExtraInfoRetriever, registryBodyResponseExtraInfoExtractor, expectedException);

    // When
    RuntimeException result = assertThrows(RuntimeException.class, () -> registryLoggerSpy.execute1(
      contextData, request,
      requestHandler, exceptionHandler,
      registryBodyRequestExtraInfoRetriever, registryBodyResponseExtraInfoExtractor,
      arg1)
    );

    // Then
    Assertions.assertSame(expectedException, result);
  }

  @ParameterizedTest
  @CsvSource("request,response,ARG1")
  <I, O, A1> void givenHandledExceptionWhenExecute1ThenOk(I request, O expectedResult, A1 arg1) {
    // Given
    Function<A1, Triple<O, String, RegistryOutcome>> requestHandler = Mockito.mock();
    Function<Exception, O> exceptionHandler = Mockito.mock();
    Function<O, Map<String, Object>> registryBodyResponseExtraInfoExtractor = Mockito.mock();

    RegistryContextData contextData = new RegistryContextData();
    Supplier<Map<String, Object>> registryBodyRequestExtraInfoRetriever = Mockito.mock();

    RuntimeException expectedException = new RuntimeException("simulated exception");
    Mockito.when(requestHandler.apply(Mockito.same(arg1)))
      .thenThrow(expectedException);

    Mockito.when(exceptionHandler.apply(Mockito.same(expectedException)))
      .thenReturn(expectedResult);

    configureRegistryLoggerSpyNoExceptionExpected(request, expectedResult, contextData, registryBodyRequestExtraInfoRetriever, registryBodyResponseExtraInfoExtractor, null, RegistryOutcome.KO);

    // When
    O result = registryLoggerSpy.execute1(
      contextData, request,
      requestHandler, exceptionHandler,
      registryBodyRequestExtraInfoRetriever, registryBodyResponseExtraInfoExtractor,
      arg1);

    // Then
    Assertions.assertSame(expectedResult, result);
  }

  @ParameterizedTest
  @CsvSource("request,ARG1")
  <I, O, A1> void givenHandledExceptionThrowingExceptionWhenExecute1ThenThrowNestedException(I request, A1 arg1) {
    // Given
    Function<A1, Triple<O, String, RegistryOutcome>> requestHandler = Mockito.mock();
    Function<Exception, O> exceptionHandler = Mockito.mock();
    Function<O, Map<String, Object>> registryBodyResponseExtraInfoExtractor = Mockito.mock();

    RegistryContextData contextData = new RegistryContextData();
    Supplier<Map<String, Object>> registryBodyRequestExtraInfoRetriever = Mockito.mock();

    RuntimeException expectedException = new RuntimeException("simulated exception");
    Mockito.when(requestHandler.apply(Mockito.same(arg1)))
      .thenThrow(expectedException);

    RuntimeException expectedNestedException = new RuntimeException("simulated exception");
    Mockito.when(exceptionHandler.apply(Mockito.same(expectedException)))
      .thenThrow(expectedNestedException);

    configureRegistryLoggerSpyExceptionExpected(request, contextData, registryBodyRequestExtraInfoRetriever, registryBodyResponseExtraInfoExtractor, expectedNestedException);

    // When
    RuntimeException result = assertThrows(RuntimeException.class, () -> registryLoggerSpy.execute1(
      contextData, request,
      requestHandler, exceptionHandler,
      registryBodyRequestExtraInfoRetriever, registryBodyResponseExtraInfoExtractor,
      arg1)
    );

    // Then
    Assertions.assertSame(expectedNestedException, result);
  }
//endregion

//region execute2
  @ParameterizedTest
  @CsvSource("request,response,ARG1,ARG2")
  <I, O, A1, A2> void whenExecute2ThenOk(I request, O expectedResult, A1 arg1, A2 arg2) {
    // Given
    BiFunction<A1, A2, Triple<O, String, RegistryOutcome>> requestHandler = Mockito.mock();
    Function<Exception, O> exceptionHandler = Mockito.mock();
    Function<O, Map<String, Object>> registryBodyResponseExtraInfoExtractor = Mockito.mock();

    RegistryContextData contextData = new RegistryContextData();
    Supplier<Map<String, Object>> registryBodyRequestExtraInfoRetriever = Mockito.mock();

    String expectedIuv = "iuv";
    RegistryOutcome expectedOutcome = RegistryOutcome.OK;

    Mockito.when(requestHandler.apply(Mockito.same(arg1), Mockito.same(arg2)))
      .thenReturn(Triple.of(expectedResult, expectedIuv, expectedOutcome));

    configureRegistryLoggerSpyNoExceptionExpected(request, expectedResult, contextData, registryBodyRequestExtraInfoRetriever, registryBodyResponseExtraInfoExtractor, expectedIuv, expectedOutcome);

    // When
    O result = registryLoggerSpy.execute2(
      contextData, request,
      requestHandler, exceptionHandler,
      registryBodyRequestExtraInfoRetriever, registryBodyResponseExtraInfoExtractor,
      arg1, arg2);

    // Then
    Assertions.assertSame(expectedResult, result);
  }

  @ParameterizedTest
  @CsvSource("request,ARG1,ARG2")
  <I, O, A1, A2> void givenNotHandledExceptionWhenExecute2ThenThrowException(I request, A1 arg1, A2 arg2) {
    // Given
    BiFunction<A1, A2, Triple<O, String, RegistryOutcome>> requestHandler = Mockito.mock();
    Function<Exception, O> exceptionHandler = null;
    Function<O, Map<String, Object>> registryBodyResponseExtraInfoExtractor = Mockito.mock();

    RegistryContextData contextData = new RegistryContextData();
    Supplier<Map<String, Object>> registryBodyRequestExtraInfoRetriever = Mockito.mock();

    RuntimeException expectedException = new RuntimeException("simulated exception");
    Mockito.when(requestHandler.apply(Mockito.same(arg1), Mockito.same(arg2)))
      .thenThrow(expectedException);

    configureRegistryLoggerSpyExceptionExpected(request, contextData, registryBodyRequestExtraInfoRetriever, registryBodyResponseExtraInfoExtractor, expectedException);

    // When
    RuntimeException result = assertThrows(RuntimeException.class, () -> registryLoggerSpy.execute2(
      contextData, request,
      requestHandler, exceptionHandler,
      registryBodyRequestExtraInfoRetriever, registryBodyResponseExtraInfoExtractor,
      arg1, arg2)
    );

    // Then
    Assertions.assertSame(expectedException, result);
  }

  @ParameterizedTest
  @CsvSource("request,response,ARG1,ARG2")
  <I, O, A1, A2> void givenHandledExceptionWhenExecute2ThenOk(I request, O expectedResult, A1 arg1, A2 arg2) {
    // Given
    BiFunction<A1, A2, Triple<O, String, RegistryOutcome>> requestHandler = Mockito.mock();
    Function<Exception, O> exceptionHandler = Mockito.mock();
    Function<O, Map<String, Object>> registryBodyResponseExtraInfoExtractor = Mockito.mock();

    RegistryContextData contextData = new RegistryContextData();
    Supplier<Map<String, Object>> registryBodyRequestExtraInfoRetriever = Mockito.mock();

    RuntimeException expectedException = new RuntimeException("simulated exception");
    Mockito.when(requestHandler.apply(Mockito.same(arg1), Mockito.same(arg2)))
      .thenThrow(expectedException);

    Mockito.when(exceptionHandler.apply(Mockito.same(expectedException)))
      .thenReturn(expectedResult);

    configureRegistryLoggerSpyNoExceptionExpected(request, expectedResult, contextData, registryBodyRequestExtraInfoRetriever, registryBodyResponseExtraInfoExtractor, null, RegistryOutcome.KO);

    // When
    O result = registryLoggerSpy.execute2(
      contextData, request,
      requestHandler, exceptionHandler,
      registryBodyRequestExtraInfoRetriever, registryBodyResponseExtraInfoExtractor,
      arg1, arg2);

    // Then
    Assertions.assertSame(expectedResult, result);
  }

  @ParameterizedTest
  @CsvSource("request,ARG1,ARG2")
  <I, O, A1, A2> void givenHandledExceptionThrowingExceptionWhenExecute2ThenThrowNestedException(I request, A1 arg1, A2 arg2) {
    // Given
    BiFunction<A1, A2, Triple<O, String, RegistryOutcome>> requestHandler = Mockito.mock();
    Function<Exception, O> exceptionHandler = Mockito.mock();
    Function<O, Map<String, Object>> registryBodyResponseExtraInfoExtractor = Mockito.mock();

    RegistryContextData contextData = new RegistryContextData();
    Supplier<Map<String, Object>> registryBodyRequestExtraInfoRetriever = Mockito.mock();

    RuntimeException expectedException = new RuntimeException("simulated exception");
    Mockito.when(requestHandler.apply(Mockito.same(arg1), Mockito.same(arg2)))
      .thenThrow(expectedException);

    RuntimeException expectedNestedException = new RuntimeException("simulated exception");
    Mockito.when(exceptionHandler.apply(Mockito.same(expectedException)))
      .thenThrow(expectedNestedException);

    configureRegistryLoggerSpyExceptionExpected(request, contextData, registryBodyRequestExtraInfoRetriever, registryBodyResponseExtraInfoExtractor, expectedNestedException);

    // When
    RuntimeException result = assertThrows(RuntimeException.class, () -> registryLoggerSpy.execute2(
      contextData, request,
      requestHandler, exceptionHandler,
      registryBodyRequestExtraInfoRetriever, registryBodyResponseExtraInfoExtractor,
      arg1, arg2)
    );

    // Then
    Assertions.assertSame(expectedNestedException, result);
  }
//endregion

//region execute3
  @ParameterizedTest
  @CsvSource("request,response,ARG1,ARG2,ARG3")
  <I, O, A1, A2, A3> void whenExecute2ThenOk(I request, O expectedResult, A1 arg1, A2 arg2, A3 arg3) {
    // Given
    TriFunction<A1, A2, A3, Triple<O, String, RegistryOutcome>> requestHandler = Mockito.mock();
    Function<Exception, O> exceptionHandler = Mockito.mock();
    Function<O, Map<String, Object>> registryBodyResponseExtraInfoExtractor = Mockito.mock();

    RegistryContextData contextData = new RegistryContextData();
    Supplier<Map<String, Object>> registryBodyRequestExtraInfoRetriever = Mockito.mock();

    String expectedIuv = "iuv";
    RegistryOutcome expectedOutcome = RegistryOutcome.OK;

    Mockito.when(requestHandler.apply(Mockito.same(arg1), Mockito.same(arg2), Mockito.same(arg3)))
      .thenReturn(Triple.of(expectedResult, expectedIuv, expectedOutcome));

    configureRegistryLoggerSpyNoExceptionExpected(request, expectedResult, contextData, registryBodyRequestExtraInfoRetriever, registryBodyResponseExtraInfoExtractor, expectedIuv, expectedOutcome);

    // When
    O result = registryLoggerSpy.execute3(
      contextData, request,
      requestHandler, exceptionHandler,
      registryBodyRequestExtraInfoRetriever, registryBodyResponseExtraInfoExtractor,
      arg1, arg2, arg3);

    // Then
    Assertions.assertSame(expectedResult, result);
  }

  @ParameterizedTest
  @CsvSource("request,ARG1,ARG2,ARG3")
  <I, O, A1, A2, A3> void givenNotHandledExceptionWhenExecute2ThenThrowException(I request, A1 arg1, A2 arg2, A3 arg3) {
    // Given
    TriFunction<A1, A2, A3, Triple<O, String, RegistryOutcome>> requestHandler = Mockito.mock();
    Function<Exception, O> exceptionHandler = null;
    Function<O, Map<String, Object>> registryBodyResponseExtraInfoExtractor = Mockito.mock();

    RegistryContextData contextData = new RegistryContextData();
    Supplier<Map<String, Object>> registryBodyRequestExtraInfoRetriever = Mockito.mock();

    RuntimeException expectedException = new RuntimeException("simulated exception");
    Mockito.when(requestHandler.apply(Mockito.same(arg1), Mockito.same(arg2), Mockito.same(arg3)))
      .thenThrow(expectedException);

    configureRegistryLoggerSpyExceptionExpected(request, contextData, registryBodyRequestExtraInfoRetriever, registryBodyResponseExtraInfoExtractor, expectedException);

    // When
    RuntimeException result = assertThrows(RuntimeException.class, () -> registryLoggerSpy.execute3(
      contextData, request,
      requestHandler, exceptionHandler,
      registryBodyRequestExtraInfoRetriever, registryBodyResponseExtraInfoExtractor,
      arg1, arg2, arg3)
    );

    // Then
    Assertions.assertSame(expectedException, result);
  }

  @ParameterizedTest
  @CsvSource("request,response,ARG1,ARG2,ARG3")
  <I, O, A1, A2, A3> void givenHandledExceptionWhenExecute2ThenOk(I request, O expectedResult, A1 arg1, A2 arg2, A3 arg3) {
    // Given
    TriFunction<A1, A2, A3, Triple<O, String, RegistryOutcome>> requestHandler = Mockito.mock();
    Function<Exception, O> exceptionHandler = Mockito.mock();
    Function<O, Map<String, Object>> registryBodyResponseExtraInfoExtractor = Mockito.mock();

    RegistryContextData contextData = new RegistryContextData();
    Supplier<Map<String, Object>> registryBodyRequestExtraInfoRetriever = Mockito.mock();

    RuntimeException expectedException = new RuntimeException("simulated exception");
    Mockito.when(requestHandler.apply(Mockito.same(arg1), Mockito.same(arg2), Mockito.same(arg3)))
      .thenThrow(expectedException);

    Mockito.when(exceptionHandler.apply(Mockito.same(expectedException)))
      .thenReturn(expectedResult);

    configureRegistryLoggerSpyNoExceptionExpected(request, expectedResult, contextData, registryBodyRequestExtraInfoRetriever, registryBodyResponseExtraInfoExtractor, null, RegistryOutcome.KO);

    // When
    O result = registryLoggerSpy.execute3(
      contextData, request,
      requestHandler, exceptionHandler,
      registryBodyRequestExtraInfoRetriever, registryBodyResponseExtraInfoExtractor,
      arg1, arg2, arg3);

    // Then
    Assertions.assertSame(expectedResult, result);
  }

  @ParameterizedTest
  @CsvSource("request,ARG1,ARG2,ARG3")
  <I, O, A1, A2, A3> void givenHandledExceptionThrowingExceptionWhenExecute2ThenThrowNestedException(I request, A1 arg1, A2 arg2, A3 arg3) {
    // Given
    TriFunction<A1, A2, A3, Triple<O, String, RegistryOutcome>> requestHandler = Mockito.mock();
    Function<Exception, O> exceptionHandler = Mockito.mock();
    Function<O, Map<String, Object>> registryBodyResponseExtraInfoExtractor = Mockito.mock();

    RegistryContextData contextData = new RegistryContextData();
    Supplier<Map<String, Object>> registryBodyRequestExtraInfoRetriever = Mockito.mock();

    RuntimeException expectedException = new RuntimeException("simulated exception");
    Mockito.when(requestHandler.apply(Mockito.same(arg1), Mockito.same(arg2), Mockito.same(arg3)))
      .thenThrow(expectedException);

    RuntimeException expectedNestedException = new RuntimeException("simulated exception");
    Mockito.when(exceptionHandler.apply(Mockito.same(expectedException)))
      .thenThrow(expectedNestedException);

    configureRegistryLoggerSpyExceptionExpected(request, contextData, registryBodyRequestExtraInfoRetriever, registryBodyResponseExtraInfoExtractor, expectedNestedException);

    // When
    RuntimeException result = assertThrows(RuntimeException.class, () -> registryLoggerSpy.execute3(
      contextData, request,
      requestHandler, exceptionHandler,
      registryBodyRequestExtraInfoRetriever, registryBodyResponseExtraInfoExtractor,
      arg1, arg2, arg3)
    );

    // Then
    Assertions.assertSame(expectedNestedException, result);
  }
//endregion
}
