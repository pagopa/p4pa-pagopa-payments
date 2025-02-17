package it.gov.pagopa.pu.pagopapayments.connector.pagopa.taxonomy;

import it.gov.pagopa.pu.pagopapayments.connector.pagopa.taxonomy.client.PagoPaTaxonomyApiClient;
import it.gov.pagopa.pu.pagopapayments.connector.pagopa.taxonomy.dto.PaTaxonomyDTO;
import it.gov.pagopa.pu.pagopapayments.connector.pagopa.taxonomy.mapper.PaTaxonomyMapper;
import it.gov.pagopa.pu.pagopapayments.dto.generated.TaxonomyDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class TaxonomyService {

  private final PagoPaTaxonomyApiClient client;

  public TaxonomyService(PagoPaTaxonomyApiClient client) {
    this.client = client;
  }

  public List<TaxonomyDTO> getTaxonomies() {
    return ObjectUtils.firstNonNull(client.getTaxonomies(), List.<PaTaxonomyDTO>of())
      .stream()
      .map(PaTaxonomyMapper::map)
      .toList();
  }
}
