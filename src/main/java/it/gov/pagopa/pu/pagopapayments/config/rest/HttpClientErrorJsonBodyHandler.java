package it.gov.pagopa.pu.pagopapayments.config.rest;

import it.gov.pagopa.pu.pagopapayments.dto.generated.ErrorFieldDTO;
import it.gov.pagopa.pu.pagopapayments.exception.common.*;
import it.gov.pagopa.pu.pagopapayments.util.SecurityUtils;
import jakarta.annotation.Nonnull;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResponseErrorHandler;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

/** It will transcode Http client errors (see ignoredClientErrors for exceptions) using the provided transcoder. */
@Slf4j
@Getter
public class HttpClientErrorJsonBodyHandler<T> extends DefaultResponseErrorHandler {

  private final String applicationName;
  private final JsonMapper jsonMapper;
  private final Class<T> errorDtoClass;
  private final BiFunction<HttpStatusCodeException, T, RuntimeException> errorTranscoder;
  private final ResponseErrorHandler errorHandler;

  /**
   * Skipped Http client errors:
   * <li>429 is handled by openApiGenerator code in order to retry it
   */
  private final Set<HttpStatusCode> ignoredClientErrors = Set.of(
    HttpStatus.TOO_MANY_REQUESTS
  );

  public HttpClientErrorJsonBodyHandler(
    JsonMapper jsonMapper,
    String applicationName,
    boolean bodyPrinterWhenError,
    Class<T> errorDtoClass,
    Function<T, String> errorDto2CodeFunction,
    Function<T, String> errorDto2MessageFunction
  ) {
    this(jsonMapper,
      applicationName,
      bodyPrinterWhenError,
      errorDtoClass,
      buildDefaultHttpClientExceptionTranscoder(applicationName, errorDto2CodeFunction, errorDto2MessageFunction)
    );
  }

  public HttpClientErrorJsonBodyHandler(
    JsonMapper jsonMapper,
    String applicationName,
    boolean bodyPrinterWhenError,
    Class<T> errorDtoClass,
    Function<T, PuErrorDTO> errorDtoNormalizer
  ) {
    this(jsonMapper, applicationName, bodyPrinterWhenError, errorDtoClass,
      buildDefaultHttpClientExceptionTranscoder(applicationName, errorDtoNormalizer));
  }

  public HttpClientErrorJsonBodyHandler(JsonMapper jsonMapper, String applicationName, boolean bodyPrinterWhenError, Class<T> errorDtoClass, BiFunction<HttpStatusCodeException, T, RuntimeException> httpClientExceptionTranscoder) {
    this.applicationName = applicationName;
    this.jsonMapper = jsonMapper;
    this.errorDtoClass = errorDtoClass;
    this.errorTranscoder = httpClientExceptionTranscoder;
    this.errorHandler = bodyPrinterWhenError
      ? RestTemplateConfig.bodyPrinterWhenError(applicationName)
      : new DefaultResponseErrorHandler();
  }

  @Override
  protected void handleError(@Nonnull ClientHttpResponse response, @Nonnull HttpStatusCode statusCode,
                             URI url, HttpMethod method) throws IOException {
    try {
      errorHandler.handleError(url, method, response);
    } catch (HttpStatusCodeException ex) {
      if (statusCode.is4xxClientError() && !ignoredClientErrors.contains(statusCode)) {
        try {
          T errorDTO = null;
          if(ex.getResponseBodyAsByteArray().length > 0) {
            errorDTO = jsonMapper.readValue(ex.getResponseBodyAsString(), errorDtoClass);
          }
          throw errorTranscoder.apply(ex, errorDTO);
        } catch (JacksonException jacksonException) {
          log.info("Cannot deserialize error response from request {} {} - {}",
            method,
            SecurityUtils.removePiiFromURI(url),
            jacksonException.getMessage());
          throw ex;
        }
      } else {
        throw ex;
      }
    }
  }

  /** A default transcoder required to invoke {@link HttpClientErrorJsonBodyHandler} which will transcode the Http client error into a BaseBusinessException using code and message extracted from the errorDTO */
  public static <T> BiFunction<HttpStatusCodeException, T, RuntimeException> buildDefaultHttpClientExceptionTranscoder(String applicationName, Function<T, String> errorDto2CodeFunction, Function<T, String> errorDto2MessageFunction) {
    return buildDefaultHttpClientExceptionTranscoder(applicationName, errorDTO -> new PuErrorDTO(
      null,
      errorDto2CodeFunction != null ?
        errorDto2CodeFunction.apply(errorDTO)
        : null,
      errorDto2MessageFunction.apply(errorDTO),
      null
    ));
  }

  /** A default transcoder required to invoke {@link HttpClientErrorJsonBodyHandler} which will transcode the Http client error into a BaseBusinessException using the errorDTO normalizer function */
  public static <T> BiFunction<HttpStatusCodeException, T, RuntimeException> buildDefaultHttpClientExceptionTranscoder(
    String applicationName,
    Function<T, PuErrorDTO> errorDtoNormalizer
  ) {
    return (exception, errorDTO) -> {
      String code = applicationName + "_" + ((HttpStatus) exception.getStatusCode()).name();
      String category = null;
      String message = exception.getMessage();
      List<ErrorFieldDTO> fields = null;

      if(errorDTO != null) {
        PuErrorDTO puErrorDTO = errorDtoNormalizer.apply(errorDTO);

        code = Objects.requireNonNullElse(
          puErrorDTO.code(),
          code);

        category = puErrorDTO.category();
        message = puErrorDTO.message();
        fields = puErrorDTO.fields();
      }

      return switch (exception.getStatusCode()) {
        case HttpStatus.CONFLICT -> new RestInvokeConflictException(applicationName, (HttpStatus) exception.getStatusCode(), category, code, message, fields);
        case HttpStatus.FORBIDDEN -> new RestInvokeForbiddenException(applicationName, (HttpStatus) exception.getStatusCode(), category, code, message);
        case HttpStatus.UNAUTHORIZED -> new RestInvokeNotAuthorizedException(applicationName, (HttpStatus) exception.getStatusCode(), category, code, message);
        case HttpStatus.NOT_FOUND -> new RestInvokeNotFoundException(applicationName, (HttpStatus) exception.getStatusCode(), category, code, message);
        default -> new RestInvokeInvalidValueException(applicationName, (HttpStatus) exception.getStatusCode(), category, code, message, fields);
      };
    };
  }
}
