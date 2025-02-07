package it.gov.pagopa.pu.pagopapayments.mapper;

import it.gov.pagopa.pu.organization.dto.generated.Taxonomy;
import it.gov.pagopa.pu.pagopapayments.dto.PaTaxonomyDTO;

import java.time.ZoneId;

public class PaTaxonomyMapper {

  private PaTaxonomyMapper(){}

  public static Taxonomy map(PaTaxonomyDTO paTaxonomyDTO) {
    if (paTaxonomyDTO == null) {
      return null;
    }

    return Taxonomy.builder()
      .taxonomyId(paTaxonomyDTO.getId())
      .taxonomyCode(paTaxonomyDTO.getDatiSpecificiIncasso())
      .organizationType(paTaxonomyDTO.getCodiceTipoEnte())
      .organizationTypeDescription(paTaxonomyDTO.getDescrizioneTipoEnte())
      .macroAreaCode(paTaxonomyDTO.getProgressivoMacroArea())
      .macroAreaName(paTaxonomyDTO.getNomeMacroArea())
      .macroAreaDescription(paTaxonomyDTO.getDescricioneMacroArea())
      .serviceTypeCode(paTaxonomyDTO.getCodiceTipoServizio())
      .serviceType(paTaxonomyDTO.getTipoServizio())
      .serviceTypeDescription(paTaxonomyDTO.getDescrizioneTipoServizio())
      .collectionReason(paTaxonomyDTO.getMotivoRiscossione())
      .startDateValidity(paTaxonomyDTO.getDataInizioValidita().toInstant().atZone(ZoneId.of("Europe/Rome")).toOffsetDateTime())
      .endDateOfValidity(paTaxonomyDTO.getDataFineValidita().toInstant().atZone(ZoneId.of("Europe/Rome")).toOffsetDateTime())
      .build();
  }
}
