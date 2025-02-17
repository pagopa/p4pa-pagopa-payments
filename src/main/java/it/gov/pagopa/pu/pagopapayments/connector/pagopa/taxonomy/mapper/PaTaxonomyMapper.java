package it.gov.pagopa.pu.pagopapayments.connector.pagopa.taxonomy.mapper;

import it.gov.pagopa.pu.pagopapayments.connector.pagopa.taxonomy.dto.PaTaxonomyDTO;
import it.gov.pagopa.pu.pagopapayments.dto.generated.TaxonomyDTO;
import it.gov.pagopa.pu.pagopapayments.util.ConversionUtils;

public class PaTaxonomyMapper {

  private PaTaxonomyMapper(){}

  public static TaxonomyDTO map(PaTaxonomyDTO paTaxonomyDTO) {
    if (paTaxonomyDTO == null) {
      return null;
    }

    return TaxonomyDTO.builder()
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
      .startDateValidity(ConversionUtils.toOffsetDateTime(paTaxonomyDTO.getDataInizioValidita()))
      .endDateOfValidity(ConversionUtils.toOffsetDateTime(paTaxonomyDTO.getDataFineValidita()))
      .version(paTaxonomyDTO.getVersioneTassonomia())
      .build();
  }
}
