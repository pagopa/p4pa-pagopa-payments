package it.gov.pagopa.pu.pagopapayments.config.rest;

import it.gov.pagopa.pu.pagopapayments.util.SecurityUtils;
import jakarta.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResponseErrorHandler;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.net.URI;
import java.util.function.BiFunction;

@Slf4j
public class HttpClientErrorHandler<T> extends DefaultResponseErrorHandler {

  private final JsonMapper jsonMapper;
  private final Class<T> errorDtoClass;
  private final BiFunction<HttpStatusCodeException, T, RuntimeException> errorTranscoder;
  private final ResponseErrorHandler errorHandler;

  public HttpClientErrorHandler(JsonMapper jsonMapper, String applicationName, boolean bodyPrinterWhenError, Class<T> errorDtoClass, BiFunction<HttpStatusCodeException, T, RuntimeException> errorTranscoder) {
    this.jsonMapper = jsonMapper;
    this.errorDtoClass = errorDtoClass;
    this.errorTranscoder = errorTranscoder;
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
      if (statusCode.is4xxClientError() && statusCode.value() != 404) {
        try {
          T errorDTO = jsonMapper.readValue(ex.getResponseBodyAsString(), errorDtoClass);
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
}
