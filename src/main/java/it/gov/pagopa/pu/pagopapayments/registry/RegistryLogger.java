package it.gov.pagopa.pu.pagopapayments.registry;

import it.gov.pagopa.pu.pagopapayments.enums.RegistryEventCategory;
import it.gov.pagopa.pu.pagopapayments.enums.RegistryEventOutcome;
import it.gov.pagopa.pu.pagopapayments.enums.RegistryEventSubType;
import it.gov.pagopa.pu.pagopapayments.enums.RegistryEventType;
import it.gov.pagopa.pu.pagopapayments.event.producer.RegistryProducerService;
import it.gov.pagopa.pu.pagopapayments.service.JAXBTransformService;
import it.gov.pagopa.pu.pagopapayments.util.IdentityUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

@Service
@Slf4j
public class RegistryLogger {

  public static final String SKIP_XML_BODY_KEY = "skipXmlBody";
  public static final String XML_BODY_KEY = "xmlBody";
  public static final String IUV_SEPARATOR = ",";

  private final JAXBTransformService jaxbTransformService;

  private final RegistryProducerService registryProducerService;

  public RegistryLogger(JAXBTransformService jaxbTransformService, RegistryProducerService registryProducerService) {
    this.jaxbTransformService = jaxbTransformService;
    this.registryProducerService = registryProducerService;
  }

  public <I, O> O execute(
    String orgFiscalCode,
    String brokerStationId,
    String pspId,
    String pspChannelId,
    String paymentMethod,
    String ccp,
    RegistryEventType eventType,
    String iuv,
    I request,
    Supplier<Triple<O, String, RegistryEventOutcome>> requestHandler,
    Function<Exception, O> exceptionHandler
  ) {
    return this.execute(
      orgFiscalCode,
      brokerStationId,
      pspId,
      pspChannelId,
      paymentMethod,
      ccp,
      eventType,
      iuv,
      request,
      requestHandler,
      exceptionHandler,
      null,
      null
    );
  }

  public <I, O> O execute(
    String orgFiscalCode,
    String brokerStationId,
    String pspId,
    String pspChannelId,
    String paymentMethod,
    String ccp,
    RegistryEventType eventType,
    String iuv,
    I request,
    Supplier<Triple<O, String, RegistryEventOutcome>> requestHandler,
    Function<Exception, O> exceptionHandler,
    Supplier<Map<String, Object>> registryBodyRequestExtraInfoRetriever,
    Function<O, Map<String, Object>> registryBodyResponseExtraInfoExtractor
  ) {
    produceReqRegistryEvent(
      orgFiscalCode,
      brokerStationId,
      pspId,
      pspChannelId,
      paymentMethod,
      ccp,
      eventType,
      iuv,
      request,
      registryBodyRequestExtraInfoRetriever
    );
    Triple<O, String, RegistryEventOutcome> response2outcome = Triple.of(null, null, RegistryEventOutcome.KO);
    try {
      response2outcome = requestHandler.get();
    } catch (Exception e) {
      response2outcome = Triple.of(exceptionHandler.apply(e), null, RegistryEventOutcome.KO);
    } finally {
      produceRespRegistryEvent(
        orgFiscalCode,
        brokerStationId,
        pspId,
        pspChannelId,
        paymentMethod,
        ccp,
        eventType,
        StringUtils.firstNonBlank(response2outcome.getMiddle(), iuv),
        response2outcome.getLeft(),
        response2outcome.getRight(),
        registryBodyResponseExtraInfoExtractor
      );
    }
    return response2outcome.getLeft();
  }

  private <I> void produceReqRegistryEvent(
    String orgFiscalCode,
    String brokerStationId,
    String pspId,
    String pspChannelId,
    String paymentMethod,
    String ccp,
    RegistryEventType eventType,
    String iuv,
    I request,
    Supplier<Map<String, Object>> registryBodyRequestExtraInfoRetriever
  ) {
    try {
      Object body = null;
      if (registryBodyRequestExtraInfoRetriever != null) {
        Map<String, Object> bodyMap = new HashMap<>(registryBodyRequestExtraInfoRetriever.get());
        if (bodyMap.containsKey(SKIP_XML_BODY_KEY)) {
          bodyMap.remove(SKIP_XML_BODY_KEY);
        } else if (request != null) {
          bodyMap.put(XML_BODY_KEY, jaxbTransformService.marshalling(request, (Class<I>) request.getClass()));
        }
        body = bodyMap;
      } else if (request != null) {
        body = jaxbTransformService.marshalling(request, (Class<I>) request.getClass());
      }
      produceRegistryEvent(
        orgFiscalCode,
        brokerStationId,
        pspId,
        pspChannelId,
        paymentMethod,
        ccp,
        eventType,
        iuv,
        body,
        RegistryEventSubType.REQ,
        RegistryEventOutcome.OK
      );
    } catch (Exception e) {
      log.error("Error producing request registry event for orgFiscalCode: {}, eventType: {}, iuv: {}", orgFiscalCode, eventType, iuv, e);
      // In case of error in producing the request event, we do not throw an exception to avoid breaking the flow
      // but we log the error and continue with the response event.
    }
  }

  private <O> void produceRespRegistryEvent(
    String orgFiscalCode,
    String brokerStationId,
    String pspId,
    String pspChannelId,
    String paymentMethod,
    String ccp,
    RegistryEventType eventType,
    String iuv,
    O response,
    RegistryEventOutcome outcome,
    Function<O, Map<String, Object>> registryBodyResponseExtraInfoExtractor
  ) {
    try {
      Object body = null;
      if (registryBodyResponseExtraInfoExtractor != null) {
        Map<String, Object> bodyMap = new HashMap<>(registryBodyResponseExtraInfoExtractor.apply(response));
        if (bodyMap.containsKey(SKIP_XML_BODY_KEY)) {
          bodyMap.remove(SKIP_XML_BODY_KEY);
        } else if (response != null) {
          bodyMap.put(XML_BODY_KEY, jaxbTransformService.marshalling(response, (Class<O>) response.getClass()));
        }
        body = bodyMap;
      } else if (response != null) {
        body = jaxbTransformService.marshalling(response, (Class<O>) response.getClass());
      }
      produceRegistryEvent(
        orgFiscalCode,
        brokerStationId,
        pspId,
        pspChannelId,
        paymentMethod,
        ccp,
        eventType,
        iuv,
        body,
        RegistryEventSubType.RESP,
        outcome
      );
    } catch (Exception e) {
      log.error("Error producing response registry event for orgFiscalCode: {}, eventType: {}, iuv: {}", orgFiscalCode, eventType, iuv, e);
      // In case of error in producing the response event, we do not throw an exception to avoid breaking the flow
      // but we log the error.
    }
  }

  private void produceRegistryEvent(
    String orgFiscalCode,
    String brokerStationId,
    String pspId,
    String pspChannelId,
    String paymentMethod,
    String ccp,
    RegistryEventType eventType,
    String iuv,
    Object body,
    RegistryEventSubType eventSubType,
    RegistryEventOutcome outcome
  ) {
    String requestorId;
    String grantorId;
    if (eventType.isExposedByPU() && RegistryEventSubType.REQ.equals(eventSubType)) {
      requestorId = RegistryProducerService.NODE_ID;
      grantorId = RegistryProducerService.PU_ID;
    } else if (eventType.isExposedByPU() && RegistryEventSubType.RESP.equals(eventSubType)) {
      requestorId = RegistryProducerService.PU_ID;
      grantorId = RegistryProducerService.NODE_ID;
    } else if (RegistryEventSubType.REQ.equals(eventSubType)) {
      requestorId = RegistryProducerService.PU_ID;
      grantorId = RegistryProducerService.NODE_ID;
    } else {
      requestorId = RegistryProducerService.NODE_ID;
      grantorId = RegistryProducerService.PU_ID;
    }

    registryProducerService.notifySilEvent(
      orgFiscalCode,
      brokerStationId,
      pspId,
      pspChannelId,
      paymentMethod,
      ccp,
      eventType,
      eventSubType,
      RegistryEventCategory.INTERFACE,
      requestorId,
      grantorId,
      iuv,
      IdentityUtils.iuv2Nav(iuv),
      outcome,
      body);
  }
}
