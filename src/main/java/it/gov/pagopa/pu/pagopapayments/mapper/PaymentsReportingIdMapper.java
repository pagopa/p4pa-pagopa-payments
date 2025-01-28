package it.gov.pagopa.pu.pagopapayments.mapper;

import gov.telematici.pagamenti.ws.TipoIdRendicontazione;
import it.gov.pagopa.pu.pagopapayments.dto.generated.ReportingIdDTO;
import it.gov.pagopa.pu.pagopapayments.util.ConversionUtils;

public class PaymentsReportingIdMapper {


  private PaymentsReportingIdMapper() {}

  public static ReportingIdDTO map(TipoIdRendicontazione tipoIdRendicontazione){
    if(tipoIdRendicontazione==null){
      return null;
    }

    return ReportingIdDTO.builder()
      .reportId(tipoIdRendicontazione.getIdentificativoFlusso())
      .reportDate(ConversionUtils.toOffsetDateTime(tipoIdRendicontazione.getDataOraFlusso()))
      .fileName(getFileName(tipoIdRendicontazione))
      .build();

  }

  private static String getFileName(TipoIdRendicontazione tipoIdRendicontazione){
    if(tipoIdRendicontazione==null){
      return null;
    }
    if(tipoIdRendicontazione.getIdentificativoFlusso()==null || tipoIdRendicontazione.getDataOraFlusso()==null){
      return null;
    }
    return tipoIdRendicontazione.getIdentificativoFlusso()+tipoIdRendicontazione.getDataOraFlusso().toString()+".xml";
  }

}
