package it.gov.pagopa.pu.pagopapayments.util;

public class Utilities {

  private Utilities() {
  }

  public static boolean isFieldValid(String field) {
    return field != null && !field.isBlank();
  }
}
