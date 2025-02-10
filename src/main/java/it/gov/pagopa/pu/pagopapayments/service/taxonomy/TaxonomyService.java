package it.gov.pagopa.pu.pagopapayments.service.taxonomy;

import it.gov.pagopa.pu.pagopapayments.connector.PagoPaApiClient;
import it.gov.pagopa.pu.pagopapayments.dto.PaTaxonomyDTO;
import it.gov.pagopa.pu.pagopapayments.dto.generated.Taxonomy;
import it.gov.pagopa.pu.pagopapayments.mapper.PaTaxonomyMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;


@Service
@Slf4j
public class TaxonomyService {

  private final PagoPaApiClient pagoPaApiClient;

  public TaxonomyService(PagoPaApiClient pagoPaApiClient) {
    this.pagoPaApiClient = pagoPaApiClient;
  }

  public List<Taxonomy> getTaxonomies() {
    List<PaTaxonomyDTO> paTaxonomyDTOList = pagoPaApiClient.getTaxonomies();
    if (paTaxonomyDTOList == null) {
      return Collections.emptyList();
    }
    return paTaxonomyDTOList.stream()
      .map(PaTaxonomyMapper::map)
      .toList();
  }
}
