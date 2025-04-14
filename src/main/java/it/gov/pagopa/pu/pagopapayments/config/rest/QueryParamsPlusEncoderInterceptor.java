package it.gov.pagopa.pu.pagopapayments.config.rest;

import jakarta.annotation.Nonnull;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.support.HttpRequestWrapper;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;

public class QueryParamsPlusEncoderInterceptor implements ClientHttpRequestInterceptor {

  private static final String PLUS_RAW = "+";
  private static final String PLUS_ENCODED = "%2B";

  @Override
  @Nonnull
  public ClientHttpResponse intercept(@Nonnull HttpRequest request,@Nonnull byte[] body,@Nonnull ClientHttpRequestExecution execution) throws IOException {
    HttpRequest encodedRequest = new HttpRequestWrapper(request) {
      @Override
      @Nonnull
      public URI getURI() {
        URI uri = super.getURI();
        String escapedQuery = uri.getRawQuery();

        if(escapedQuery != null){
          escapedQuery = escapedQuery.replace(PLUS_RAW, PLUS_ENCODED);
          return UriComponentsBuilder.fromUri(uri)
                  .replaceQuery(escapedQuery)
                  .build(true).toUri();
        }else {
          return uri;
        }
      }
    };
    return execution.execute(encodedRequest, body);
  }
}
