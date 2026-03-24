package it.gov.pagopa.pu.pagopapayments.registry;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.vavr.Function4;
import it.gov.pagopa.pu.pagopapayments.event.producer.RegistryProducerService;
import it.gov.pagopa.pu.pagopapayments.service.JAXBTransformService;
import it.gov.pagopa.pu.registries.dto.generated.RegistryEventCategory;
import it.gov.pagopa.pu.registries.dto.generated.RegistryEventSubType;
import it.gov.pagopa.pu.registries.dto.generated.RegistryOutcome;
import jakarta.xml.bind.annotation.XmlType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.function.TriFunction;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

@Service
@Slf4j
public class RegistryLogger {

  public static final String PU_ID = "piattaformaunitaria";
  public static final String NODE_ID = "NodoDeiPagamentiSPC";

  public static final String SKIP_PAYLOAD_KEY = "skipPayload";
  public static final String PAYLOAD_KEY = "payload";

  private final ObjectMapper objectMapper;
  private final JAXBTransformService jaxbTransformService;
  private final RegistryProducerService registryProducerService;

  public RegistryLogger(ObjectMapper objectMapper, JAXBTransformService jaxbTransformService, RegistryProducerService registryProducerService) {
    this.objectMapper = objectMapper;
    this.jaxbTransformService = jaxbTransformService;
    this.registryProducerService = registryProducerService;
  }

  public <I, O> O execute(
    RegistryContextData contextData, I request,
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
    RegistryContextData contextData, I request,
    Supplier<Triple<O, String, RegistryOutcome>> requestHandler,
    Function<Exception, O> exceptionHandler,
    Supplier<Map<String, Object>> registryBodyRequestExtraInfoRetriever,
    Function<O, Map<String, Object>> registryBodyResponseExtraInfoExtractor
  ) {
    preExecute(contextData, request, registryBodyRequestExtraInfoRetriever);

    O response = null;
    String responseIuv = null;
    RegistryOutcome outcome = RegistryOutcome.KO;
    Exception blException = null;
    try {
      Triple<O, String, RegistryOutcome> response2outcome = requestHandler.get();

      response = response2outcome.getLeft();
      responseIuv = response2outcome.getMiddle();
      outcome = response2outcome.getRight();
    } catch (Exception e) {
      if (exceptionHandler == null) {
        blException = e;
        throw e;
      }
      try {
        response = exceptionHandler.apply(e);
      } catch (Exception e2) {
        blException = e2;
        throw e2;
      }
    } finally {
      postExecute(contextData, registryBodyResponseExtraInfoExtractor, responseIuv, response, outcome, blException);
    }
    return response;
  }

//region executeX methods
  public <I, O, A1> O execute1(
    RegistryContextData contextData, I request,
    Function<A1, Triple<O, String, RegistryOutcome>> requestHandler,
    Function<Exception, O> exceptionHandler,
    A1 arg1
  ) {
    return this.execute1(contextData, request, requestHandler, exceptionHandler,
      null, null,
      arg1
    );
  }

  public <I, O, A1> O execute1(
    RegistryContextData contextData,
    I request,
    Function<A1, Triple<O, String, RegistryOutcome>> requestHandler,
    Function<Exception, O> exceptionHandler,
    Supplier<Map<String, Object>> registryBodyRequestExtraInfoRetriever,
    Function<O, Map<String, Object>> registryBodyResponseExtraInfoExtractor,
    A1 arg1
  ) {
    preExecute(contextData, request, registryBodyRequestExtraInfoRetriever);

    O response = null;
    String responseIuv = null;
    RegistryOutcome outcome = RegistryOutcome.KO;
    Exception blException = null;
    try {
      Triple<O, String, RegistryOutcome> response2outcome = requestHandler.apply(arg1);

      response = response2outcome.getLeft();
      responseIuv = response2outcome.getMiddle();
      outcome = response2outcome.getRight();
    } catch (Exception e) {
      if (exceptionHandler == null) {
        blException = e;
        throw e;
      }
      try {
        response = exceptionHandler.apply(e);
      } catch (Exception e2) {
        blException = e2;
        throw e2;
      }
    } finally {
      postExecute(contextData, registryBodyResponseExtraInfoExtractor, responseIuv, response, outcome, blException);
    }
    return response;
  }

  public <I, O, A1, A2> O execute2(
    RegistryContextData contextData, I request,
    BiFunction<A1, A2, Triple<O, String, RegistryOutcome>> requestHandler,
    Function<Exception, O> exceptionHandler,
    A1 arg1, A2 arg2
  ) {
    return this.execute2(contextData, request, requestHandler, exceptionHandler,
      null, null,
      arg1, arg2
    );
  }

  @SuppressWarnings("squid:S107") // suppressing too many parameters exception: method introduced to reduce object creation
  public <I, O, A1, A2> O execute2(
    RegistryContextData contextData, I request,
    BiFunction<A1, A2, Triple<O, String, RegistryOutcome>> requestHandler,
    Function<Exception, O> exceptionHandler,
    Supplier<Map<String, Object>> registryBodyRequestExtraInfoRetriever,
    Function<O, Map<String, Object>> registryBodyResponseExtraInfoExtractor,
    A1 arg1, A2 arg2
  ) {
    preExecute(contextData, request, registryBodyRequestExtraInfoRetriever);

    O response = null;
    String responseIuv = null;
    RegistryOutcome outcome = RegistryOutcome.KO;
    Exception blException = null;
    try {
      Triple<O, String, RegistryOutcome> response2outcome = requestHandler.apply(arg1, arg2);

      response = response2outcome.getLeft();
      responseIuv = response2outcome.getMiddle();
      outcome = response2outcome.getRight();
    } catch (Exception e) {
      if (exceptionHandler == null) {
        blException = e;
        throw e;
      }
      try {
        response = exceptionHandler.apply(e);
      } catch (Exception e2) {
        blException = e2;
        throw e2;
      }
    } finally {
      postExecute(contextData, registryBodyResponseExtraInfoExtractor, responseIuv, response, outcome, blException);
    }
    return response;
  }

  public <I, O, A1, A2, A3> O execute3(
    RegistryContextData contextData, I request,
    TriFunction<A1, A2, A3, Triple<O, String, RegistryOutcome>> requestHandler,
    Function<Exception, O> exceptionHandler,
    A1 arg1, A2 arg2, A3 arg3
  ) {
    return this.execute3(contextData, request, requestHandler, exceptionHandler,
      null, null,
      arg1, arg2, arg3
    );
  }

  @SuppressWarnings("squid:S107") // suppressing too many parameters exception: method introduced to reduce object creation
  public <I, O, A1, A2, A3> O execute3(
    RegistryContextData contextData, I request,
    TriFunction<A1, A2, A3, Triple<O, String, RegistryOutcome>> requestHandler,
    Function<Exception, O> exceptionHandler,
    Supplier<Map<String, Object>> registryBodyRequestExtraInfoRetriever,
    Function<O, Map<String, Object>> registryBodyResponseExtraInfoExtractor,
    A1 arg1, A2 arg2, A3 arg3
  ) {
    preExecute(contextData, request, registryBodyRequestExtraInfoRetriever);

    O response = null;
    String responseIuv = null;
    RegistryOutcome outcome = RegistryOutcome.KO;
    Exception blException = null;
    try {
      Triple<O, String, RegistryOutcome> response2outcome = requestHandler.apply(arg1, arg2, arg3);

      response = response2outcome.getLeft();
      responseIuv = response2outcome.getMiddle();
      outcome = response2outcome.getRight();
    } catch (Exception e) {
      if (exceptionHandler == null) {
        blException = e;
        throw e;
      }
      try {
        response = exceptionHandler.apply(e);
      } catch (Exception e2) {
        blException = e2;
        throw e2;
      }
    } finally {
      postExecute(contextData, registryBodyResponseExtraInfoExtractor, responseIuv, response, outcome, blException);
    }
    return response;
  }

  @SuppressWarnings("squid:S107") // suppressing too many parameters exception: method introduced to reduce object creation
  public <I, O, A1, A2, A3, A4> O execute4(
    RegistryContextData contextData, I request,
    Function4<A1, A2, A3, A4, Triple<O, String, RegistryOutcome>> requestHandler,
    Function<Exception, O> exceptionHandler,
    A1 arg1, A2 arg2, A3 arg3, A4 arg4
  ) {
    return this.execute4(contextData, request, requestHandler, exceptionHandler,
      null, null,
      arg1, arg2, arg3, arg4
    );
  }

  @SuppressWarnings("squid:S107") // suppressing too many parameters exception: method introduced to reduce object creation
  public <I, O, A1, A2, A3, A4> O execute4(
    RegistryContextData contextData, I request,
    Function4<A1, A2, A3, A4, Triple<O, String, RegistryOutcome>> requestHandler,
    Function<Exception, O> exceptionHandler,
    Supplier<Map<String, Object>> registryBodyRequestExtraInfoRetriever,
    Function<O, Map<String, Object>> registryBodyResponseExtraInfoExtractor,
    A1 arg1, A2 arg2, A3 arg3, A4 arg4
  ) {
    preExecute(contextData, request, registryBodyRequestExtraInfoRetriever);

    O response = null;
    String responseIuv = null;
    RegistryOutcome outcome = RegistryOutcome.KO;
    Exception blException = null;
    try {
      Triple<O, String, RegistryOutcome> response2outcome = requestHandler.apply(arg1, arg2, arg3, arg4);

      response = response2outcome.getLeft();
      responseIuv = response2outcome.getMiddle();
      outcome = response2outcome.getRight();
    } catch (Exception e) {
      if (exceptionHandler == null) {
        blException = e;
        throw e;
      }
      try {
        response = exceptionHandler.apply(e);
      } catch (Exception e2) {
        blException = e2;
        throw e2;
      }
    } finally {
      postExecute(contextData, registryBodyResponseExtraInfoExtractor, responseIuv, response, outcome, blException);
    }
    return response;
  }
//endregion

  <I> void preExecute(RegistryContextData contextData, I request, Supplier<Map<String, Object>> registryBodyRequestExtraInfoRetriever) {
    produceReqRegistryEvent(
      contextData,
      request,
      registryBodyRequestExtraInfoRetriever
    );
  }

  <O> void postExecute(RegistryContextData contextData, Function<O, Map<String, Object>> registryBodyResponseExtraInfoExtractor, String responseIuv, O response, RegistryOutcome outcome, Exception blException) {
    contextData.setIuv(StringUtils.firstNonBlank(responseIuv, contextData.getIuv()));
    produceRespRegistryEvent(
      contextData,
      response,
      outcome,
      registryBodyResponseExtraInfoExtractor,
      blException
    );
  }

  private <I> void produceReqRegistryEvent(
    RegistryContextData contextData,
    I request,
    Supplier<Map<String, Object>> registryBodyRequestExtraInfoRetriever
  ) {
    try {
      Object body;
      if (registryBodyRequestExtraInfoRetriever != null) {
        Map<String, Object> bodyMap = new HashMap<>(registryBodyRequestExtraInfoRetriever.get());
        body = bodyMap2Body(request, bodyMap);
      } else {
        body = serializePayload(request);
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
    Function<O, Map<String, Object>> registryBodyResponseExtraInfoExtractor,
    Exception blException) {
    try {
      Object body;
      if (registryBodyResponseExtraInfoExtractor != null && response != null) {
        Map<String, Object> bodyMap = new HashMap<>(registryBodyResponseExtraInfoExtractor.apply(response));
        body = bodyMap2Body(response, bodyMap);
      } else if (blException != null) {
        if(blException instanceof RestClientResponseException httpStatusCodeException){
          body = Map.of(
            "status", httpStatusCodeException.getStatusCode().value(),
            "body", httpStatusCodeException.getResponseBodyAsString());
        } else {
          body = Map.of("exceptionMessage", blException.getMessage());
        }
      } else {
        body = serializePayload(response);
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
      // In case of error in producing the response event, we do not throw an exception to avoid breaking the flow,
      // but we log the error.
    }
  }

  private <I> Object bodyMap2Body(I payload, Map<String, Object> bodyMap) {
    Object body;
    if (bodyMap.containsKey(SKIP_PAYLOAD_KEY)) {
      bodyMap.remove(SKIP_PAYLOAD_KEY);
    } else if (payload != null) {
      bodyMap.put(PAYLOAD_KEY, serializePayload(payload));
    }
    if (bodyMap.size() == 1) {
      Object justBody = bodyMap.get(PAYLOAD_KEY);
      body = Objects.requireNonNullElse(justBody, bodyMap);
    } else {
      body = bodyMap;
    }
    return body;
  }

  private <I> String serializePayload(I payload) {
    if (payload != null) {
      try {
        if (payload.getClass().getAnnotation(XmlType.class) != null) {
          //noinspection unchecked: it will necessarily be the right class
          return jaxbTransformService.marshalling(payload, (Class<I>) payload.getClass());
        } else return objectMapper.writeValueAsString(payload);
      } catch (Exception e) {
        log.error("Cannot deserialize payload", e);
        return payload.toString();
      }
    } else {
      return null;
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
