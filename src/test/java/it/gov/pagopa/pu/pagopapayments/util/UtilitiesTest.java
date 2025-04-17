package it.gov.pagopa.pu.pagopapayments.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UtilitiesTest {

  @Test
  void givenValidInputWhenIsFieldValidThenTrue() {
    assertTrue(Utilities.isFieldValid("valid input"));
  }

  @Test
  void givenNullWhenIsFieldValidThenFalse() {
    assertFalse(Utilities.isFieldValid(null));
  }

  @Test
  void givenEmptyInputWhenIsFieldValidThenFalse() {
    assertFalse(Utilities.isFieldValid(""));
  }
}
