package it.gov.pagopa.pu.pagopapayments.mapper;

import gov.telematici.pagamenti.ws.TipoIdRendicontazione;
import it.gov.pagopa.nodo.fdrorganization.dto.generated.FlowByPSP;
import it.gov.pagopa.nodo.fdrorganization.dto.generated.Payment;
import it.gov.pagopa.nodo.fdrorganization.dto.generated.SingleFlowResponse;
import it.gov.pagopa.pu.pagopapayments.dto.BrokerForNodoPaDTO;
import it.gov.pagopa.pu.pagopapayments.dto.PaPaymentReportingDTO;
import it.gov.pagopa.pu.pagopapayments.dto.generated.PaymentsReportingIdDTO;
import it.gov.pagopa.pu.pagopapayments.util.ConversionUtils;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PaymentsReportingMapper {

  public static final String PAYMENTS_REPORTING_FILE_EXTENSION = ".xml";

  private PaymentsReportingMapper() {}

  public List<PaymentsReportingIdDTO> mapToIdDtoList(List<FlowByPSP> flowByPSPList) {
    if(flowByPSPList == null || flowByPSPList.isEmpty()) {
      return new ArrayList<>();
    }
    return flowByPSPList.stream()
      .map(this::mapIdDto)
      .toList();
  }

  private PaymentsReportingIdDTO mapIdDto(FlowByPSP flowByPSP) {
    if(flowByPSP==null){
      return null;
    }

    return mapIdDto(flowByPSP.getFdr(), flowByPSP.getRevision(), flowByPSP.getFlowDate());
  }

  public static PaymentsReportingIdDTO mapIdDto(TipoIdRendicontazione tipoIdRendicontazione){
    if(tipoIdRendicontazione==null){
      return null;
    }
    OffsetDateTime flowDate = ConversionUtils.toOffsetDateTime(tipoIdRendicontazione.getDataOraFlusso());

    return mapIdDto(tipoIdRendicontazione.getIdentificativoFlusso(), null, flowDate);
  }

  private static PaymentsReportingIdDTO mapIdDto(String flowId, Long revision, OffsetDateTime flowDate) {
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

  public static boolean isFilenameInvalid(String filename, String paymentsReportingId) {
    return !filename.startsWith(paymentsReportingId) || !filename.endsWith(PAYMENTS_REPORTING_FILE_EXTENSION);
  }

  public PaPaymentReportingDTO mapPaymentsReporting(BrokerForNodoPaDTO brokerForNodoPaDTO, SingleFlowResponse singleFlowResponse, List<Payment> paymentList) {
    return PaPaymentReportingDTO.builder()
      .idPA(brokerForNodoPaDTO.getOrganization().getOrgFiscalCode())
      .idBrokerPA(brokerForNodoPaDTO.getBroker().getBrokerFiscalCode())
      .idStation(brokerForNodoPaDTO.getBroker().getStationId())
      .fiscalCode(brokerForNodoPaDTO.getOrganization().getOrgFiscalCode())
      .paymentReportingBytes(new byte[0]) //TO DO mapping from singleFlowResponse and paymentList to xml (byte array)
      .build();
  }

}
