package it.gov.pagopa.pu.pagopapayments.performancelogger;

import io.vavr.CheckedFunction1;
import io.vavr.CheckedFunction3;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.springframework.util.function.ThrowingFunction;
import org.springframework.util.function.ThrowingSupplier;

import java.util.function.Function;

/**
 * Utility class to produce performance log
 */
@Slf4j
public final class PerformanceLogger {
  private PerformanceLogger() {
  }

  private static final PerformanceLoggerThresholdLevels defaultThresholdLevels = new PerformanceLoggerThresholdLevels(60, 300);
  private static final Function<Throwable, RuntimeException> exceptionWrapper = RuntimeException::new;

  /**
   * It will execute the provided logic printing the timing required to execute it.
   *
   * @param appenderName    The name of the appender which could be used to set logging level
   * @param contextData     A string printed together with the performance log in order to identify it
   * @param logic           The logic to execute and take its time
   * @param payloadBuilder  An optional function which till take the output of the invoked logic in order to print a payload after the performance log
   * @param thresholdLevels An optional object to configure the log level based on the logic duration (if not provided, it will use {@link #defaultThresholdLevels}
   * @return The object returned by the monitored logic
   */
  public static <T> T execute(
    String appenderName, String contextData,
    ThrowingSupplier<T> logic, ThrowingFunction<T, String> payloadBuilder,
    PerformanceLoggerThresholdLevels thresholdLevels) {
    long startTime = System.currentTimeMillis();
    String payload = "";
    try {
      T out = logic.get();
      payload = buildPayload(out, payloadBuilder);
      return out;
    } catch (Exception e) {
      payload = buildExceptionPayload(e);
      throw e;
    } finally {
      log(appenderName, contextData, startTime, payload, thresholdLevels);
    }
  }

  /**
   * Overloaded method of {@link #execute(String, String, ThrowingSupplier, ThrowingFunction, PerformanceLoggerThresholdLevels)} used to avoid ThrowingSupplier object creation
   */
  public static <A1, T> T execute1(
    String appenderName, String contextData,
    CheckedFunction1<A1, T> logic, ThrowingFunction<T, String> payloadBuilder,
    PerformanceLoggerThresholdLevels thresholdLevels,
    A1 arg1) {
    long startTime = System.currentTimeMillis();
    String payload = "";
    try {
      T out = logic.apply(arg1);
      payload = buildPayload(out, payloadBuilder);
      return out;
    } catch (RuntimeException e) {
      payload = buildExceptionPayload(e);
      throw e;
    } catch (Throwable e) {
      payload = buildExceptionPayload(e);
      throw exceptionWrapper.apply(e);
    } finally {
      log(appenderName, contextData, startTime, payload, thresholdLevels);
    }
  }

  /**
   * Overloaded method of {@link #execute(String, String, ThrowingSupplier, ThrowingFunction, PerformanceLoggerThresholdLevels)} used to avoid ThrowingSupplier object creation
   */
  @SuppressWarnings("squid:S107") // suppressing too many parameters exception: method introduced to reduce object creation
  public static <A1, A2, A3, T> T execute3(
    String appenderName, String contextData,
    CheckedFunction3<A1, A2, A3, T> logic, ThrowingFunction<T, String> payloadBuilder,
    PerformanceLoggerThresholdLevels thresholdLevels,
    A1 arg1, A2 arg2, A3 arg3) {
    long startTime = System.currentTimeMillis();
    String payload = "";
    try {
      T out = logic.apply(arg1,  arg2, arg3);
      payload = buildPayload(out, payloadBuilder);
      return out;
    } catch (RuntimeException e) {
      payload = buildExceptionPayload(e);
      throw e;
    } catch (Throwable e) {
      payload = buildExceptionPayload(e);
      throw exceptionWrapper.apply(e);
    } finally {
      log(appenderName, contextData, startTime, payload, thresholdLevels);
    }
  }

  private static <T> String buildPayload(T out, Function<T, String> payloadBuilder) {
    String payload;
    if (payloadBuilder != null) {
      if (out != null) {
        try {
          payload = payloadBuilder.apply(out);
        } catch (Exception e) {
          log.warn("Something went wrong while building payload", e);
          payload = "Payload builder thrown Exception %s: %s".formatted(e.getClass(), e.getMessage());
        }
      } else {
        payload = "Returned null";
      }
    } else {
      payload = "";
    }
    return payload;
  }

  private static String buildExceptionPayload(Throwable e) {
    return "Exception %s: %s".formatted(e.getClass(), e.getMessage());
  }

  public static void log(String appenderName, String contextData, long startTime, String payload, PerformanceLoggerThresholdLevels thresholdLevels) {
    long durationMillis = System.currentTimeMillis() - startTime;
    Level level = resolveLevel(durationMillis, thresholdLevels);
    LoggerFactory.getLogger("PERFORMANCE_LOG." + appenderName)
      .atLevel(level)
      .log(
        "{}Time occurred to perform business logic: {} ms. {}",
        contextData != null ? "[" + contextData + "] " : "",
        durationMillis,
        payload);
  }

  static Level resolveLevel(long durationMillis, PerformanceLoggerThresholdLevels thresholdLevels) {
    long durationSeconds = durationMillis / 1000;
    thresholdLevels = ObjectUtils.firstNonNull(thresholdLevels, defaultThresholdLevels);
    if (durationSeconds < thresholdLevels.getWarn()) {
      return Level.INFO;
    } else if (durationSeconds < thresholdLevels.getError()) {
      return Level.WARN;
    } else {
      return Level.ERROR;
    }
  }
}
