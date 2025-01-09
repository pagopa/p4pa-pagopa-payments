package it.gov.pagopa.pu.pagopapayments.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;

import java.util.function.Supplier;

@Slf4j
public class RestUtil {
  public static <T> T handleRestException(Supplier<T> invoker, Supplier<String> logErrorDetail, boolean logElapsed) {
    long httpCallStart = 0;
    if(logElapsed)
      httpCallStart = System.currentTimeMillis();
    try{
      return invoker.get();
    } catch (HttpClientErrorException.NotFound nfe) {
      int statusCode = nfe.getStatusCode().value();
      String body = nfe.getResponseBodyAsString();
      log.warn("HttpClientErrorException.NotFound on {} - returned code[{}] body[{}]", logErrorDetail.get(), statusCode, body);
      return null;
    } catch (HttpServerErrorException he) {
      int statusCode = he.getStatusCode().value();
      String body = he.getResponseBodyAsString();
      log.error("HttpServerErrorException on {} - returned code[{}] body[{}]", logErrorDetail.get(), statusCode, body);
      throw he;
    } catch (RestClientException e) {
      log.error("error on {}", logErrorDetail.get(), e);
      throw e;
    } finally {
      if(logElapsed) {
        long elapsed = Math.max(0, System.currentTimeMillis() - httpCallStart);
        log.info("elapsed time(ms) for {}: {}", logErrorDetail.get(), elapsed);
      }
    }
  }
  public static <T> T handleRestException(Supplier<T> invoker, Supplier<String> logErrorDetail){
    return handleRestException(invoker, logErrorDetail, false);
  }
  public static <T> T handleRestException(Supplier<T> invoker, boolean logElapsed) {
    return handleRestException(invoker, ()->"", logElapsed);
  }
  public static <T> T handleRestException(Supplier<T> invoker) {
    return handleRestException(invoker, ()->"", false);
  }

  public static <T> T handleRestExceptionWithResponseEntity(Supplier<ResponseEntity<T>> invoker, Supplier<String> logErrorDetail, boolean logElapsed) {
    return handleRestException(invoker, logErrorDetail, logElapsed).getBody();
  }
  public static <T> T handleRestExceptionWithResponseEntity(Supplier<ResponseEntity<T>> invoker, Supplier<String> logErrorDetail){
    return handleRestExceptionWithResponseEntity(invoker, logErrorDetail, false);
  }
  public static <T> T handleRestExceptionWithResponseEntity(Supplier<ResponseEntity<T>> invoker, boolean logElapsed) {
    return handleRestExceptionWithResponseEntity(invoker, ()->"", logElapsed);
  }
  public static <T> T handleRestExceptionWithResponseEntity(Supplier<ResponseEntity<T>> invoker) {
    return handleRestExceptionWithResponseEntity(invoker, ()->"", false);
  }
}
