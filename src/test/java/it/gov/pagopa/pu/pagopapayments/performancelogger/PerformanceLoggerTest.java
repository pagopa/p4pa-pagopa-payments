package it.gov.pagopa.pu.pagopapayments.performancelogger;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import it.gov.pagopa.payhub.auth.utils.MemoryAppender;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

public class PerformanceLoggerTest {
    public static final String APPENDER_NAME = "APPENDER";
    public static final String CONTEXT_DATA = "TEST";

    private MemoryAppender memoryAppender;

    @BeforeEach
    public void setupMemoryAppender() {
        this.memoryAppender = buildPerformanceLoggerMemoryAppender(APPENDER_NAME);
    }

    static MemoryAppender buildPerformanceLoggerMemoryAppender(String appender) {
        ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger("PERFORMANCE_LOG."+appender);
        MemoryAppender memoryAppender = new MemoryAppender();
        memoryAppender.setContext((LoggerContext) LoggerFactory.getILoggerFactory());
        logger.setLevel(ch.qos.logback.classic.Level.INFO);
        logger.addAppender(memoryAppender);
        memoryAppender.start();
        return memoryAppender;
    }

    @Test
    void givenExceptionInBusinessLogicWhenThenLogExceptionInfo(){
        // Given
        RuntimeException expectedException = new RuntimeException("DUMMY");

        // When
        RuntimeException result = Assertions.assertThrows(RuntimeException.class, () -> PerformanceLogger.execute(APPENDER_NAME, CONTEXT_DATA, () -> {
            throw expectedException;
        }, null, null));

        //Then
        Assertions.assertSame(expectedException, result);
        assertPerformanceLogMessage(APPENDER_NAME, CONTEXT_DATA, "Exception class java.lang.RuntimeException: DUMMY", memoryAppender);
    }

    @Test
    void givenExceptionInPayloadBuilderWhenThenLogExceptionInfo(){
        // Given
        Object expectedResult = new Object();

        // When
        Object result = PerformanceLogger.execute(APPENDER_NAME, CONTEXT_DATA, () -> expectedResult, x -> {
            throw new RuntimeException("PayloadBuilder is receiving the expected parameter? " + (x == expectedResult));
        }, null);

        //Then
        Assertions.assertSame(expectedResult, result);
        assertPerformanceLogMessage(APPENDER_NAME, CONTEXT_DATA, "Payload builder thrown Exception class java.lang.RuntimeException: PayloadBuilder is receiving the expected parameter? true", memoryAppender);
    }

    @Test
    void givenNoPayloadBuilderWhenThenLogExceptionInfo(){
        // Given
        Object expectedResult = new Object();

        // When
        Object result = PerformanceLogger.execute(APPENDER_NAME, CONTEXT_DATA, () -> expectedResult, null, null);

        //Then
        Assertions.assertSame(expectedResult, result);
        assertPerformanceLogMessage(APPENDER_NAME, CONTEXT_DATA, "", memoryAppender);
    }

    @Test
    void givenPayloadBuilderReturningNullWhenThenLogExceptionInfo(){
        // Given
        Object expectedResult = new Object();

        // When
        Object result = PerformanceLogger.execute(APPENDER_NAME, CONTEXT_DATA, () -> expectedResult, x -> null, null);

        //Then
        Assertions.assertSame(expectedResult, result);
        assertPerformanceLogMessage(APPENDER_NAME, CONTEXT_DATA, "null", memoryAppender);
    }

    @Test
    void givenPayloadBuilderAndNullOutputReturningNullWhenThenLogExceptionInfo(){
        // When
        Object result = PerformanceLogger.execute(APPENDER_NAME, CONTEXT_DATA, () -> null, x -> null, null);

        //Then
        Assertions.assertNull(result);
        assertPerformanceLogMessage(APPENDER_NAME, CONTEXT_DATA, "Returned null", memoryAppender);
    }

    @Test
    void testThresholdLevelTranscoding(){
        PerformanceLoggerThresholdLevels thresholdLevels = new PerformanceLoggerThresholdLevels(1, 2);

        Assertions.assertEquals(Level.INFO, PerformanceLogger.resolveLevel(0, thresholdLevels));
        Assertions.assertEquals(Level.WARN, PerformanceLogger.resolveLevel(1000, thresholdLevels));
        Assertions.assertEquals(Level.ERROR, PerformanceLogger.resolveLevel(2000, thresholdLevels));
    }

    public static void assertPerformanceLogMessage(String expectedAppenderName, String expectedContextData, String expectedPayload, MemoryAppender memoryAppender) {
        Assertions.assertEquals(1, memoryAppender.getLoggedEvents().size());
        ILoggingEvent event = memoryAppender.getLoggedEvents().getFirst();
        Assertions.assertEquals("PERFORMANCE_LOG." + expectedAppenderName, event.getLoggerName());
        String logMessage = event.getFormattedMessage();
        Assertions.assertTrue(
                logMessage.matches(
                        "\\[%s] Time occurred to perform business logic: \\d+ ms\\. .*".formatted(expectedContextData)
                ) &&
                        logMessage.endsWith(". " + expectedPayload),
                "Unexpected logged message: " + logMessage
        );
    }
}
