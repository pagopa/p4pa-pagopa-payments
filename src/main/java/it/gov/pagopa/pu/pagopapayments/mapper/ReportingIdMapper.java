package it.gov.pagopa.pu.pagopapayments.mapper;

import gov.telematici.pagamenti.ws.TipoIdRendicontazione;
import it.gov.pagopa.pu.pagopapayments.dto.generated.ReportingIdDTO;
import it.gov.pagopa.pu.pagopapayments.util.ConversionUtils;

public class ReportingIdMapper {


  private ReportingIdMapper() {}

  public static ReportingIdDTO map(TipoIdRendicontazione tipoIdRendicontazione){
    return ReportingIdDTO.builder()
      .reportId(tipoIdRendicontazione.getIdentificativoFlusso())
      .reportDate(ConversionUtils.toOffsetDateTime(tipoIdRendicontazione.getDataOraFlusso()))
      .build();

  }

}
