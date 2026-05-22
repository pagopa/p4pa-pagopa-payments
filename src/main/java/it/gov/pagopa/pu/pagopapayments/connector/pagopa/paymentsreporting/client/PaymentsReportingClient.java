package it.gov.pagopa.pu.pagopapayments.connector.pagopa.paymentsreporting.client;

import it.gov.digitpa.schemas._2011.pagamenti.FlussoRiversamento;
import it.gov.pagopa.nodo.fdrorganization.dto.generated.*;
import it.gov.pagopa.pu.pagopapayments.connector.pagopa.paymentsreporting.config.PaymentsReportingApisHolder;
import it.gov.pagopa.pu.pagopapayments.connector.pagopa.paymentsreporting.mapper.PaymentReporting2NodoChiediFlussoRendicontazioneMapper;
import it.gov.pagopa.pu.pagopapayments.dto.BrokerForNodoPaDTO;
import it.gov.pagopa.pu.pagopapayments.dto.PaPaymentReportingDTO;
import it.gov.pagopa.pu.pagopapayments.mapper.PaymentsReportingMapper;
import it.gov.pagopa.pu.pagopapayments.registry.RegistryContextData;
import it.gov.pagopa.pu.pagopapayments.registry.RegistryEventType;
import it.gov.pagopa.pu.pagopapayments.registry.RegistryLogger;
import it.gov.pagopa.pu.pagopapayments.util.PageUtils;
import it.gov.pagopa.pu.registries.dto.generated.RegistryOutcome;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class PaymentsReportingClient {
  private final PaymentsReportingApisHolder apisHolder;
  private final RegistryLogger registryLogger;
  private final PaymentsReportingMapper paymentsReportingMapper;
  private final PaymentReporting2NodoChiediFlussoRendicontazioneMapper paymentReporting2NodoChiediFlussoRendicontazioneMapper;

  public PaymentsReportingClient(PaymentsReportingApisHolder apisHolder, RegistryLogger registryLogger, PaymentsReportingMapper paymentsReportingMapper, PaymentReporting2NodoChiediFlussoRendicontazioneMapper paymentReporting2NodoChiediFlussoRendicontazioneMapper) {
    this.apisHolder = apisHolder;
    this.registryLogger = registryLogger;
    this.paymentsReportingMapper = paymentsReportingMapper;
    this.paymentReporting2NodoChiediFlussoRendicontazioneMapper = paymentReporting2NodoChiediFlussoRendicontazioneMapper;
  }

  public List<FlowByPSP> fetchIdList(BrokerForNodoPaDTO brokerForNodoPaDTO, OffsetDateTime latestFlowDate) {
    return PageUtils.fetchAllFromPaginatedApi(
        page -> apisHolder.getOrganizationApi(brokerForNodoPaDTO.getBrokerApiKeys().getSyncPaymentsReportingKey())
          .iOrganizationsControllerGetAllPublishedFlows(
            brokerForNodoPaDTO.getOrganization().getOrgFiscalCode(), latestFlowDate,
            (long) page, null, null, null
          ),
        this::isPaginatedFlowsResponseEmpty,
        this::getTotalPageFromPaginatedFlowsResponse,
        PaginatedFlowsResponse::getData
    );
  }

  private boolean isPaginatedFlowsResponseEmpty(PaginatedFlowsResponse paginatedFlowsResponse) {
    return paginatedFlowsResponse == null ||
      paginatedFlowsResponse.getMetadata() == null ||
      paginatedFlowsResponse.getMetadata().getTotPage() == null ||
      paginatedFlowsResponse.getMetadata().getTotPage() == 0;
  }

  private Integer getTotalPageFromPaginatedFlowsResponse(PaginatedFlowsResponse paginatedFlowsResponse) {
    return paginatedFlowsResponse.getMetadata().getTotPage();
  }

  public SingleFlowResponse fetchPaymentReportingFlow(BrokerForNodoPaDTO brokerForNodoPaDTO, String reportingId, Long revision, String pspId) {
    return apisHolder.getOrganizationApi(brokerForNodoPaDTO.getBrokerApiKeys().getSyncPaymentsReportingKey())
      .iOrganizationsControllerGetSinglePublishedFlow(
              reportingId, brokerForNodoPaDTO.getOrganization().getOrgFiscalCode(),
              pspId, revision
      );
  }

  public List<Payment> fetchAllPaymentsForPaymentReportingFlow(BrokerForNodoPaDTO brokerForNodoPaDTO, String reportingId, Long revision, String pspId, SingleFlowResponse singleFlowResponse) {
    RegistryContextData contextData = buildRegistryContextData(brokerForNodoPaDTO, pspId);
    return registryLogger.execute(
      contextData,
      paymentReporting2NodoChiediFlussoRendicontazioneMapper.createFlussoRendicontazioneRequest(
        brokerForNodoPaDTO,
        reportingId
      ),
      () -> fetchAllPaymentsRequestHandler(brokerForNodoPaDTO, reportingId, revision, pspId),
      null,
      null,
      paymentList -> registryBodyResponseRetriever(
        brokerForNodoPaDTO,
        singleFlowResponse,
        paymentList
      )
    );
  }

  private static RegistryContextData buildRegistryContextData(BrokerForNodoPaDTO brokerForNodoPaDTO, String pspId) {
    return RegistryContextData.builder()
      .orgFiscalCode(brokerForNodoPaDTO.getOrganization().getOrgFiscalCode())
      .pspId(pspId)
      .eventType(RegistryEventType.NodeForPa_fetchPaymentReporting)
      .build();
  }

  private Triple<List<Payment>, String, RegistryOutcome> fetchAllPaymentsRequestHandler(BrokerForNodoPaDTO brokerForNodoPaDTO, String reportingId, Long revision, String pspId) {
    List<Payment> responseList = PageUtils.fetchAllFromPaginatedApi(
      page -> apisHolder.getOrganizationApi(brokerForNodoPaDTO.getBrokerApiKeys().getSyncPaymentsReportingKey())
        .iOrganizationsControllerGetPaymentsFromPublishedFlow(
                reportingId, brokerForNodoPaDTO.getOrganization().getOrgFiscalCode(),
                pspId, revision, (long) page, null
        ),
      this::isPaginatedPaymentsResponseEmpty,
      this::getTotalPageFromPaginatedPaymentsResponse,
      PaginatedPaymentsResponse::getData
    );
    return Triple.of(responseList, null, responseList.isEmpty() ? RegistryOutcome.KO : RegistryOutcome.OK);
  }

  private Map<String, Object> registryBodyResponseRetriever(BrokerForNodoPaDTO brokerForNodoPaDTO, SingleFlowResponse singleFlowResponse, List<Payment> paymentList) {
    FlussoRiversamento paymentsReporting = paymentsReportingMapper.mapPaymentsReporting(
      singleFlowResponse,
      paymentList
    );
    PaPaymentReportingDTO paPaymentReportingDTO = paymentsReportingMapper.mapPaPaymentsReportingDTO(
      brokerForNodoPaDTO,
      paymentsReporting
    );
    return Map.of(
      RegistryLogger.SKIP_PAYLOAD_KEY, true,
      "xml", new String(paPaymentReportingDTO.getPaymentReportingBytes())
    );
  }

  private boolean isPaginatedPaymentsResponseEmpty(PaginatedPaymentsResponse paginatedPaymentsResponse) {
    return paginatedPaymentsResponse == null ||
      paginatedPaymentsResponse.getMetadata() == null ||
      paginatedPaymentsResponse.getMetadata().getTotPage() == null ||
      paginatedPaymentsResponse.getMetadata().getTotPage() == 0;
  }

  private Integer getTotalPageFromPaginatedPaymentsResponse(PaginatedPaymentsResponse paginatedPaymentsResponse) {
    return paginatedPaymentsResponse.getMetadata().getTotPage();
  }

}
