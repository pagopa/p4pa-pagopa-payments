package it.gov.pagopa.pu.pagopapayments.connector;

import it.gov.pagopa.pu.pagopapayments.util.RestUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class PagoPaApiClient {

  private final RestTemplate restTemplate;

  private final String baseUrlGithub;

  private final String taxonomyPathContext;

  public PagoPaApiClient(
    @Value("${rest.pagopa.platform.baseUrl}") String baseUrlGithub,
    @Value("${rest.pagopa.platform.taxonomy.service.context}") String taxonomyPathContext,
    RestTemplateBuilder restTemplateBuilder) {
    this.restTemplate= restTemplateBuilder.build();
    this.baseUrlGithub = baseUrlGithub;
    this.taxonomyPathContext = taxonomyPathContext;
  }

  public String getTaxonomies() {
    String url = baseUrlGithub + taxonomyPathContext;
    return RestUtil.handleRestException(() -> restTemplate.getForObject(url, String.class), "getTaxonomies", true);
  }

}
