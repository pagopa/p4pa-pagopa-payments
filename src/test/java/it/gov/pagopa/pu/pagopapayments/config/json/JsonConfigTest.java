package it.gov.pagopa.pu.pagopapayments.config.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.pu.pagopapayments.util.TestUtils;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

class JsonConfigTest {

  private final JsonConfig jsonConfig = new JsonConfig();

  private final ObjectMapper j2ObjectMapper = jsonConfig.objectMapper();
  private final JsonMapper j3JsonMapper = jsonConfig.objectMapperJackson3();

  @Data
  @NoArgsConstructor
  @JsonPropertyOrder({"name", "nullField", "nonNullableNullField", "value", "dateTime", "offsetDateTime"})
  public static class SampleDTO {
    public String name;
    private String nullField;
    @JsonInclude(JsonInclude.Include.ALWAYS)
    private String nonNullableNullField;
    public Integer value;
    public LocalDateTime dateTime;
    public OffsetDateTime offsetDateTime;
  }

  @BeforeEach
  void init(){
      TestUtils.clearDefaultTimezone();
  }

  @Test
  void testJackson2Jackson3ConfigurationAlignment() throws JsonProcessingException {
    // Given
    SampleDTO dto = new SampleDTO();
    dto.setName("NAME");
    dto.setValue(42);
    dto.setDateTime(LocalDateTime.now());
    dto.setOffsetDateTime(OffsetDateTime.now());

    // When
    String j2Serialized = j2ObjectMapper.writeValueAsString(dto);
    String j3Serialized = j3JsonMapper.writeValueAsString(dto);

    // Then
    Assertions.assertEquals(j2Serialized, j3Serialized);

    // When deserialized
    SampleDTO j2Deserialized = j2ObjectMapper.readValue(j2Serialized, SampleDTO.class);
    SampleDTO j3Deserialized = j3JsonMapper.readValue(j2Serialized, SampleDTO.class);

    Assertions.assertEquals(dto, j2Deserialized);
    Assertions.assertEquals(dto, j3Deserialized);
  }

  @Test
  void testDateConversions() throws JsonProcessingException {
    // Given
    String json = """
      {
          "dateTime": "2025-12-22T17:17:39.940891+00:00",
          "name": "NAME",
          "nonNullableNullField": null,
          "offsetDateTime": "2025-12-22T18:17:39.941234",
          "value": 42
      }
      """;

    SampleDTO expectedResult = new SampleDTO();
    expectedResult.setName("NAME");
    expectedResult.setValue(42);
    expectedResult.setDateTime(LocalDateTime.of(2025, 12, 22, 18, 17, 39, 940891000));
    expectedResult.setOffsetDateTime(OffsetDateTime.of(2025, 12, 22, 18, 17, 39, 941234000, ZoneOffset.of("+01:00")));

    // When
    SampleDTO j2Deserialized = j2ObjectMapper.readValue(json, SampleDTO.class);
    SampleDTO j3Deserialized = j3JsonMapper.readValue(json, SampleDTO.class);

    // Then
    Assertions.assertEquals(expectedResult, j2Deserialized);
    Assertions.assertEquals(expectedResult, j3Deserialized);
  }
}
