package it.gov.pagopa.pu.pagopapayments.endpoint;

import it.gov.pagopa.pagopa_api.pa.pafornode.*;
import it.gov.pagopa.pagopa_api.xsd.common_types.v1_0.CtFaultBean;
import it.gov.pagopa.pagopa_api.xsd.common_types.v1_0.CtResponse;
import it.gov.pagopa.pagopa_api.xsd.common_types.v1_0.StOutcome;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.organization.dto.generated.Broker;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.pagopapayments.dto.PaSendRtDTO;
import it.gov.pagopa.pu.pagopapayments.dto.RetrievePaymentDTO;
import it.gov.pagopa.pu.pagopapayments.enums.PagoPaNodeFaults;
import it.gov.pagopa.pu.pagopapayments.exception.PagoPaNodeFaultException;
import it.gov.pagopa.pu.pagopapayments.mapper.PaDemandPaymentNoticeMapper;
import it.gov.pagopa.pu.pagopapayments.mapper.PaGetPaymentMapper;
import it.gov.pagopa.pu.pagopapayments.mapper.PaSendRTMapper;
import it.gov.pagopa.pu.pagopapayments.mapper.PaVerifyPaymentNoticeMapper;
import it.gov.pagopa.pu.pagopapayments.registry.RegistryContextData;
import it.gov.pagopa.pu.pagopapayments.registry.RegistryEventType;
import it.gov.pagopa.pu.pagopapayments.registry.RegistryLogger;
import it.gov.pagopa.pu.pagopapayments.service.demandpaymentnotice.DemandPaymentNoticeService;
import it.gov.pagopa.pu.pagopapayments.service.receipt.ReceiptService;
import it.gov.pagopa.pu.pagopapayments.service.synchronouspayments.SynchronousPaymentService;
import it.gov.pagopa.pu.pagopapayments.util.Utilities;
import it.gov.pagopa.pu.registries.dto.generated.RegistryOutcome;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import java.util.Objects;
import java.util.stream.Collectors;

@Endpoint
@Slf4j
public class PaForNodeEndpoint {
  public static final String NAMESPACE_URI = "http://pagopa-api.pagopa.gov.it/pa/paForNode.xsd";
  public static final String NAME = "PaForNode";

  private final SynchronousPaymentService synchronousPaymentService;
  private final ReceiptService receiptService;
  private final PaSendRTMapper paSendRTMapper;
  private final RegistryLogger registryLogger;
  private final DemandPaymentNoticeService demandPaymentNoticeService;


  public PaForNodeEndpoint(SynchronousPaymentService synchronousPaymentService, ReceiptService receiptService, PaSendRTMapper paSendRTMapper, RegistryLogger registryLogger, DemandPaymentNoticeService demandPaymentNoticeService) {
    this.synchronousPaymentService = synchronousPaymentService;
    this.receiptService = receiptService;
    this.paSendRTMapper = paSendRTMapper;
    this.registryLogger = registryLogger;
    this.demandPaymentNoticeService = demandPaymentNoticeService;
  }

  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "paDemandPaymentNoticeRequest")
  @ResponsePayload
  public PaDemandPaymentNoticeResponse paDemandPaymentNotice(@RequestPayload PaDemandPaymentNoticeRequest request) {
    RegistryContextData contextData = RegistryContextData.builder()
      .orgFiscalCode(request.getIdPA())
      .brokerStationId(request.getIdStation())
      .eventType(RegistryEventType.PaForNode_paDemandPaymentNotice)
      .build();

    return registryLogger.execute(
      contextData,
      request,
      () -> {
        log.info("processing paDemandPaymentNotice idPA[{}] servizio[{}/{}]", request.getIdPA(), request.getIdSoggettoServizio(), request.getIdServizio());
        Triple<DebtPositionDTO, Organization, Broker> response = demandPaymentNoticeService.handleRequest(request);
        DebtPositionDTO debtPositionDTO = response.getLeft();
        PaDemandPaymentNoticeResponse resp = PaDemandPaymentNoticeMapper.debtPositionDto2PaVerifyPaymentNoticeRes(debtPositionDTO,response.getMiddle(),response.getRight());
        return Triple.of(resp,
          debtPositionDTO.getPaymentOptions().stream()
            .flatMap(option -> option.getInstallments().stream())
            .map(InstallmentDTO::getIuv)
            .collect(Collectors.joining(",")),
          RegistryOutcome.OK);
      },
      e -> {
        PaDemandPaymentNoticeResponse resp;
        if (Objects.requireNonNull(e) instanceof PagoPaNodeFaultException spe) {
          log.error("Error in paDemandPaymentNotice [{}/{}] {}", request.getIdBrokerPA(), request.getIdServizio(), request.getIdStation(), e);
          resp = handleFault(spe.getErrorCode(), spe.getErrorEmitter(), new PaDemandPaymentNoticeResponse());
        } else {
          log.error("Error in paDemandPaymentNotice [{}/{}] {}", request.getIdBrokerPA(), request.getIdServizio(), request.getIdStation(), e);
          resp = handleFault(PagoPaNodeFaults.PAA_SYSTEM_ERROR, request.getIdBrokerPA(), new PaDemandPaymentNoticeResponse());
        }

        return resp;
      }
    );
  }

  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "paVerifyPaymentNoticeReq")
  @ResponsePayload
  public PaVerifyPaymentNoticeRes paVerifyPaymentNotice(@RequestPayload PaVerifyPaymentNoticeReq request) {
    RegistryContextData contextData = RegistryContextData.builder()
      .orgFiscalCode(request.getIdPA())
      .brokerStationId(request.getIdStation())
      .eventType(RegistryEventType.PaForNode_paVerifyPaymentNotice)
      .iuv(Utilities.nav2Iuv(request.getQrCode().getNoticeNumber()))
      .build();

    return registryLogger.execute(contextData, request, () -> {
        log.info("processing paVerifyPaymentNotice idPA[{}] notice[{}/{}]", request.getIdPA(), request.getQrCode().getFiscalCode(), request.getQrCode().getNoticeNumber());
        RetrievePaymentDTO retrievePaymentDTO = PaVerifyPaymentNoticeMapper.paVerifyPaymentNoticeReq2RetrievePaymentDTO(request);

        Triple<InstallmentDTO, Organization, Broker> triple = synchronousPaymentService.retrievePayment(retrievePaymentDTO);

        return Triple.of(PaVerifyPaymentNoticeMapper.installmentDto2PaVerifyPaymentNoticeRes(
          triple.getLeft(), triple.getMiddle(), triple.getRight()), null, RegistryOutcome.OK);
      },
      e -> {
        if (e instanceof PagoPaNodeFaultException spe) {
          log.error("Fault in paVerifyPaymentNotice [{}/{}] {}", request.getQrCode().getFiscalCode(), request.getQrCode().getNoticeNumber(), spe.getErrorCode());
          return handleFault(spe.getErrorCode(), spe.getErrorEmitter(), new PaVerifyPaymentNoticeRes());
        } else {
          log.error("Error in paVerifyPaymentNotice [{}/{}]", request.getQrCode().getFiscalCode(), request.getQrCode().getNoticeNumber(), e);
          return handleFault(PagoPaNodeFaults.PAA_SYSTEM_ERROR, request.getIdPA(), new PaVerifyPaymentNoticeRes());
        }
      });
  }

  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "paGetPaymentReq")
  @ResponsePayload
  public PaGetPaymentRes paGetPayment(@RequestPayload PaGetPaymentReq request) {
    log.info("processing UNSUPPORTED V1 paGetPayment idPA[{}] notice[{}/{}]", request.getIdPA(), request.getQrCode().getFiscalCode(), request.getQrCode().getNoticeNumber());
    return handleFault(PagoPaNodeFaults.PAA_SYSTEM_ERROR, request.getIdBrokerPA(), new PaGetPaymentRes());
  }

  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "paGetPaymentV2Request")
  @ResponsePayload
  public PaGetPaymentV2Response paGetPaymentV2(@RequestPayload PaGetPaymentV2Request request) {
    RegistryContextData contextData = RegistryContextData.builder()
      .orgFiscalCode(request.getIdPA())
      .brokerStationId(request.getIdStation())
      .eventType(RegistryEventType.PaForNode_paGetPaymentV2)
      .iuv(Utilities.nav2Iuv(request.getQrCode().getNoticeNumber()))
      .build();

    return registryLogger.execute(contextData, request, () -> {
        log.info("processing paGetPaymentV2 idPA[{}] notice[{}/{}]", request.getIdPA(), request.getQrCode().getFiscalCode(), request.getQrCode().getNoticeNumber());
        RetrievePaymentDTO retrievePaymentDTO = PaGetPaymentMapper.paPaGetPaymentV2Request2RetrievePaymentDTO(request);

        Triple<InstallmentDTO, Organization, Broker> triple = synchronousPaymentService.retrievePayment(retrievePaymentDTO);

        return Triple.of(PaGetPaymentMapper.installmentDto2PaGetPaymentV2Response(
            triple.getLeft(), triple.getMiddle(), triple.getRight(), request.getTransferType()), null, RegistryOutcome.OK);
      },
      e -> {
        if (e instanceof PagoPaNodeFaultException spe) {
          log.error("Fault in paGetPaymentV2 [{}/{}] {}", request.getQrCode().getFiscalCode(), request.getQrCode().getNoticeNumber(), spe.getErrorCode());
          return handleFault(spe.getErrorCode(), spe.getErrorEmitter(), new PaGetPaymentV2Response());
        } else {
          log.error("Error in paGetPaymentV2 [{}/{}]", request.getQrCode().getFiscalCode(), request.getQrCode().getNoticeNumber(), e);
          return handleFault(PagoPaNodeFaults.PAA_SYSTEM_ERROR, request.getIdPA(), new PaGetPaymentV2Response());
        }
      });
  }

  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "paSendRTReq")
  @ResponsePayload
  public PaSendRTRes paSendRT(@RequestPayload PaSendRTReq request) {
    log.info("processing UNSUPPORTED V1 paSendRT idPA[{}] notice[{}/{}]", request.getIdPA(), request.getReceipt().getFiscalCode(), request.getReceipt().getNoticeNumber());
    return handleFault(PagoPaNodeFaults.PAA_SYSTEM_ERROR, request.getIdBrokerPA(), new PaSendRTRes());
  }

  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "paSendRTV2Request")
  @ResponsePayload
  public PaSendRTV2Response paSendRTV2(@RequestPayload PaSendRTV2Request request) {
    RegistryContextData contextData = RegistryContextData.builder()
      .orgFiscalCode(request.getReceipt().getFiscalCode())
      .brokerStationId(request.getIdStation())
      .pspId(request.getReceipt().getIdPSP())
      .pspChannelId(request.getReceipt().getIdChannel())
      .paymentMethod(request.getReceipt().getPaymentMethod())
      .ccp(request.getReceipt().getReceiptId())
      .eventType(RegistryEventType.PaForNode_paSendRTV2)
      .iuv(Utilities.nav2Iuv(request.getReceipt().getNoticeNumber()))
      .build();

    return registryLogger.execute(
      contextData,
      request,
      () -> {
        log.info("processing paSendRTV2 idPA[{}] notice[{}/{}]", request.getIdPA(), request.getReceipt().getFiscalCode(), request.getReceipt().getNoticeNumber());
        PaSendRtDTO paSendRtDTO = paSendRTMapper.paSendRtV2Request2PaSendRtDTO(request);
        receiptService.processReceivedReceipt(paSendRtDTO);
        PaSendRTV2Response resp = new PaSendRTV2Response();
        resp.setOutcome(StOutcome.OK);
        return Triple.of(resp, null, RegistryOutcome.OK);
      },
      e -> {
        PaSendRTV2Response resp;

        if (Objects.requireNonNull(e) instanceof PagoPaNodeFaultException spe) {
          log.error("Fault in paSendRTV2 [{}/{}] {}", request.getReceipt().getNoticeNumber(), request.getReceipt().getFiscalCode(), spe.getErrorCode());
          resp = handleFault(spe.getErrorCode(), spe.getErrorEmitter(), new PaSendRTV2Response());
        } else {
          log.error("Error in paSendRTV2 [{}/{}] {}", request.getReceipt().getNoticeNumber(), request.getReceipt().getFiscalCode(), request.getReceipt().getReceiptId(), e);
          resp = handleFault(PagoPaNodeFaults.PAA_SYSTEM_ERROR, request.getIdPA(), new PaSendRTV2Response());
        }

        return resp;
      }
    );
  }

  private <T extends CtResponse> T handleFault(PagoPaNodeFaults fault, String idFaultEmitter, T responseObj) {
    responseObj.setFault(new CtFaultBean());
    responseObj.getFault().setFaultCode(fault.code());
    responseObj.getFault().setDescription(fault.description());
    responseObj.getFault().setFaultString(fault.description());
    responseObj.getFault().setId(idFaultEmitter);
    responseObj.getFault().setSerial(0);
    responseObj.setOutcome(StOutcome.KO);
    return responseObj;
  }

}
