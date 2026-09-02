package it.gov.pagopa.pu.pagopapayments.exception.transcoder.handler;

import it.gov.pagopa.pu.pagopapayments.dto.generated.ErrorFieldDTO;
import it.gov.pagopa.pu.pagopapayments.exception.transcoder.ExceptionMessageTranscoded;
import it.gov.pagopa.pu.pagopapayments.util.TestUtils;
import jakarta.validation.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public class ConstraintViolationExceptionMessageTranscoderTest {

  public static final ExceptionMessageTranscoded EXPECTED_CONSTRAINT_EXCEPTION_MESSAGE_TRANSCODED = new ExceptionMessageTranscoded(
    "PAGOPA_PAYMENTS_BAD_REQUEST",
    "Invalid request content. digitsField: valore numerico fuori dai limiti (previsto <5 digits>.<2 digits>); nonNullableField: non deve essere null; notBlankField: non deve essere spazio; notEmptyCollectionField: non deve essere vuoto; regexpValidatedField: deve corrispondere a \"[0-9]+\"",
    List.of(
      new ErrorFieldDTO(
        "digitsField", "Digits", "valore numerico fuori dai limiti (previsto <5 digits>.<2 digits>)"
      ),
      new ErrorFieldDTO(
        "nonNullableField", "NotNull", "non deve essere null"
      ),
      new ErrorFieldDTO(
        "notBlankField", "NotBlank", "non deve essere spazio"
      ),
      new ErrorFieldDTO(
        "notEmptyCollectionField", "NotEmpty", "non deve essere vuoto"
      ),
      new ErrorFieldDTO(
        "regexpValidatedField", "Pattern", "deve corrispondere a \"[0-9]+\""
      )
    )
  );

  private final ConstraintViolationExceptionMessageTranscoder transcoder = new ConstraintViolationExceptionMessageTranscoder();

  @Data
  private static class SampleValidatedDTO {
    private String nullableField;
    @NotNull
    private String nonNullableField;
    @Pattern(regexp = "[0-9]+")
    private String regexpValidatedField = "qwerty";
    @Digits(integer = 5, fraction = 2)
    private String digitsField = "123456.789";
    @NotEmpty
    private Collection<String> notEmptyCollectionField = List.of();
    @NotBlank
    private String notBlankField = "";
  }

  @BeforeEach
  void init() {
    TestUtils.clearLocale();
  }

  public static ConstraintViolationException buildConstraintViolationException() {
    try (ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory()) {
      Validator validator = validatorFactory.getValidator();
      Set<ConstraintViolation<SampleValidatedDTO>> violations = validator.validate(new SampleValidatedDTO());
      return new ConstraintViolationException(violations);
    }
  }

  @Test
  void testTranscode() {
    // Given
    ConstraintViolationException constraintViolationException = buildConstraintViolationException();

    // When
    ExceptionMessageTranscoded result = transcoder.transcode(constraintViolationException);

    // Then
    Assertions.assertEquals(
      EXPECTED_CONSTRAINT_EXCEPTION_MESSAGE_TRANSCODED,
      result
    );
  }
}
