package it.gov.pagopa.pu.pagopapayments.connector.auth.mapper;

import it.gov.pagopa.pu.auth.dto.generated.AuthErrorDTO;
import it.gov.pagopa.pu.pagopapayments.config.rest.PuErrorDTO;
import it.gov.pagopa.pu.pagopapayments.dto.generated.ErrorFieldDTO;
import it.gov.pagopa.pu.pagopapayments.util.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;

class AuthErrorDTOMapperTest {

  @Test
  void whenMapThenReturnPuErrorDTO() {
    // Given
    AuthErrorDTO errorDTO = TestUtils.getPodamFactory().manufacturePojo(AuthErrorDTO.class);
    Objects.requireNonNull(errorDTO.getFields());

    // When
    PuErrorDTO result = AuthErrorDTOMapper.map(errorDTO);

    // Then
    TestUtils.checkNotNullFields(result);

    Assertions.assertEquals(errorDTO.getError().getValue(), result.category());
    Assertions.assertEquals(errorDTO.getCode(), result.code());
    Assertions.assertEquals(errorDTO.getErrorDescription(), result.message());

    Assertions.assertEquals(5, result.fields().size());
    List<ErrorFieldDTO> fields = result.fields();
    for (int i = 0; i < fields.size(); i++) {
      ErrorFieldDTO ef = fields.get(i);
      TestUtils.checkNotNullFields(ef);

      it.gov.pagopa.pu.auth.dto.generated.ErrorFieldDTO expectedErrorFieldDTO = errorDTO.getFields().get(i);
      Assertions.assertEquals(expectedErrorFieldDTO.getField(), ef.getField());
      Assertions.assertEquals(expectedErrorFieldDTO.getError(), ef.getError());
      Assertions.assertEquals(expectedErrorFieldDTO.getMessage(), ef.getMessage());
    }

  }

}
