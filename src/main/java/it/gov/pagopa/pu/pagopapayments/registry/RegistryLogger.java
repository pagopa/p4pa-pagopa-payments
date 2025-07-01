package it.gov.pagopa.pu.pagopapayments.registry;

import it.gov.pagopa.pu.pagopapayments.event.producer.RegistryProducerService;
import it.gov.pagopa.pu.pagopapayments.service.JAXBTransformService;
import it.gov.pagopa.pu.registries.dto.generated.RegistryEventCategory;
import it.gov.pagopa.pu.registries.dto.generated.RegistryEventSubType;
import it.gov.pagopa.pu.registries.dto.generated.RegistryOutcome;
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

  public static final String PU_ID = "piattaformaunitaria";
  public static final String NODE_ID = "NodoDeiPagamentiSPC";

  public static final String SKIP_XML_BODY_KEY = "skipXmlBody";
  public static final String XML_BODY_KEY = "xmlBody";

  private final JAXBTransformService jaxbTransformService;

  private final RegistryProducerService registryProducerService;

  public RegistryLogger(JAXBTransformService jaxbTransformService, RegistryProducerService registryProducerService) {
    this.jaxbTransformService = jaxbTransformService;
    this.registryProducerService = registryProducerService;
  }

  public <I, O> O execute(
    RegistryContextData contextData,
    I request,
    Supplier<Triple<O, String, RegistryOutcome>> requestHandler,
    Function<Exception, O> exceptionHandler
  ) {
    return this.execute(
      contextData,
      request,
      requestHandler,
      exceptionHandler,
      null,
      null
    );
  }

  public <I, O> O execute(
    RegistryContextData contextData,
    I request,
    Supplier<Triple<O, String, RegistryOutcome>> requestHandler,
    Function<Exception, O> exceptionHandler,
    Supplier<Map<String, Object>> registryBodyRequestExtraInfoRetriever,
    Function<O, Map<String, Object>> registryBodyResponseExtraInfoExtractor
  ) {
    produceReqRegistryEvent(
      contextData,
      request,
      registryBodyRequestExtraInfoRetriever
    );
    Triple<O, String, RegistryOutcome> response2outcome = Triple.of(null, null, RegistryOutcome.KO);
    try {
      response2outcome = requestHandler.get();
    } catch (Exception e) {
      if (exceptionHandler == null) {
        response2outcome = Triple.of(null, null, RegistryOutcome.KO);
        throw e;
      }
      response2outcome = Triple.of(exceptionHandler.apply(e), null, RegistryOutcome.KO);
    } finally {
      contextData.setIuv(StringUtils.firstNonBlank(response2outcome.getMiddle(), contextData.getIuv()));
      produceRespRegistryEvent(
        contextData,
        response2outcome.getLeft(),
        response2outcome.getRight(),
        registryBodyResponseExtraInfoExtractor
      );
    }
    return response2outcome.getLeft();
  }

  private <I> void produceReqRegistryEvent(
    RegistryContextData contextData,
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
          //noinspection unchecked: it will necessarily be the right class
          bodyMap.put(XML_BODY_KEY, jaxbTransformService.marshalling(request, (Class<I>) request.getClass()));
        }
        body = bodyMap;
      } else if (request != null) {
        //noinspection unchecked: it will necessarily be the right class
        body = jaxbTransformService.marshalling(request, (Class<I>) request.getClass());
      }
      produceRegistryEvent(
        contextData,
        body,
        RegistryEventSubType.REQ,
        RegistryOutcome.OK
      );
    } catch (Exception e) {
      log.error("Error producing request registry event for orgFiscalCode: {}, eventType: {}, iuv: {}",
        contextData.getOrgFiscalCode(), contextData.getEventType(), contextData.getIuv(), e);
      // In case of error in producing the request event, we do not throw an exception to avoid breaking the flow
      // but we log the error and continue with the response event.
    }
  }

  private <O> void produceRespRegistryEvent(
    RegistryContextData contextData,
    O response,
    RegistryOutcome outcome,
    Function<O, Map<String, Object>> registryBodyResponseExtraInfoExtractor
  ) {
    try {
      Object body = null;
      if (registryBodyResponseExtraInfoExtractor != null) {
        Map<String, Object> bodyMap = new HashMap<>(registryBodyResponseExtraInfoExtractor.apply(response));
        if (bodyMap.containsKey(SKIP_XML_BODY_KEY)) {
          bodyMap.remove(SKIP_XML_BODY_KEY);
        } else if (response != null) {
          //noinspection unchecked: it will necessarily be the right class
          bodyMap.put(XML_BODY_KEY, jaxbTransformService.marshalling(response, (Class<O>) response.getClass()));
        }
        body = bodyMap;
      } else if (response != null) {
        //noinspection unchecked: it will necessarily be the right class
        body = jaxbTransformService.marshalling(response, (Class<O>) response.getClass());
      }
      produceRegistryEvent(
        contextData,
        body,
        RegistryEventSubType.RESP,
        outcome
      );
    } catch (Exception e) {
      log.error("Error producing response registry event for orgFiscalCode: {}, eventType: {}, iuv: {}",
        contextData.getOrgFiscalCode(), contextData.getEventType(), contextData.getIuv(), e);
      // In case of error in producing the response event, we do not throw an exception to avoid breaking the flow
      // but we log the error.
    }
  }

  private void produceRegistryEvent(
    RegistryContextData contextData,
    Object body,
    RegistryEventSubType eventSubType,
    RegistryOutcome outcome
  ) {
    String requestorId;
    String grantorId;
    if (contextData.getEventType().isExposedByPU() == RegistryEventSubType.REQ.equals(eventSubType)) {
      requestorId = NODE_ID;
      grantorId = PU_ID;
    } else {
      requestorId = PU_ID;
      grantorId = NODE_ID;
    }

    registryProducerService.notifyPagoPaEvent(
      contextData,
      eventSubType,
      RegistryEventCategory.INTERFACCIA,
      requestorId,
      grantorId,
      outcome,
      body);
  }
}
