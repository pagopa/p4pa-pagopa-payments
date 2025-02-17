package it.gov.pagopa.pu.pagopapayments.controller;

import it.gov.pagopa.pu.pagopapayments.connector.pagopa.taxonomy.TaxonomyService;
import it.gov.pagopa.pu.pagopapayments.dto.generated.TaxonomyDTO;
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
    TaxonomyDTO taxonomy = TaxonomyDTO.builder()
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

    List<TaxonomyDTO> expectedTaxonomies = List.of(taxonomy);
    when(taxonomyService.getTaxonomies()).thenReturn(expectedTaxonomies);

    // When
    ResponseEntity<List<TaxonomyDTO>> response = taxonomyController.fetchTaxonomies();

    // Then
    assertEquals(ResponseEntity.ok(expectedTaxonomies), response);
    verify(taxonomyService, times(1)).getTaxonomies();
  }

  @Test
  void testGetTaxonomiesEmpty() {
    // Given
    List<TaxonomyDTO> expectedTaxonomies = List.of();
    when(taxonomyService.getTaxonomies()).thenReturn(expectedTaxonomies);

    // When
    ResponseEntity<List<TaxonomyDTO>> response = taxonomyController.fetchTaxonomies();

    // Then
    assertEquals(ResponseEntity.ok(expectedTaxonomies), response);
    verify(taxonomyService, times(1)).getTaxonomies();
  }

}
