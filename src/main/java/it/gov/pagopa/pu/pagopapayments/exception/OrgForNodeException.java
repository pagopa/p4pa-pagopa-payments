package it.gov.pagopa.pu.pagopapayments.exception;

import it.gov.pagopa.pu.pagopapayments.enums.OrgForNodeError;
import it.gov.pagopa.pu.pagopapayments.exception.common.BaseBusinessException;
import lombok.Getter;

@Getter
public class OrgForNodeException extends BaseBusinessException {

  private final OrgForNodeError error;

  public OrgForNodeException(OrgForNodeError error) {
    super(error.getAppErrorCode(), error.getErrorMessage());
    this.error = error;
  }
}
