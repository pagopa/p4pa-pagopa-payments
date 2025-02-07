package it.gov.pagopa.pu.pagopapayments.controller;

import it.gov.pagopa.pu.organization.dto.generated.Taxonomy;
import it.gov.pagopa.pu.pagopapayments.service.taxonomy.TaxonomyService;
import it.gov.pagopa.pu.pagopapayments.util.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
public class TaxonomyController {

  private final TaxonomyService taxonomyService;

    public TaxonomyController(TaxonomyService taxonomyService) {
        this.taxonomyService = taxonomyService;
    }


    public ResponseEntity<List<Taxonomy>> getTaxonomies(){
    log.info("retrieve taxonomies");
    List<Taxonomy> result = taxonomyService.getTaxonomies();
    return ResponseEntity.ok(result);
  }


}
