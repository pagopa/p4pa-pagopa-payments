package it.gov.pagopa.pu.pagopapayments.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.pu.organization.dto.generated.Taxonomy;
import it.gov.pagopa.pu.pagopapayments.connector.PagoPaApiClient;
import it.gov.pagopa.pu.pagopapayments.dto.PaTaxonomyDTO;
import it.gov.pagopa.pu.pagopapayments.exception.ApplicationException;
import it.gov.pagopa.pu.pagopapayments.mapper.PaTaxonomyMapper;
import it.gov.pagopa.pu.pagopapayments.service.taxonomy.TaxonomyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaxonomyServiceTest {

  @Mock
  private PagoPaApiClient pagoPaApiClientMock;

  @Mock
  private Jackson2ObjectMapperBuilder mapperBuilderMock;

  @Mock
  private ObjectMapper objectMapperMock;

  @InjectMocks
  private TaxonomyService taxonomyService;

  @BeforeEach
  void setUp() {
    when(mapperBuilderMock.build()).thenReturn(objectMapperMock);
    taxonomyService = new TaxonomyService(pagoPaApiClientMock, mapperBuilderMock);
  }

  @Test
  void testGetTaxonomiesSuccess() throws JsonProcessingException {
    // Given
    String json = "[{\"id\":\"1\"}]";
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
    List<Taxonomy> expectedTaxonomies = List.of(PaTaxonomyMapper.map(paTaxonomyDTOList.get(0)));

    when(pagoPaApiClientMock.getTaxonomies()).thenReturn(json);
    doReturn(paTaxonomyDTOList).when(objectMapperMock).readValue(eq(json), any(TypeReference.class));

    // When
    List<Taxonomy> actualTaxonomies = taxonomyService.getTaxonomies();

    // Then
    assertEquals(expectedTaxonomies, actualTaxonomies);
    verify(pagoPaApiClientMock, times(1)).getTaxonomies();
    verify(objectMapperMock, times(1)).readValue(eq(json), any(TypeReference.class));
  }

  @Test
  void testGetTaxonomiesJsonProcessingException() throws JsonProcessingException {
    // Given
    String json = "invalid json";
    when(pagoPaApiClientMock.getTaxonomies()).thenReturn(json);
    doThrow(new JsonProcessingException("Error") {}).when(objectMapperMock).readValue(eq(json), any(TypeReference.class));

    // When
    ApplicationException exception = assertThrows(ApplicationException.class, () -> taxonomyService.getTaxonomies());

    // Then
    assertEquals("Error", exception.getMessage());
    verify(pagoPaApiClientMock, times(1)).getTaxonomies();
    verify(objectMapperMock, times(1)).readValue(eq(json), any(TypeReference.class));
  }


}
