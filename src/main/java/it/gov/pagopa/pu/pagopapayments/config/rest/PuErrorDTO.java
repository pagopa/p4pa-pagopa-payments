package it.gov.pagopa.pu.pagopapayments.config.rest;

import it.gov.pagopa.pu.pagopapayments.dto.generated.ErrorFieldDTO;

import java.util.List;

public record PuErrorDTO(
  String category,
  String code,
  String message,
  List<ErrorFieldDTO> fields
) {
}
