package it.gov.pagopa.pu.pagopapayments.exception.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.pu.pagopapayments.config.json.JsonConfig;
import it.gov.pagopa.pu.pagopapayments.dto.generated.ErrorFieldDTO;
import it.gov.pagopa.pu.pagopapayments.exception.PagopaPaymentsExceptionHandler;
import it.gov.pagopa.pu.pagopapayments.exception.transcoder.handler.ConstraintViolationExceptionMessageTranscoderTest;
import it.gov.pagopa.pu.pagopapayments.util.TestUtils;
import it.gov.pagopa.pu.pagopapayments.util.UtilitiesTest;
import jakarta.servlet.ServletException;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.HttpHostConnectException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.server.ServerErrorException;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import java.time.LocalDateTime;
import java.util.List;

import static it.gov.pagopa.pu.pagopapayments.exception.transcoder.handler.ConstraintViolationExceptionMessageTranscoderTest.EXPECTED_CONSTRAINT_EXCEPTION_MESSAGE_TRANSCODED;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@ExtendWith({SpringExtension.class})
@WebMvcTest(value = {CommonExceptionHandlerTest.TestController.class})
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = {
  CommonExceptionHandlerTest.TestController.class,
  PagopaPaymentsExceptionHandler.class,
  JsonConfig.class})
public abstract class CommonExceptionHandlerTest {

  public static final String DATA = "data";
  public static final TestRequestBody BODY = new TestRequestBody("bodyData", null, "abc", LocalDateTime.now());

  @Autowired
  protected MockMvc mockMvc;
  @Autowired
  protected ObjectMapper objectMapper;

  @MockitoSpyBean
  protected TestController testControllerSpy;
  @MockitoSpyBean
  protected RequestMappingHandlerAdapter requestMappingHandlerAdapterSpy;

  @RestController
  @Slf4j
  protected static class TestController {
    @PostMapping(value = "/test", produces = MediaType.APPLICATION_JSON_VALUE)
    public String testEndpoint(@RequestParam(DATA) String data, @Valid @RequestBody TestRequestBody body) {
      return "OK";
    }
  }

  protected final String traceId = "TRACEID";

  @BeforeEach
  void init() {
    TestUtils.clearDefaultTimezone();
    UtilitiesTest.setTraceId(traceId);
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class TestRequestBody {
    @NotNull
    private String requiredField;
    private String notRequiredField;
    @Pattern(regexp = "[a-z]+")
    private String lowerCaseAlphabeticField;
    private LocalDateTime dateTimeField;
  }

  protected ResultActions performRequest(String data, MediaType accept) throws Exception {
    return performRequest(data, accept, objectMapper.writeValueAsString(CommonExceptionHandlerTest.BODY));
  }

  protected ResultActions performRequest(String data, MediaType accept, String body) throws Exception {
    MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/test")
      .param(DATA, data)
      .accept(accept);

    if (body != null) {
      requestBuilder
        .contentType(MediaType.APPLICATION_JSON)
        .content(body);
    }

    return mockMvc.perform(requestBuilder);
  }


  @Test
  void handleInvalidValueExceptionError() throws Exception {
    doThrow(new InvalidValueException("ERRORCODE", "Error", List.of(new ErrorFieldDTO().field("fieldName").error("fieldError").message("fieldErrorMessage")))).when(testControllerSpy).testEndpoint(DATA,BODY);

    performRequest(DATA, MediaType.APPLICATION_JSON)
      .andExpect(MockMvcResultMatchers.status().isBadRequest())
      .andExpect(MockMvcResultMatchers.jsonPath("$.category").value("PAGOPA_PAYMENTS_BAD_REQUEST"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("ERRORCODE"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Error"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.fields[0].field").value("fieldName"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.fields[0].error").value("fieldError"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.fields[0].message").value("fieldErrorMessage"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.traceId").value(traceId));

  }

  @Test
  void handleForbiddenErrorExceptionError() throws Exception {
    doThrow(new ForbiddenException("ERRORCODE", "Error")).when(testControllerSpy).testEndpoint(DATA, BODY);

    performRequest(DATA, MediaType.APPLICATION_JSON)
      .andExpect(MockMvcResultMatchers.status().isForbidden())
      .andExpect(MockMvcResultMatchers.jsonPath("$.category").value("PAGOPA_PAYMENTS_FORBIDDEN"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("ERRORCODE"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Error"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.fields").doesNotExist())
      .andExpect(MockMvcResultMatchers.jsonPath("$.traceId").value(traceId));

  }

  @Test
  void handleNotAuthorizedExceptionError() throws Exception {
    doThrow(new NotAuthorizedException("ERRORCODE", "Error")).when(testControllerSpy).testEndpoint(DATA, BODY);

    performRequest(DATA, MediaType.APPLICATION_JSON)
      .andExpect(MockMvcResultMatchers.status().isUnauthorized())
      .andExpect(MockMvcResultMatchers.jsonPath("$.category").value("PAGOPA_PAYMENTS_UNAUTHORIZED"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("ERRORCODE"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Error"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.fields").doesNotExist())
      .andExpect(MockMvcResultMatchers.jsonPath("$.traceId").value(traceId));

  }

  @Test
  void handleAuthorizationDeniedException() throws Exception {
    doThrow(new AuthorizationDeniedException("Error")).when(testControllerSpy).testEndpoint(DATA, BODY);

    performRequest(DATA, MediaType.APPLICATION_JSON)
      .andExpect(MockMvcResultMatchers.status().isForbidden())
      .andExpect(MockMvcResultMatchers.jsonPath("$.category").value("PAGOPA_PAYMENTS_FORBIDDEN"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("PAGOPA_PAYMENTS_FORBIDDEN"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Error"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.fields").doesNotExist())
      .andExpect(MockMvcResultMatchers.jsonPath("$.traceId").value(traceId));
  }

  @Test
  void handleConflictExceptionError() throws Exception {
    doThrow(new ConflictException("ERRORCODE", "Error")).when(testControllerSpy).testEndpoint(DATA, BODY);

    performRequest(DATA, MediaType.APPLICATION_JSON)
      .andExpect(MockMvcResultMatchers.status().isConflict())
      .andExpect(MockMvcResultMatchers.jsonPath("$.category").value("PAGOPA_PAYMENTS_CONFLICT"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("ERRORCODE"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Error"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.fields").doesNotExist())
      .andExpect(MockMvcResultMatchers.jsonPath("$.traceId").value(traceId));

  }

  @Test
  void handleMissingServletRequestParameterException() throws Exception {

    performRequest(null, MediaType.APPLICATION_JSON)
      .andExpect(MockMvcResultMatchers.status().isBadRequest())
      .andExpect(MockMvcResultMatchers.jsonPath("$.category").value("PAGOPA_PAYMENTS_BAD_REQUEST"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("PAGOPA_PAYMENTS_BAD_REQUEST"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Required request parameter 'data' for method parameter type String is not present"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.fields[0].field").value("data"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.fields[0].error").value("NotNull"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.fields[0].message").value("Required request parameter 'data' for method parameter type String is not present"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.traceId").value(traceId));

  }

  @Test
  void handleRuntimeExceptionError() throws Exception {
    doThrow(new RuntimeException("Error")).when(testControllerSpy).testEndpoint(DATA, BODY);

    performRequest(DATA, MediaType.APPLICATION_JSON)
      .andExpect(MockMvcResultMatchers.status().isInternalServerError())
      .andExpect(MockMvcResultMatchers.jsonPath("$.category").value("PAGOPA_PAYMENTS_GENERIC_ERROR"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("PAGOPA_PAYMENTS_GENERIC_ERROR"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Error"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.fields").doesNotExist())
      .andExpect(MockMvcResultMatchers.jsonPath("$.traceId").value(traceId));
  }

  @Test
  void handleGenericServletException() throws Exception {
    doThrow(new ServletException("Error"))
      .when(requestMappingHandlerAdapterSpy).handle(any(), any(), any());

    performRequest(DATA, MediaType.APPLICATION_JSON)
      .andExpect(MockMvcResultMatchers.status().isInternalServerError())
      .andExpect(MockMvcResultMatchers.jsonPath("$.category").value("PAGOPA_PAYMENTS_GENERIC_ERROR"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("PAGOPA_PAYMENTS_GENERIC_ERROR"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Error"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.fields").doesNotExist())
      .andExpect(MockMvcResultMatchers.jsonPath("$.traceId").value(traceId));
  }

  @Test
  void handle4xxHttpServletException() throws Exception {
    performRequest(DATA, MediaType.parseMediaType("application/hal+json"))
      .andExpect(MockMvcResultMatchers.status().isNotAcceptable())
      .andExpect(MockMvcResultMatchers.jsonPath("$.category").value("PAGOPA_PAYMENTS_BAD_REQUEST"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("PAGOPA_PAYMENTS_BAD_REQUEST"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("No acceptable representation"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.fields").doesNotExist())
      .andExpect(MockMvcResultMatchers.jsonPath("$.traceId").value(traceId));
  }

  @Test
  void handleUrlNotFound() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders.post("/NOTEXISTENTURL"))
      .andExpect(MockMvcResultMatchers.status().isNotFound())
      .andExpect(MockMvcResultMatchers.jsonPath("$.category").value("PAGOPA_PAYMENTS_NOT_FOUND"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("PAGOPA_PAYMENTS_NOT_FOUND"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("No static resource NOTEXISTENTURL for request '/NOTEXISTENTURL'."))
      .andExpect(MockMvcResultMatchers.jsonPath("$.fields").doesNotExist())
      .andExpect(MockMvcResultMatchers.jsonPath("$.traceId").value(traceId));
  }

  @Test
  void handleNotFoundException() throws Exception {
    doThrow(new NotFoundException("ERROR_CODE", "Error"))
      .when(requestMappingHandlerAdapterSpy).handle(any(), any(), any());

    performRequest(DATA, MediaType.APPLICATION_JSON)
      .andExpect(MockMvcResultMatchers.status().isNotFound())
      .andExpect(MockMvcResultMatchers.jsonPath("$.category").value("PAGOPA_PAYMENTS_NOT_FOUND"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("ERROR_CODE"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Error"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.fields").doesNotExist())
      .andExpect(MockMvcResultMatchers.jsonPath("$.traceId").value(traceId));
  }

  @Test
  void handleNoBodyException() throws Exception {
    performRequest(DATA, MediaType.APPLICATION_JSON, null)
      .andExpect(MockMvcResultMatchers.status().isBadRequest())
      .andExpect(MockMvcResultMatchers.jsonPath("$.category").value("PAGOPA_PAYMENTS_BAD_REQUEST"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("PAGOPA_PAYMENTS_BAD_REQUEST"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Required request body is missing"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.fields").doesNotExist())
      .andExpect(MockMvcResultMatchers.jsonPath("$.traceId").value(traceId));
  }

  @Test
  void handleMalformedBodyException() throws Exception {
    performRequest(DATA, MediaType.APPLICATION_JSON,
      "{\"")
      .andExpect(MockMvcResultMatchers.status().isBadRequest())
      .andExpect(MockMvcResultMatchers.jsonPath("$.category").value("PAGOPA_PAYMENTS_BAD_REQUEST"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("PAGOPA_PAYMENTS_BAD_REQUEST"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Cannot parse body. Unexpected end-of-input: was expecting closing '\"' for name"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.fields").doesNotExist())
      .andExpect(MockMvcResultMatchers.jsonPath("$.traceId").value(traceId));
  }

  @Test
  void handleInvalidBodyException() throws Exception {
    performRequest(DATA, MediaType.APPLICATION_JSON,
      "{\"notRequiredField\":\"notRequired\",\"lowerCaseAlphabeticField\":\"ABC\"}")
      .andExpect(MockMvcResultMatchers.status().isBadRequest())
      .andExpect(MockMvcResultMatchers.jsonPath("$.category").value("PAGOPA_PAYMENTS_BAD_REQUEST"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("PAGOPA_PAYMENTS_BAD_REQUEST"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Invalid request content. lowerCaseAlphabeticField: must match \"[a-z]+\"; requiredField: must not be null"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.fields[0].field").value("lowerCaseAlphabeticField"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.fields[0].error").value("Pattern"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.fields[0].message").value("must match \"[a-z]+\""))
      .andExpect(MockMvcResultMatchers.jsonPath("$.fields[1].field").value("requiredField"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.fields[1].error").value("NotNull"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.fields[1].message").value("must not be null"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.traceId").value(traceId));
  }

  @Test
  void handleNotParsableBodyException() throws Exception {
    performRequest(DATA, MediaType.APPLICATION_JSON,
      "{\"notRequiredField\":\"notRequired\",\"dateTimeField\":\"2025-02-05\"}")
      .andExpect(MockMvcResultMatchers.status().isBadRequest())
      .andExpect(MockMvcResultMatchers.jsonPath("$.category").value("PAGOPA_PAYMENTS_BAD_REQUEST"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("PAGOPA_PAYMENTS_BAD_REQUEST"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Cannot parse body. dateTimeField: Text '2025-02-05' could not be parsed at index 10"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.fields[0].field").value("dateTimeField"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.fields[0].error").value("DateTimeParse"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.fields[0].message").value("Text '2025-02-05' could not be parsed at index 10"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.traceId").value(traceId));
  }

  @Test
  void handle5xxHttpServletException() throws Exception {
    doThrow(new ServerErrorException("Error", new RuntimeException("Error")))
      .when(requestMappingHandlerAdapterSpy).handle(any(), any(), any());

    performRequest(DATA, MediaType.APPLICATION_JSON)
      .andExpect(MockMvcResultMatchers.status().isInternalServerError())
      .andExpect(MockMvcResultMatchers.jsonPath("$.category").value("PAGOPA_PAYMENTS_GENERIC_ERROR"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("PAGOPA_PAYMENTS_GENERIC_ERROR"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("500 INTERNAL_SERVER_ERROR \"Error\""))
      .andExpect(MockMvcResultMatchers.jsonPath("$.fields").doesNotExist())
      .andExpect(MockMvcResultMatchers.jsonPath("$.traceId").value(traceId));
  }

  @Test
  void handleHttpClientErrorTooManyRequestsException() throws Exception {
    doThrow(HttpClientErrorException.create(HttpStatus.TOO_MANY_REQUESTS, "TooManyRequests", null, null, null))
      .when(requestMappingHandlerAdapterSpy).handle(any(), any(), any());

    performRequest(DATA, MediaType.APPLICATION_JSON)
      .andExpect(MockMvcResultMatchers.status().isTooManyRequests())
      .andExpect(MockMvcResultMatchers.jsonPath("$.category").value("PAGOPA_PAYMENTS_TOO_MANY_REQUESTS"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("PAGOPA_PAYMENTS_TOO_MANY_REQUESTS"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("429 TooManyRequests"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.fields").doesNotExist())
      .andExpect(MockMvcResultMatchers.jsonPath("$.traceId").value(traceId));
  }

  private final ConstraintViolationException constraintViolationException = ConstraintViolationExceptionMessageTranscoderTest.buildConstraintViolationException();
  @Test
  void handleViolationException() throws Exception {
    doThrow(constraintViolationException).when(testControllerSpy).testEndpoint(DATA, BODY);

    assertConstraintViolationException();
  }

  private void assertConstraintViolationException() throws Exception {
    ResultActions resultActions = performRequest(DATA, MediaType.APPLICATION_JSON)
      .andExpect(MockMvcResultMatchers.status().isBadRequest())
      .andExpect(MockMvcResultMatchers.jsonPath("$.category").value("PAGOPA_PAYMENTS_BAD_REQUEST"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("PAGOPA_PAYMENTS_BAD_REQUEST"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(EXPECTED_CONSTRAINT_EXCEPTION_MESSAGE_TRANSCODED.getMessage()))
      .andExpect(MockMvcResultMatchers.jsonPath("$.traceId").value(traceId));

    for (int i = 0; i < EXPECTED_CONSTRAINT_EXCEPTION_MESSAGE_TRANSCODED.getFields().size(); i++) {
      ErrorFieldDTO errorFieldDTO = EXPECTED_CONSTRAINT_EXCEPTION_MESSAGE_TRANSCODED.getFields().get(i);
      resultActions
        .andExpect(MockMvcResultMatchers.jsonPath("$.fields[" + i + "].field").value(errorFieldDTO.getField()))
        .andExpect(MockMvcResultMatchers.jsonPath("$.fields[" + i + "].error").value(errorFieldDTO.getError()))
        .andExpect(MockMvcResultMatchers.jsonPath("$.fields[" + i + "].message").value(errorFieldDTO.getMessage()));
    }
  }

  @Test
  void handleHttpHostConnectionExceptionException() throws Exception {
    doThrow(new RuntimeException("connection refused", new HttpHostConnectException("error"))).when(testControllerSpy).testEndpoint(DATA, BODY);

    performRequest(DATA, MediaType.APPLICATION_JSON)
      .andExpect(MockMvcResultMatchers.status().isInternalServerError())
      .andExpect(MockMvcResultMatchers.jsonPath("$.category").value("PAGOPA_PAYMENTS_GENERIC_ERROR"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("PAGOPA_PAYMENTS_CONNECTION_ERROR"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("connection refused"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.fields").doesNotExist())
      .andExpect(MockMvcResultMatchers.jsonPath("$.traceId").value(traceId));
  }

}
