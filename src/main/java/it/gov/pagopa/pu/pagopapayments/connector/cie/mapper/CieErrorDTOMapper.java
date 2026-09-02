package it.gov.pagopa.pu.pagopapayments.connector.cie.mapper;

import it.gov.pagopa.pu.cie.dto.generated.ErrorDTO;
import it.gov.pagopa.pu.pagopapayments.config.rest.PuErrorDTO;
import it.gov.pagopa.pu.pagopapayments.dto.generated.ErrorFieldDTO;

public class CieErrorDTOMapper {

  private CieErrorDTOMapper() {
    /* This utility class should not be instantiated */
  }


  public static PuErrorDTO map(ErrorDTO errorDTO) {
    return new PuErrorDTO(
      errorDTO.getCategory().getValue(),
      errorDTO.getCode(),
      errorDTO.getMessage(),
      errorDTO.getFields() != null
        ? errorDTO.getFields().stream()
        .map(field -> new ErrorFieldDTO(
          field.getField(),
          field.getError(),
          field.getMessage()
        ))
        .toList()
        : null
    );
  }
}
