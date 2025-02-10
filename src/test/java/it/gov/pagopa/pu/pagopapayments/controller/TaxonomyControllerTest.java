package it.gov.pagopa.pu.pagopapayments.controller;

import it.gov.pagopa.pu.pagopapayments.dto.generated.Taxonomy;
import it.gov.pagopa.pu.pagopapayments.service.taxonomy.TaxonomyService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class TaxonomyControllerTest {

  @Mock
  private TaxonomyService taxonomyService;

  @InjectMocks
  private TaxonomyController taxonomyController;

  @Test
  void testGetTaxonomiesSuccess() {
    // Given
    Taxonomy taxonomy = Taxonomy.builder()
      .taxonomyId(1L)
      .taxonomyCode("9/0123AB")
      .organizationType("01")
      .organizationTypeDescription("organization")
      .macroAreaCode("02")
      .macroAreaName("macroArea")
      .macroAreaDescription("macroArea description")
      .serviceTypeCode("03")
      .serviceType("serviceType")
      .serviceTypeDescription("serviceType description")
      .collectionReason("collectionReason")
      .startDateValidity(OffsetDateTime.now())
      .endDateOfValidity(OffsetDateTime.now())
      .build();

    List<Taxonomy> expectedTaxonomies = List.of(taxonomy);
    when(taxonomyService.getTaxonomies()).thenReturn(expectedTaxonomies);

    // When
    ResponseEntity<List<Taxonomy>> response = taxonomyController.fetchTaxonomies();

    // Then
    assertEquals(ResponseEntity.ok(expectedTaxonomies), response);
    verify(taxonomyService, times(1)).getTaxonomies();
  }

  @Test
  void testGetTaxonomiesEmpty() {
    // Given
    List<Taxonomy> expectedTaxonomies = List.of();
    when(taxonomyService.getTaxonomies()).thenReturn(expectedTaxonomies);

    // When
    ResponseEntity<List<Taxonomy>> response = taxonomyController.fetchTaxonomies();

    // Then
    assertEquals(ResponseEntity.ok(expectedTaxonomies), response);
    verify(taxonomyService, times(1)).getTaxonomies();
  }

}
