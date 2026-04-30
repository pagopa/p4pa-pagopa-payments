package it.gov.pagopa.pu.pagopapayments.util;

import java.time.ZoneId;
import java.util.TimeZone;

public class Constants {

  private Constants(){}

  public static final ZoneId ZONEID = ZoneId.of("Europe/Rome");
  public static final TimeZone DEFAULT_TIMEZONE = TimeZone.getTimeZone(ZONEID);

  public static final String WORKFLOW_STATUS_COMPLETED_VALUE = "WORKFLOW_EXECUTION_STATUS_COMPLETED";

  public static final String SPONTANEOUS_PSP_DP_TYPE_ORG_CODE = "SPONTANEOUS_PSP";

  public static final String INVALID_EC_FISCAL_CODE_ERROR_CODE = "FDR-2008";

}
