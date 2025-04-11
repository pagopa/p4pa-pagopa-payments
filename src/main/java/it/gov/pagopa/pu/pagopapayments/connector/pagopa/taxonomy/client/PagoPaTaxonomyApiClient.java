package it.gov.pagopa.pu.pagopapayments.connector.pagopa.taxonomy.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.pu.pagopapayments.config.rest.RestTemplateConfig;
import it.gov.pagopa.pu.pagopapayments.connector.pagopa.taxonomy.dto.PaTaxonomyDTO;
import it.gov.pagopa.pu.pagopapayments.exception.ApplicationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@Slf4j
public class PagoPaTaxonomyApiClient {

  private final RestTemplate restTemplate;
  private final ObjectMapper objectMapper;
  private final String baseUrl;

  public PagoPaTaxonomyApiClient(
    @Value("${rest.pagopa-taxonomies.base-url}") String baseUrl,
    @Value("${rest.pagopa-taxonomies.print-body-when-error}") boolean printBodyWhenError,

    RestTemplateBuilder restTemplateBuilder,
    ObjectMapper objectMapper
  ) {
    this.restTemplate = restTemplateBuilder.build();
    this.baseUrl = baseUrl;
    this.objectMapper = objectMapper;

    if (printBodyWhenError) {
      restTemplate.setErrorHandler(RestTemplateConfig.bodyPrinterWhenError("PAGOPA-TAXONOMIES"));
    }
  }

  public List<PaTaxonomyDTO> getTaxonomies() {
    String json = restTemplate.getForObject(baseUrl, String.class);
    return deserializeTaxonomies(json);
  }

  private List<PaTaxonomyDTO> deserializeTaxonomies(String json) {
    try {
      return objectMapper.readValue(json, new TypeReference<>() {});
    } catch (JsonProcessingException e) {
      throw new ApplicationException(e);
    }
  }

}
