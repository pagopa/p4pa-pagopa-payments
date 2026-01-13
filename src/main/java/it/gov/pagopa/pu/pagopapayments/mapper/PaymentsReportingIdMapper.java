package it.gov.pagopa.pu.pagopapayments.mapper;

import gov.telematici.pagamenti.ws.TipoIdRendicontazione;
import it.gov.pagopa.nodo.fdrorganization.dto.generated.FlowByPSP;
import it.gov.pagopa.pu.pagopapayments.dto.generated.PaymentsReportingIdDTO;
import it.gov.pagopa.pu.pagopapayments.util.ConversionUtils;

import java.time.OffsetDateTime;
import java.util.Optional;

public class PaymentsReportingIdMapper {


  public static final String PAYMENTS_REPORTING_FILE_EXTENSION = ".xml";

  private PaymentsReportingIdMapper() {}

  public static PaymentsReportingIdDTO map(FlowByPSP flowByPSP) {
    if(flowByPSP==null){
      return null;
    }

    return map(flowByPSP.getFdr(), flowByPSP.getRevision(), flowByPSP.getFlowDate());
  }

  public static PaymentsReportingIdDTO map(TipoIdRendicontazione tipoIdRendicontazione){
    if(tipoIdRendicontazione==null){
      return null;
    }
    OffsetDateTime flowDate = ConversionUtils.toOffsetDateTime(tipoIdRendicontazione.getDataOraFlusso());

    return map(tipoIdRendicontazione.getIdentificativoFlusso(), null, flowDate);
  }

  public static PaymentsReportingIdDTO map(String flowId, Long revision, OffsetDateTime flowDate) {
    return PaymentsReportingIdDTO.builder()
      .pagopaPaymentsReportingId(flowId)
      .revision(Optional.ofNullable(revision).orElse(0L).intValue()) //TODO check int type for revision
      .flowDateTime(flowDate)
      .paymentsReportingFileName(getFileName(flowId, flowDate))
      .build();
  }

  private static String getFileName(String flowId, OffsetDateTime flowDate) {
    if(flowId == null || flowDate == null) {
      return null;
    }
    return flowId + flowDate + PAYMENTS_REPORTING_FILE_EXTENSION;
  }

  public static boolean isFilenameInvalid(String fileName, String paymentsReportingId) {
    return !fileName.startsWith(paymentsReportingId) || !fileName.endsWith(PAYMENTS_REPORTING_FILE_EXTENSION);
  }

}
