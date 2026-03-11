package it.gov.pagopa.pu.pagopapayments.exception;

import it.gov.pagopa.pu.pagopapayments.enums.OrgForNodeError;
import lombok.Getter;

@Getter
public class OrgForNodeException extends RuntimeException {

  private final OrgForNodeError error;

  public OrgForNodeException(OrgForNodeError error) {
    super(error.getErrorMessage());
    this.error = error;
  }
}
