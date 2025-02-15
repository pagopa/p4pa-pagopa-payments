package it.gov.pagopa.pu.pagopapayments.service;

import it.gov.pagopa.pu.pagopapayments.connector.pagopa.taxonomy.client.PagoPaTaxonomyApiClient;
import it.gov.pagopa.pu.pagopapayments.dto.PaTaxonomyDTO;
import it.gov.pagopa.pu.pagopapayments.dto.generated.Taxonomy;
import it.gov.pagopa.pu.pagopapayments.mapper.PaTaxonomyMapper;
import it.gov.pagopa.pu.pagopapayments.service.taxonomy.TaxonomyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaxonomyServiceTest {

  @Mock
  private PagoPaTaxonomyApiClient pagoPaTaxonomyApiClientMock;

  @InjectMocks
  private TaxonomyService taxonomyService;

  @BeforeEach
  void setUp() {
    taxonomyService = new TaxonomyService(pagoPaTaxonomyApiClientMock);
  }



  @Test
  void getTaxonomiesReturnsEmptyListWhenJsonIsEmpty() {
    // Given
    List<PaTaxonomyDTO> expectedTaxonomies = Collections.emptyList();
    when(pagoPaTaxonomyApiClientMock.getTaxonomies()).thenReturn(expectedTaxonomies);

    // When
    List<Taxonomy> actualTaxonomies = taxonomyService.getTaxonomies();

    // Then
    assertTrue(actualTaxonomies.isEmpty());
    verify(pagoPaTaxonomyApiClientMock, times(1)).getTaxonomies();
  }

  @Test
  void getTaxonomiesReturnsListOfTaxonomies() {
    // Given
    PaTaxonomyDTO paTaxonomyDTO = PaTaxonomyDTO.builder()
      .id(1L)
      .codiceTipoEnte("01")
      .descrizioneTipoEnte("ente")
      .codiceTipoServizio("01")
      .tipoServizio("tipoServizio")
      .descrizioneTipoServizio("tipoServizioDesc")
      .progressivoMacroArea("01")
      .descricioneMacroArea("macroArea")
      .nomeMacroArea("macroArea nome")
      .datiSpecificiIncasso("datiSpecifici")
      .motivoRiscossione("motivoRiscossione")
      .dataInizioValidita(new Date())
      .dataFineValidita(new Date())
      .build();
    List<PaTaxonomyDTO> paTaxonomyDTOList = List.of(paTaxonomyDTO);
    List<Taxonomy> expectedTaxonomies = List.of(PaTaxonomyMapper.map(paTaxonomyDTO));
    when(pagoPaTaxonomyApiClientMock.getTaxonomies()).thenReturn(paTaxonomyDTOList);

    // When
    List<Taxonomy> actualTaxonomies = taxonomyService.getTaxonomies();

    // Then
    assertEquals(expectedTaxonomies, actualTaxonomies);
    verify(pagoPaTaxonomyApiClientMock, times(1)).getTaxonomies();
  }
}
