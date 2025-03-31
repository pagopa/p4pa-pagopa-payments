package it.gov.pagopa.pu.pagopapayments.controller;

import it.gov.pagopa.pu.pagopapayments.connector.pagopa.taxonomy.TaxonomyService;
import it.gov.pagopa.pu.pagopapayments.controller.generated.TaxonomiesApi;
import it.gov.pagopa.pu.pagopapayments.dto.generated.TaxonomyDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
public class TaxonomyController implements TaxonomiesApi {

  private final TaxonomyService taxonomyService;

    public TaxonomyController(TaxonomyService taxonomyService) {
        this.taxonomyService = taxonomyService;
    }


  @Override
  public ResponseEntity<List<TaxonomyDTO>> fetchTaxonomies(){
    log.info("retrieve taxonomies");
    List<TaxonomyDTO> result = taxonomyService.getTaxonomies();
    return ResponseEntity.ok(result);
  }


}
