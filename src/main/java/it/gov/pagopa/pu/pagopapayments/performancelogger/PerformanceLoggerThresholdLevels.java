package it.gov.pagopa.pu.pagopapayments.performancelogger;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PerformanceLoggerThresholdLevels {
    /** Number of seconds from which the log will be printed as a WARN */
    private long warn;
    /** Number of seconds from which the log will be printed as an ERROR */
    private long error;
}
