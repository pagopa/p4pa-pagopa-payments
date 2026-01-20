package it.gov.pagopa.pu.pagopapayments.mapper;

import gov.telematici.pagamenti.ws.TipoIdRendicontazione;
import it.gov.digitpa.schemas._2011.pagamenti.*;
import it.gov.pagopa.nodo.fdrorganization.dto.generated.*;
import it.gov.pagopa.pu.pagopapayments.dto.BrokerForNodoPaDTO;
import it.gov.pagopa.pu.pagopapayments.dto.PaPaymentReportingDTO;
import it.gov.pagopa.pu.pagopapayments.dto.generated.PaymentsReportingIdDTO;
import it.gov.pagopa.pu.pagopapayments.service.JAXBTransformService;
import it.gov.pagopa.pu.pagopapayments.util.ConversionUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class PaymentsReportingMapper {

  public static final String PAYMENTS_REPORTING_FILE_EXTENSION = ".xml";
  public static final String XML_OBJECT_VERSION = "1.0";

  private final JAXBTransformService jaxbTransformService;

  public PaymentsReportingMapper(JAXBTransformService jaxbTransformService) {
    this.jaxbTransformService = jaxbTransformService;
  }

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

    return mapIdDto(flowByPSP.getFdr(), flowByPSP.getRevision(), flowByPSP.getPspId(), flowByPSP.getFlowDate());
  }

  public PaymentsReportingIdDTO mapIdDto(TipoIdRendicontazione tipoIdRendicontazione){
    if(tipoIdRendicontazione==null){
      return null;
    }
    OffsetDateTime flowDate = ConversionUtils.toOffsetDateTime(tipoIdRendicontazione.getDataOraFlusso());

    return mapIdDto(tipoIdRendicontazione.getIdentificativoFlusso(), null, null, flowDate);
  }

  private PaymentsReportingIdDTO mapIdDto(String flowId, Long revision, String pspId, OffsetDateTime flowDate) {
    return PaymentsReportingIdDTO.builder()
      .pagopaPaymentsReportingId(flowId)
      .revision(Optional.ofNullable(revision).map(Long::intValue).orElse(null))
      .pspId(pspId)
      .flowDateTime(flowDate)
      .paymentsReportingFileName(getFileName(flowId, flowDate))
      .build();
  }

  private String getFileName(String flowId, OffsetDateTime flowDate) {
    if(flowId == null || flowDate == null) {
      return null;
    }
    return flowId + flowDate + PAYMENTS_REPORTING_FILE_EXTENSION;
  }

  public boolean isFilenameInvalid(String filename, String paymentsReportingId) {
    return !filename.startsWith(paymentsReportingId) || !filename.endsWith(PAYMENTS_REPORTING_FILE_EXTENSION);
  }

  public PaPaymentReportingDTO mapPaPaymentsReportingDTO(BrokerForNodoPaDTO brokerForNodoPaDTO, FlussoRiversamento flussoRiversamento) {
    String paymentReportingMarshalling = jaxbTransformService.marshalling(flussoRiversamento, FlussoRiversamento.class);
    return PaPaymentReportingDTO.builder()
      .idPA(brokerForNodoPaDTO.getOrganization().getOrgFiscalCode())
      .idBrokerPA(brokerForNodoPaDTO.getBroker().getBrokerFiscalCode())
      .idStation(brokerForNodoPaDTO.getBroker().getStationId())
      .fiscalCode(brokerForNodoPaDTO.getOrganization().getOrgFiscalCode())
      .paymentReportingBytes(paymentReportingMarshalling.getBytes(StandardCharsets.UTF_8))
      .build();
  }

  public FlussoRiversamento mapPaymentsReporting(SingleFlowResponse singleFlowResponse, List<Payment> paymentList) {
    if(singleFlowResponse == null) {
      return null;
    }
    FlussoRiversamento flussoRiversamento = new FlussoRiversamento();
    flussoRiversamento.setVersioneOggetto(XML_OBJECT_VERSION); //field unused for PaymentsReportingRestServiceImpl but needed for backward compatibility with PaymentsReportingSoapServiceImpl
    flussoRiversamento.setIdentificativoFlusso(singleFlowResponse.getFdr());
    flussoRiversamento.setRevisioneFlusso(Optional.ofNullable(singleFlowResponse.getRevision()).orElse(0L).intValue());
    flussoRiversamento.setDataOraFlusso(ConversionUtils.toXMLGregorianCalendar(singleFlowResponse.getFdrDate()));
    flussoRiversamento.setIdentificativoUnivocoRegolamento(singleFlowResponse.getRegulation());
    flussoRiversamento.setDataRegolamento(ConversionUtils.toXMLGregorianCalendar(singleFlowResponse.getRegulationDate()));
    flussoRiversamento.setIstitutoMittente(this.mapSender(singleFlowResponse.getSender()));
    flussoRiversamento.setCodiceBicBancaDiRiversamento(singleFlowResponse.getBicCodePouringBank());
    flussoRiversamento.setIstitutoRicevente(this.mapReceiver(singleFlowResponse.getReceiver()));
    flussoRiversamento.setNumeroTotalePagamenti(Optional.ofNullable(singleFlowResponse.getTotPayments()).map(BigDecimal::new).orElse(null));
    flussoRiversamento.setImportoTotalePagamenti(Optional.ofNullable(singleFlowResponse.getSumPayments()).map(BigDecimal::new).orElse(null));
    flussoRiversamento.getDatiSingoliPagamentis().addAll(this.mapPaymentList(paymentList));
    return flussoRiversamento;
  }

  private CtIstitutoMittente mapSender(Sender sender) {
    if(sender == null) {
      return null;
    }
    CtIstitutoMittente ctIstitutoMittente = new CtIstitutoMittente();
    ctIstitutoMittente.setDenominazioneMittente(sender.getPspName());
    ctIstitutoMittente.setIdentificativoUnivocoMittente(mapSenderId(sender));
    return ctIstitutoMittente;
  }

  private CtIdentificativoUnivoco mapSenderId(Sender sender) {
    CtIdentificativoUnivoco ctIdentificativoUnivoco = new CtIdentificativoUnivoco();
    ctIdentificativoUnivoco.setCodiceIdentificativoUnivoco(sender.getId());
    ctIdentificativoUnivoco.setTipoIdentificativoUnivoco(mapSenderType(sender.getType()));
    return ctIdentificativoUnivoco;
  }

  private StTipoIdentificativoUnivoco mapSenderType(SenderTypeEnum senderType) {
    return switch (senderType) {
      case LEGAL_PERSON -> StTipoIdentificativoUnivoco.G;
      case ABI_CODE -> StTipoIdentificativoUnivoco.A;
      case BIC_CODE -> StTipoIdentificativoUnivoco.B;
    };
  }

  private CtIstitutoRicevente mapReceiver(Receiver receiver) {
    if(receiver == null) {
      return null;
    }
    CtIstitutoRicevente ctIstitutoRicevente = new CtIstitutoRicevente();
    ctIstitutoRicevente.setDenominazioneRicevente(receiver.getOrganizationName());
    ctIstitutoRicevente.setIdentificativoUnivocoRicevente(mapReceiverId(receiver));
    return ctIstitutoRicevente;
  }

  private CtIdentificativoUnivocoPersonaG mapReceiverId(Receiver receiver) {
    CtIdentificativoUnivocoPersonaG ctIdentificativoUnivocoPersonaG = new CtIdentificativoUnivocoPersonaG();
    ctIdentificativoUnivocoPersonaG.setCodiceIdentificativoUnivoco(receiver.getId());
    ctIdentificativoUnivocoPersonaG.setTipoIdentificativoUnivoco(StTipoIdentificativoUnivocoPersG.G);
    return ctIdentificativoUnivocoPersonaG;
  }

  private List<CtDatiSingoliPagamenti> mapPaymentList(List<Payment> paymentList) {
    if(paymentList == null || paymentList.isEmpty()) {
      return new ArrayList<>();
    }
    ArrayList<CtDatiSingoliPagamenti> ctDatiSingoliPagamentiList = new ArrayList<>();
    paymentList.stream().filter(Objects::nonNull).forEach(payment -> {
      CtDatiSingoliPagamenti ctDatiSingoliPagamenti = new CtDatiSingoliPagamenti();
      ctDatiSingoliPagamenti.setIdentificativoUnivocoVersamento(payment.getIuv());
      ctDatiSingoliPagamenti.setIdentificativoUnivocoRiscossione(payment.getIur());
      ctDatiSingoliPagamenti.setIndiceDatiSingoloPagamento(payment.getIndex().intValue());
      ctDatiSingoliPagamenti.setSingoloImportoPagato(BigDecimal.valueOf(payment.getPay()));
      ctDatiSingoliPagamenti.setDataEsitoSingoloPagamento(ConversionUtils.toXMLGregorianCalendar(payment.getPayDate()));
      ctDatiSingoliPagamenti.setCodiceEsitoSingoloPagamento(payment.getPayStatus().getValue());
      ctDatiSingoliPagamentiList.add(ctDatiSingoliPagamenti);
    });
    return ctDatiSingoliPagamentiList;
  }

}
