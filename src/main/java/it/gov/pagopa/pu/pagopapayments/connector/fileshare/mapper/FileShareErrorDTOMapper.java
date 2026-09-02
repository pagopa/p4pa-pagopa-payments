package it.gov.pagopa.pu.pagopapayments.connector.fileshare.mapper;

import it.gov.pagopa.pu.fileshare.dto.generated.FileshareErrorDTO;
import it.gov.pagopa.pu.pagopapayments.config.rest.PuErrorDTO;
import it.gov.pagopa.pu.pagopapayments.dto.generated.ErrorFieldDTO;

public class FileShareErrorDTOMapper {

  private FileShareErrorDTOMapper() {
    /* This utility class should not be instantiated */
  }


  public static PuErrorDTO map(FileshareErrorDTO errorDTO) {
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
