package it.gov.pagopa.pu.pagopapayments.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.pu.pagopapayments.config.json.JsonConfig;
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
import org.hibernate.validator.internal.engine.ConstraintViolationImpl;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import java.util.Map;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@ExtendWith({SpringExtension.class})
@WebMvcTest(value = {PagoPaPaymentsExceptionHandlerTest.TestController.class})
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = {
  PagoPaPaymentsExceptionHandlerTest.TestController.class,
  PagopaPaymentsExceptionHandler.class,
  JsonConfig.class})
class PagoPaPaymentsExceptionHandlerTest {

  public static final String DATA = "data";
  public static final TestRequestBody BODY = new TestRequestBody("bodyData", null, "abc", LocalDateTime.now());

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private ObjectMapper objectMapper;

  @MockitoSpyBean
  private TestController testControllerSpy;
  @MockitoSpyBean
  private RequestMappingHandlerAdapter requestMappingHandlerAdapterSpy;

  @RestController
  @Slf4j
  static class TestController {
    @PostMapping(value = "/test", produces = MediaType.APPLICATION_JSON_VALUE)
    String testEndpoint(@RequestParam(DATA) String data, @Valid @RequestBody TestRequestBody body) {
      return "OK";
    }
  }

  @BeforeEach
  void init() {
    TestUtils.clearDefaultTimezone();
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

  private final String traceId = "TRACEID";
  @BeforeEach
  void setTraceId(){
    UtilitiesTest.setTraceId(traceId);
  }
  @AfterEach
  void clearTraceId(){
    UtilitiesTest.clearTraceIdContext();
  }

  private ResultActions performRequest(String data, MediaType accept) throws Exception {
    return performRequest(data, accept, objectMapper.writeValueAsString(PagoPaPaymentsExceptionHandlerTest.BODY));
  }

  private ResultActions performRequest(String data, MediaType accept, String body) throws Exception {
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
  void handleResourceNotFoundException() throws Exception {
    doThrow(new NotFoundException("ERRORCODE", "Error")).when(testControllerSpy).testEndpoint(DATA, BODY);

    performRequest(DATA, MediaType.APPLICATION_JSON)
      .andExpect(MockMvcResultMatchers.status().isNotFound())
      .andExpect(MockMvcResultMatchers.jsonPath("$.category").value("PAGOPA_PAYMENTS_NOT_FOUND"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("ERRORCODE"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("[ERRORCODE] Error"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.traceId").value(traceId));
  }

  @Test
  void handleConflictException() throws Exception {
    doThrow(new ConflictException("ERRORCODE", "Error")).when(testControllerSpy).testEndpoint(DATA, BODY);

    performRequest(DATA, MediaType.APPLICATION_JSON)
      .andExpect(MockMvcResultMatchers.status().isConflict())
      .andExpect(MockMvcResultMatchers.jsonPath("$.category").value("PAGOPA_PAYMENTS_CONFLICT"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("ERRORCODE"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("[ERRORCODE] Error"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.traceId").value(traceId));
  }

  @Test
  void handleForbiddenException() throws Exception {
    doThrow(new ForbiddenException("ERRORCODE", "Error")).when(testControllerSpy).testEndpoint(DATA, BODY);

    performRequest(DATA, MediaType.APPLICATION_JSON)
      .andExpect(MockMvcResultMatchers.status().isForbidden())
      .andExpect(MockMvcResultMatchers.jsonPath("$.category").value("PAGOPA_PAYMENTS_FORBIDDEN"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("ERRORCODE"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("[ERRORCODE] Error"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.traceId").value(traceId));
  }

  @Test
  void handleNotAuthorized() throws Exception {
    doThrow(new NotAuthorizedException("ERRORCODE", "Error")).when(testControllerSpy).testEndpoint(DATA, BODY);

    performRequest(DATA, MediaType.APPLICATION_JSON)
      .andExpect(MockMvcResultMatchers.status().isUnauthorized())
      .andExpect(MockMvcResultMatchers.jsonPath("$.category").value("PAGOPA_PAYMENTS_UNAUTHORIZED"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("ERRORCODE"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("[ERRORCODE] Error"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.traceId").value(traceId));
  }

  @Test
  void handleMissingServletRequestParameterException() throws Exception {
    performRequest(null, MediaType.APPLICATION_JSON)
      .andExpect(MockMvcResultMatchers.status().isBadRequest())
      .andExpect(MockMvcResultMatchers.jsonPath("$.category").value("PAGOPA_PAYMENTS_BAD_REQUEST"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("PAGOPA_PAYMENTS_BAD_REQUEST"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("[PAGOPA_PAYMENTS_BAD_REQUEST] Required request parameter 'data' for method parameter type String is not present"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.traceId").value(traceId));

  }

  @Test
  void handleRuntimeExceptionError() throws Exception {
    doThrow(new RuntimeException("Error")).when(testControllerSpy).testEndpoint(DATA, BODY);

    performRequest(DATA, MediaType.APPLICATION_JSON)
      .andExpect(MockMvcResultMatchers.status().isInternalServerError())
      .andExpect(MockMvcResultMatchers.jsonPath("$.category").value("PAGOPA_PAYMENTS_GENERIC_ERROR"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("PAGOPA_PAYMENTS_GENERIC_ERROR"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("[PAGOPA_PAYMENTS_GENERIC_ERROR] Error"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.traceId").value(traceId));
  }

  @Test
  void handleNotPayableSilActualizedAmountException() throws Exception {
    doThrow(new NotPayableSilActualizedAmountException("Error")).when(testControllerSpy).testEndpoint(DATA, BODY);

    performRequest(DATA, MediaType.APPLICATION_JSON)
      .andExpect(MockMvcResultMatchers.status().isConflict())
      .andExpect(MockMvcResultMatchers.jsonPath("$.category").value("PAGOPA_PAYMENTS_NOT_PAYABLE"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("ACTUALIZATION_ERROR"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("[ACTUALIZATION_ERROR] Error"))
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
      .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("[PAGOPA_PAYMENTS_GENERIC_ERROR] Error"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.traceId").value(traceId));
  }

  @Test
  void handle4xxHttpServletException() throws Exception {
    performRequest(DATA, MediaType.parseMediaType("application/hal+json"))
      .andExpect(MockMvcResultMatchers.status().isNotAcceptable())
      .andExpect(MockMvcResultMatchers.jsonPath("$.category").value("PAGOPA_PAYMENTS_BAD_REQUEST"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("PAGOPA_PAYMENTS_BAD_REQUEST"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("[PAGOPA_PAYMENTS_BAD_REQUEST] No acceptable representation"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.traceId").value(traceId));
  }

  @Test
  void handleUrlNotFound() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders.post("/NOTEXISTENTURL"))
      .andExpect(MockMvcResultMatchers.status().isNotFound())
      .andExpect(MockMvcResultMatchers.jsonPath("$.category").value("PAGOPA_PAYMENTS_NOT_FOUND"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("PAGOPA_PAYMENTS_NOT_FOUND"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("[PAGOPA_PAYMENTS_NOT_FOUND] No static resource NOTEXISTENTURL for request '/NOTEXISTENTURL'."))
      .andExpect(MockMvcResultMatchers.jsonPath("$.traceId").value(traceId));
  }

  @Test
  void handleNoBodyException() throws Exception {
    performRequest(DATA, MediaType.APPLICATION_JSON, null)
      .andExpect(MockMvcResultMatchers.status().isBadRequest())
      .andExpect(MockMvcResultMatchers.jsonPath("$.category").value("PAGOPA_PAYMENTS_BAD_REQUEST"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("PAGOPA_PAYMENTS_BAD_REQUEST"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("[PAGOPA_PAYMENTS_BAD_REQUEST] Required request body is missing"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.traceId").value(traceId));
  }

  @Test
  void handleMalformedBodyException() throws Exception {
    performRequest(DATA, MediaType.APPLICATION_JSON,
      "{\"")
      .andExpect(MockMvcResultMatchers.status().isBadRequest())
      .andExpect(MockMvcResultMatchers.jsonPath("$.category").value("PAGOPA_PAYMENTS_BAD_REQUEST"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("PAGOPA_PAYMENTS_BAD_REQUEST"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("[PAGOPA_PAYMENTS_BAD_REQUEST] Cannot parse body. Unexpected end-of-input: was expecting closing '\"' for name"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.traceId").value(traceId));
  }

  @Test
  void handleInvalidBodyException() throws Exception {
    performRequest(DATA, MediaType.APPLICATION_JSON,
      "{\"notRequiredField\":\"notRequired\",\"lowerCaseAlphabeticField\":\"ABC\"}")
      .andExpect(MockMvcResultMatchers.status().isBadRequest())
      .andExpect(MockMvcResultMatchers.jsonPath("$.category").value("PAGOPA_PAYMENTS_BAD_REQUEST"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("PAGOPA_PAYMENTS_BAD_REQUEST"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("[PAGOPA_PAYMENTS_BAD_REQUEST] Invalid request content. lowerCaseAlphabeticField: must match \"[a-z]+\"; requiredField: must not be null"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.traceId").value(traceId));
  }

  @Test
  void handleNotParsableBodyException() throws Exception {
    performRequest(DATA, MediaType.APPLICATION_JSON,
      "{\"notRequiredField\":\"notRequired\",\"dateTimeField\":\"2025-02-05\"}")
      .andExpect(MockMvcResultMatchers.status().isBadRequest())
      .andExpect(MockMvcResultMatchers.jsonPath("$.category").value("PAGOPA_PAYMENTS_BAD_REQUEST"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("PAGOPA_PAYMENTS_BAD_REQUEST"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("[PAGOPA_PAYMENTS_BAD_REQUEST] Cannot parse body. dateTimeField: Text '2025-02-05' could not be parsed at index 10"))
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
      .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("[PAGOPA_PAYMENTS_GENERIC_ERROR] 500 INTERNAL_SERVER_ERROR \"Error\""))
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
      .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("[PAGOPA_PAYMENTS_TOO_MANY_REQUESTS] 429 TooManyRequests"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.traceId").value(traceId));
  }

  private final ConstraintViolationException constraintViolationException = new ConstraintViolationException("Error", Set.of(ConstraintViolationImpl.forParameterValidation(
    "error message template", Map.of(), Map.of(), "resolved message", null, null, null, null, PathImpl.createPathFromString("fieldName"), null, null, null
  )));
  @Test
  void handleViolationException() throws Exception {
    doThrow(constraintViolationException).when(testControllerSpy).testEndpoint(DATA, BODY);

    performRequest(DATA, MediaType.APPLICATION_JSON)
      .andExpect(MockMvcResultMatchers.status().isBadRequest())
      .andExpect(MockMvcResultMatchers.jsonPath("$.category").value("PAGOPA_PAYMENTS_BAD_REQUEST"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("PAGOPA_PAYMENTS_BAD_REQUEST"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("[PAGOPA_PAYMENTS_BAD_REQUEST] Invalid request content. fieldName: resolved message"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.traceId").value(traceId));
  }

}
