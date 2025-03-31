package it.gov.pagopa.pu.pagopapayments.mapper;

import gov.telematici.pagamenti.ws.TipoIdRendicontazione;
import it.gov.pagopa.pu.pagopapayments.dto.generated.PaymentsReportingIdDTO;
import it.gov.pagopa.pu.pagopapayments.util.ConversionUtils;

public class PaymentsReportingIdMapper {


  public static final String PAYMENTS_REPORTING_FILE_EXTENSION = ".xml";

  private PaymentsReportingIdMapper() {}

  public static PaymentsReportingIdDTO map(TipoIdRendicontazione tipoIdRendicontazione){
    if(tipoIdRendicontazione==null){
      return null;
    }

    return PaymentsReportingIdDTO.builder()
      .pagopaPaymentsReportingId(tipoIdRendicontazione.getIdentificativoFlusso())
      .flowDateTime(ConversionUtils.toOffsetDateTime(tipoIdRendicontazione.getDataOraFlusso()))
      .paymentsReportingFileName(getFileName(tipoIdRendicontazione))
      .build();

  }

  private static String getFileName(TipoIdRendicontazione tipoIdRendicontazione){
    if(tipoIdRendicontazione==null){
      return null;
    }
    if(tipoIdRendicontazione.getIdentificativoFlusso()==null || tipoIdRendicontazione.getDataOraFlusso()==null){
      return null;
    }
    return tipoIdRendicontazione.getIdentificativoFlusso()+tipoIdRendicontazione.getDataOraFlusso().toString()+ PAYMENTS_REPORTING_FILE_EXTENSION;
  }

  public static boolean validateFileName(String fileName, String paymentsReportingId){
    return fileName.startsWith(paymentsReportingId) && fileName.endsWith(PAYMENTS_REPORTING_FILE_EXTENSION);
  }

}
