package it.gov.pagopa.pu.pagopapayments;

import io.github.springwolf.core.asyncapi.schemas.converters.SchemaTitleModelConverter;
import io.swagger.v3.core.converter.ModelConverters;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springdoc.core.properties.SpringDocConfigProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.MockMvcPrint;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.json.JsonAssert;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(print = MockMvcPrint.NONE)
@TestPropertySource(properties = {
  "logging.level.org.springdoc.core.utils.SpringDocAnnotationsUtils=OFF",
  "springdoc.api-docs.enabled=true",
  "springdoc.swagger-ui.enabled=false",
  "springwolf.enabled=false",
})
@Slf4j
class OpenApiGeneratorTest {

  @Autowired
  private MockMvc mockMvc;

  @Value("${springdoc.api-docs.version}")
  private String apiDocsVersion;

  @BeforeEach
  void init() {
    // removing ModelConverters configured by SpringWolf which will cause the setting of the title in each schema
    boolean openapi31 = apiDocsVersion.equalsIgnoreCase(SpringDocConfigProperties.ApiDocs.OpenApiVersion.OPENAPI_3_1.toString());
    ModelConverters modelConverters = ModelConverters.getInstance(openapi31);
    modelConverters.getConverters().stream()
      .filter(SchemaTitleModelConverter.class::isInstance)
      .forEach(modelConverters::removeConverter);
  }

  @Test
  void generateAndVerifyCommit() throws Exception {
    MvcResult result = mockMvc.perform(
      get("/v3/api-docs")
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
    ).andExpect(status().isOk())
      .andReturn();

    String openApiResult = result.getResponse().getContentAsString()
      .replace("\r", "");

    Assertions.assertTrue(openApiResult.startsWith("{\n  \"openapi\" : \"3."));

    Path openApiGeneratedPath = Path.of("openapi/generated.openapi.json");
    boolean toStore=true;
    if(Files.exists(openApiGeneratedPath)){
      String storedOpenApi = Files.readString(openApiGeneratedPath);
      try {
        JsonAssert.comparator(JsonCompareMode.STRICT).assertIsMatch(storedOpenApi, openApiResult);
        toStore=false;
      } catch (Throwable e){
        log.info("Observed the following changes: {}", e.getMessage());
      }
    }
    if(toStore){
      Files.writeString(openApiGeneratedPath, openApiResult, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    String gitStatus = execCmd("git", "status");
    Assertions.assertFalse(gitStatus.contains("openapi/generated.openapi.json"), "Generated OpenApi not committed");
  }

  public static String execCmd(String... cmd) throws java.io.IOException {
    java.util.Scanner s = new java.util.Scanner(Runtime.getRuntime().exec(cmd).getInputStream()).useDelimiter("\\A");
    return s.hasNext() ? s.next() : "";
  }
}
