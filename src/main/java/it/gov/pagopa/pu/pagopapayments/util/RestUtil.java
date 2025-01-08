package it.gov.pagopa.pu.pagopapayments.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;

import java.util.function.Supplier;

@Slf4j
public class RestUtil {
  public static <T> T handleRestException(Supplier<T> in, Supplier<String> logErrorDetail, boolean logElapsed) {
    long httpCallStart = 0;
    if(logElapsed)
      httpCallStart = System.currentTimeMillis();
    try{
      return in.get();
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
  public static <T> T handleRestException(Supplier<T> in, Supplier<String> logErrorDetail){
    return handleRestException(in, logErrorDetail, false);
  }
  public static <T> T handleRestException(Supplier<T> in, boolean logElapsed) {
    return handleRestException(in, ()->"", logElapsed);
  }
  public static <T> T handleRestException(Supplier<T> in) {
    return handleRestException(in, ()->"", false);
  }

  public static <T> T handleRestExceptionWithResponseEntity(Supplier<ResponseEntity<T>> in, Supplier<String> logErrorDetail, boolean logElapsed) {
    return handleRestException(in, logErrorDetail, logElapsed).getBody();
  }
  public static <T> T handleRestExceptionWithResponseEntity(Supplier<ResponseEntity<T>> in, Supplier<String> logErrorDetail){
    return handleRestExceptionWithResponseEntity(in, logErrorDetail, false);
  }
  public static <T> T handleRestExceptionWithResponseEntity(Supplier<ResponseEntity<T>> in, boolean logElapsed) {
    return handleRestExceptionWithResponseEntity(in, ()->"", logElapsed);
  }
  public static <T> T handleRestExceptionWithResponseEntity(Supplier<ResponseEntity<T>> in) {
    return handleRestExceptionWithResponseEntity(in, ()->"", false);
  }
}
