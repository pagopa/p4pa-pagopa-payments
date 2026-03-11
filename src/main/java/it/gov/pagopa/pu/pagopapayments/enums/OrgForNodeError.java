package it.gov.pagopa.pu.pagopapayments.enums;

import it.gov.pagopa.pu.pagopapayments.exception.OrgForNodeException;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum OrgForNodeError {

  ODP_101(HttpStatus.BAD_REQUEST, "ODP-101", "PAA_SINTASSI Errore di sintassi del modello nella richiesta"),
  ODP_103(HttpStatus.INTERNAL_SERVER_ERROR, "ODP-103", "PAA_SYSTEM_ERROR Errore generico"),
  ODP_107(HttpStatus.NOT_FOUND, "ODP-107", "PAA_PAGAMENTO_SCONOSCIUTO Errore per pagamento sconosciuto"),
  ODP_108(HttpStatus.CONFLICT, "ODP-108", "PAA_PAGAMENTO_DUPLICATO Errore per pagamento duplicato"),
  ODP_110(HttpStatus.UNPROCESSABLE_ENTITY, "ODP-110", "PAA_PAGAMENTO_SCADUTO Errore per pagamento scaduto");

  private final HttpStatus httpStatus;
  private final String appErrorCode;
  private final String errorMessage;

  OrgForNodeError(HttpStatus httpStatus, String appErrorCode, String errorMessage) {
    this.httpStatus = httpStatus;
    this.appErrorCode = appErrorCode;
    this.errorMessage = errorMessage;
  }

  public OrgForNodeException toException() {
    return new OrgForNodeException(this);
  }
}
