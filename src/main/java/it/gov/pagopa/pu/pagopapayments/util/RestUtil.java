package it.gov.pagopa.pu.pagopapayments.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;

import java.util.function.Supplier;

@Slf4j
public class RestUtil {

  private RestUtil(){}

  public static <T> T handleRestException(Supplier<T> invoker, String operationId, boolean logElapsed) {
    long httpCallStart = 0;
    if(logElapsed)
      httpCallStart = System.currentTimeMillis();
    try{
      return invoker.get();
    } catch (HttpServerErrorException he) {
      int statusCode = he.getStatusCode().value();
      String body = he.getResponseBodyAsString();
      log.error("HttpServerErrorException on {} - returned code[{}] body[{}]", operationId, statusCode, body);
      throw he;
    } catch (RestClientException e) {
      if(e instanceof HttpClientErrorException nfe){
        int statusCode = nfe.getStatusCode().value();
        if(statusCode == HttpStatus.NOT_FOUND.value()){
          String body = nfe.getResponseBodyAsString();
          log.warn("HttpClientErrorException - NotFound on {} - returned code[{}] body[{}]", operationId, statusCode, body);
          return null;
        }
      }
      log.error("error on {}", operationId, e);
      throw e;
    } finally {
      if(logElapsed) {
        long elapsed = Math.max(0, System.currentTimeMillis() - httpCallStart);
        log.info("elapsed time(ms) for {}: {}", operationId, elapsed);
      }
    }
  }
  public static <T> T handleRestException(Supplier<T> invoker, String operationId){
    return handleRestException(invoker, operationId, false);
  }
  public static <T> T handleRestException(Supplier<T> invoker, boolean logElapsed) {
    return handleRestException(invoker, "", logElapsed);
  }
  public static <T> T handleRestException(Supplier<T> invoker) {
    return handleRestException(invoker, "", false);
  }

  public static <T> T handleRestExceptionWithResponseEntity(Supplier<ResponseEntity<T>> invoker, String operationId, boolean logElapsed) {
    return handleRestException(invoker, operationId, logElapsed).getBody();
  }
  public static <T> T handleRestExceptionWithResponseEntity(Supplier<ResponseEntity<T>> invoker, String operationId){
    return handleRestExceptionWithResponseEntity(invoker, operationId, false);
  }
  public static <T> T handleRestExceptionWithResponseEntity(Supplier<ResponseEntity<T>> invoker, boolean logElapsed) {
    return handleRestExceptionWithResponseEntity(invoker, "", logElapsed);
  }
  public static <T> T handleRestExceptionWithResponseEntity(Supplier<ResponseEntity<T>> invoker) {
    return handleRestExceptionWithResponseEntity(invoker, "", false);
  }
}
