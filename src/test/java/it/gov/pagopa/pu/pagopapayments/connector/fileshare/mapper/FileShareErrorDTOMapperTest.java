package it.gov.pagopa.pu.pagopapayments.connector.fileshare.mapper;

import it.gov.pagopa.pu.fileshare.dto.generated.FileshareErrorDTO;
import it.gov.pagopa.pu.pagopapayments.config.rest.PuErrorDTO;
import it.gov.pagopa.pu.pagopapayments.dto.generated.ErrorFieldDTO;
import it.gov.pagopa.pu.pagopapayments.util.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;

class FileShareErrorDTOMapperTest {

  @Test
  void whenMapThenReturnPuErrorDTO() {
    // Given
    FileshareErrorDTO errorDTO = TestUtils.getPodamFactory().manufacturePojo(FileshareErrorDTO.class);
    Objects.requireNonNull(errorDTO.getFields());

    // When
    PuErrorDTO result = FileShareErrorDTOMapper.map(errorDTO);

    // Then
    TestUtils.checkNotNullFields(result);

    Assertions.assertEquals(errorDTO.getCategory().getValue(), result.category());
    Assertions.assertEquals(errorDTO.getCode(), result.code());
    Assertions.assertEquals(errorDTO.getMessage(), result.message());

    Assertions.assertEquals(5, result.fields().size());
    List<ErrorFieldDTO> fields = result.fields();
    for (int i = 0; i < fields.size(); i++) {
      ErrorFieldDTO ef = fields.get(i);
      TestUtils.checkNotNullFields(ef);

      it.gov.pagopa.pu.fileshare.dto.generated.ErrorFieldDTO expectedErrorFieldDTO = errorDTO.getFields().get(i);
      Assertions.assertEquals(expectedErrorFieldDTO.getField(), ef.getField());
      Assertions.assertEquals(expectedErrorFieldDTO.getError(), ef.getError());
      Assertions.assertEquals(expectedErrorFieldDTO.getMessage(), ef.getMessage());
    }

  }

}
