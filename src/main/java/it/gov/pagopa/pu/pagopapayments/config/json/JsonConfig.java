package it.gov.pagopa.pu.pagopapayments.config.json;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import it.gov.pagopa.pu.pagopapayments.config.json.jackson3.LocalDateTimeToOffsetDateTimeJackson3Deserializer;
import it.gov.pagopa.pu.pagopapayments.config.json.jackson3.LocalDateTimeToOffsetDateTimeJackson3Serializer;
import it.gov.pagopa.pu.pagopapayments.config.json.jackson3.OffsetDateTimeToLocalDateTimeJackson3Deserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import tools.jackson.databind.JacksonModule;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.TimeZone;

@Configuration
@EnableSpringDataWebSupport
public class JsonConfig {

  @Bean
  public ObjectMapper objectMapper() {
    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(configureDateTimeModule());
    mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
    mapper.setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.PUBLIC_ONLY);
    mapper.setVisibility(PropertyAccessor.IS_GETTER, JsonAutoDetect.Visibility.PUBLIC_ONLY);
    mapper.setVisibility(PropertyAccessor.SETTER, JsonAutoDetect.Visibility.PUBLIC_ONLY);
    mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
    mapper.setVisibility(PropertyAccessor.CREATOR, JsonAutoDetect.Visibility.ANY);
    mapper.setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL);
    mapper.setTimeZone(TimeZone.getDefault());
    return mapper;
  }

  /**
   * openApi is documenting LocalDateTime as date-time, which is interpreted as an OffsetDateTime by openApiGenerator
   */
  private static SimpleModule configureDateTimeModule() {
    return new JavaTimeModule()
      .addSerializer(LocalDateTime.class, new LocalDateTimeToOffsetDateTimeSerializer())
      .addDeserializer(LocalDateTime.class, new OffsetDateTimeToLocalDateTimeDeserializer())
      .addDeserializer(OffsetDateTime.class, new LocalDateTimeToOffsetDateTimeDeserializer());
  }

  @Bean
  public JsonMapper objectMapperJackson3() {
    return JsonMapper.builder()
      .addModule(configureJackson3DateTimeModule())
      .configure(tools.jackson.databind.SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
      .configure(tools.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
      .changeDefaultPropertyInclusion(incl -> incl.withValueInclusion(JsonInclude.Include.NON_NULL))
      .defaultTimeZone(TimeZone.getDefault())
      .build();
  }

  /**
   * openApi is documenting LocalDateTime as date-time, which is interpreted as an OffsetDateTime by openApiGenerator
   */
  private static JacksonModule configureJackson3DateTimeModule() {
    return new tools.jackson.databind.module.SimpleModule()
      .addSerializer(LocalDateTime.class, new LocalDateTimeToOffsetDateTimeJackson3Serializer())
      .addDeserializer(LocalDateTime.class, new OffsetDateTimeToLocalDateTimeJackson3Deserializer())
      .addDeserializer(OffsetDateTime.class, new LocalDateTimeToOffsetDateTimeJackson3Deserializer());
  }
}
