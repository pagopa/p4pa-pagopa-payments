package it.gov.pagopa.pu.pagopapayments.service.taxonomy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.pu.organization.dto.generated.Taxonomy;
import it.gov.pagopa.pu.pagopapayments.connector.PagoPaApiClient;
import it.gov.pagopa.pu.pagopapayments.dto.PaTaxonomyDTO;
import it.gov.pagopa.pu.pagopapayments.exception.ApplicationException;
import it.gov.pagopa.pu.pagopapayments.mapper.PaTaxonomyMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class TaxonomyService {

  private final PagoPaApiClient pagoPaApiClient;
  private final ObjectMapper objectMapper;

  public TaxonomyService(PagoPaApiClient pagoPaApiClient, Jackson2ObjectMapperBuilder mapperBuilder) {
    this.pagoPaApiClient = pagoPaApiClient;
    this.objectMapper = mapperBuilder.build();
  }

  public List<Taxonomy> getTaxonomies() {
    String json = pagoPaApiClient.getTaxonomies();
    List<PaTaxonomyDTO> paTaxonomyDTOList;
    try {
      paTaxonomyDTOList = objectMapper.readValue(json, new TypeReference<List<PaTaxonomyDTO>>() {});
    } catch (JsonProcessingException e) {
      throw new ApplicationException(e);
    }
    if (paTaxonomyDTOList == null) {
      return Collections.emptyList();
    }
    return paTaxonomyDTOList.stream()
      .map(PaTaxonomyMapper::map)
      .toList();
  }
}
