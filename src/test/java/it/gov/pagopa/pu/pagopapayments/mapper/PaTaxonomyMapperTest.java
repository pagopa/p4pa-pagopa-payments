package it.gov.pagopa.pu.pagopapayments.mapper;

import it.gov.pagopa.pu.pagopapayments.dto.PaTaxonomyDTO;
import it.gov.pagopa.pu.pagopapayments.dto.generated.Taxonomy;
import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PaTaxonomyMapperTest {

    @Test
  void testMapWithValidPaTaxonomyDTO() {
    // Given
    PaTaxonomyDTO paTaxonomyDTO = new PaTaxonomyDTO();
    paTaxonomyDTO.setId(1L);
    paTaxonomyDTO.setDatiSpecificiIncasso("9/023434");
    paTaxonomyDTO.setCodiceTipoEnte("typeCode");
    paTaxonomyDTO.setDescrizioneTipoEnte("typeDescription");
    paTaxonomyDTO.setProgressivoMacroArea("macroAreaCode");
    paTaxonomyDTO.setNomeMacroArea("macroAreaName");
    paTaxonomyDTO.setDescricioneMacroArea("macroAreaDescription");
    paTaxonomyDTO.setCodiceTipoServizio("serviceTypeCode");
    paTaxonomyDTO.setTipoServizio("serviceType");
    paTaxonomyDTO.setDescrizioneTipoServizio("serviceTypeDescription");
    paTaxonomyDTO.setMotivoRiscossione("collectionReason");
    paTaxonomyDTO.setDataInizioValidita(new Date());
    paTaxonomyDTO.setDataFineValidita(new Date());

    // When
    Taxonomy taxonomy = PaTaxonomyMapper.map(paTaxonomyDTO);

    // Then
    assertEquals(paTaxonomyDTO.getId(), taxonomy.getTaxonomyId());
    assertEquals(paTaxonomyDTO.getDatiSpecificiIncasso(), taxonomy.getTaxonomyCode());
    assertEquals(paTaxonomyDTO.getCodiceTipoEnte(), taxonomy.getOrganizationType());
    assertEquals(paTaxonomyDTO.getDescrizioneTipoEnte(), taxonomy.getOrganizationTypeDescription());
    assertEquals(paTaxonomyDTO.getProgressivoMacroArea(), taxonomy.getMacroAreaCode());
    assertEquals(paTaxonomyDTO.getNomeMacroArea(), taxonomy.getMacroAreaName());
    assertEquals(paTaxonomyDTO.getDescricioneMacroArea(), taxonomy.getMacroAreaDescription());
    assertEquals(paTaxonomyDTO.getCodiceTipoServizio(), taxonomy.getServiceTypeCode());
    assertEquals(paTaxonomyDTO.getTipoServizio(), taxonomy.getServiceType());
    assertEquals(paTaxonomyDTO.getDescrizioneTipoServizio(), taxonomy.getServiceTypeDescription());
    assertEquals(paTaxonomyDTO.getMotivoRiscossione(), taxonomy.getCollectionReason());
    assertEquals(paTaxonomyDTO.getDataInizioValidita().toInstant().atZone(ZoneId.of("Europe/Rome")).toOffsetDateTime(), taxonomy.getStartDateValidity());
    assertEquals(paTaxonomyDTO.getDataFineValidita().toInstant().atZone(ZoneId.of("Europe/Rome")).toOffsetDateTime(), taxonomy.getEndDateOfValidity());
  }

  @Test
  void testMapWithNullPaTaxonomyDTO() {
    // Given
    PaTaxonomyDTO paTaxonomyDTO = null;

    // When
    Taxonomy taxonomy = PaTaxonomyMapper.map(paTaxonomyDTO);

    // Then
    assertNull(taxonomy);
  }
}
