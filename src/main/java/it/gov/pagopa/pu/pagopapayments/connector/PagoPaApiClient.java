package it.gov.pagopa.pu.pagopapayments.connector;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.pu.pagopapayments.dto.PaTaxonomyDTO;
import it.gov.pagopa.pu.pagopapayments.exception.ApplicationException;
import it.gov.pagopa.pu.pagopapayments.util.RestUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@Slf4j
public class PagoPaApiClient {

  private final RestTemplate restTemplate;
  private final ObjectMapper objectMapper;
  private final String baseUrl;

  public PagoPaApiClient(
    @Value("${rest.pagopa-taxonomies.base-url}") String baseUrl,
    RestTemplateBuilder restTemplateBuilder,
    ObjectMapper objectMapper) {
    this.restTemplate = restTemplateBuilder.build();
    this.baseUrl = baseUrl;
    this.objectMapper = objectMapper;
  }

  public List<PaTaxonomyDTO> getTaxonomies() {
    String json = RestUtil.handleRestException(() -> restTemplate.getForObject(baseUrl, String.class), "getTaxonomies", true);
    return deserializeTaxonomies(json);
  }

  private List<PaTaxonomyDTO> deserializeTaxonomies(String json) {
    try {
      return objectMapper.readValue(json, new TypeReference<List<PaTaxonomyDTO>>() {});
    } catch (JsonProcessingException e) {
      throw new ApplicationException(e);
    }
  }

}
