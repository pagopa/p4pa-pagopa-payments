package it.gov.pagopa.pu.pagopapayments.registry;

import it.gov.pagopa.pu.pagopapayments.enums.RegistryEventCategory;
import it.gov.pagopa.pu.pagopapayments.enums.RegistryEventOutcome;
import it.gov.pagopa.pu.pagopapayments.enums.RegistryEventSubType;
import it.gov.pagopa.pu.pagopapayments.enums.RegistryEventType;
import it.gov.pagopa.pu.pagopapayments.event.producer.RegistryProducerService;
import it.gov.pagopa.pu.pagopapayments.service.JAXBTransformService;
import it.gov.pagopa.pu.pagopapayments.util.TestUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.jemos.podam.api.PodamFactory;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistryLoggerTest {

  @Mock
  private JAXBTransformService jaxbTransformService;
  @Mock
  private RegistryProducerService registryProducerService;
  @InjectMocks
  private RegistryLogger registryLogger;

  private final PodamFactory podamFactory = TestUtils.getPodamFactory();

  @Test
  void testProduceReqRegistryEvent() {
    // Given
    RegistryContextData contextData = podamFactory.manufacturePojo(RegistryContextData.class);
    contextData.setEventType(RegistryEventType.paSendRTV2);
    String blIuv = "businessLogicIUV";
    String xmlRequest = "<xml>mockRequest</xml>";
    Object request = new Object();
    when(jaxbTransformService.marshalling(eq(request), any())).thenReturn(xmlRequest);

    // When
    Object actualResponse = registryLogger.execute(
      contextData, request,
      () -> Triple.of(null, blIuv, RegistryEventOutcome.OK),
      e -> null
    );

    // Then
    assertNull(actualResponse);

    verify(registryProducerService).notifyPagoPaEvent(
      contextData,
      RegistryEventSubType.REQ,
      RegistryEventCategory.INTERFACCIA,
      RegistryLogger.NODE_ID,
      RegistryLogger.PU_ID,
      RegistryEventOutcome.OK,
      xmlRequest
    );

    contextData.setIuv(blIuv);
    verify(registryProducerService).notifyPagoPaEvent(
      contextData,
      RegistryEventSubType.RESP,
      RegistryEventCategory.INTERFACCIA,
      RegistryLogger.PU_ID,
      RegistryLogger.NODE_ID,
      RegistryEventOutcome.OK,
      null
    );
  }

  @Test
  void testProduceReqRegistryEventWithExtraInfo() {
    // Given
    RegistryContextData contextData = podamFactory.manufacturePojo(RegistryContextData.class);
    contextData.setEventType(RegistryEventType.paSendRTV2);
    String blIuv = "businessLogicIUV";
    String xmlRequest = "<xml>mockRequest</xml>";
    Object request = new Object();
    when(jaxbTransformService.marshalling(eq(request), any())).thenReturn(xmlRequest);

    // When
    Object actualResponse = registryLogger.execute(
      contextData, request,
      () -> Triple.of(null, blIuv, RegistryEventOutcome.OK),
      e -> null,
      () -> {
        // Simulate extra info retrieval
        return Map.of("extraInfoKey", "extraInfoValue");
      },
      null);

    // Then
    assertNull(actualResponse);

    verify(registryProducerService).notifyPagoPaEvent(
      eq(contextData),
      eq(RegistryEventSubType.REQ),
      eq(RegistryEventCategory.INTERFACCIA),
      eq(RegistryLogger.NODE_ID),
      eq(RegistryLogger.PU_ID),
      eq(RegistryEventOutcome.OK),
      argThat(o -> (o instanceof Map<?,?> m) &&
        m.containsKey("extraInfoKey") && "extraInfoValue".equals(m.get("extraInfoKey")) &&
        m.containsKey(RegistryLogger.XML_BODY_KEY) && xmlRequest.equals(m.get(RegistryLogger.XML_BODY_KEY)))
    );

    contextData.setIuv(blIuv);
    verify(registryProducerService).notifyPagoPaEvent(
      contextData,
      RegistryEventSubType.RESP,
      RegistryEventCategory.INTERFACCIA,
      RegistryLogger.PU_ID,
      RegistryLogger.NODE_ID,
      RegistryEventOutcome.OK,
      null
    );
  }

  @Test
  void testProduceReqRegistryEventWithExtraInfoSkipXmlBody() {
    // Given
    RegistryContextData contextData = podamFactory.manufacturePojo(RegistryContextData.class);
    contextData.setEventType(RegistryEventType.paSendRTV2);
    String blIuv = "businessLogicIUV";
    Object request = new Object();

    // When
    Object actualResponse = registryLogger.execute(
      contextData, request,
      () -> Triple.of(null, blIuv, RegistryEventOutcome.OK),
      e -> null,
      () -> {
        // Simulate extra info retrieval
        return Map.of("extraInfoKey", "extraInfoValue", RegistryLogger.SKIP_XML_BODY_KEY, true);
      }, null);

    // Then
    assertNull(actualResponse);

    verify(registryProducerService).notifyPagoPaEvent(
      eq(contextData),
      eq(RegistryEventSubType.REQ),
      eq(RegistryEventCategory.INTERFACCIA),
      eq(RegistryLogger.NODE_ID),
      eq(RegistryLogger.PU_ID),
      eq(RegistryEventOutcome.OK),
      argThat(o -> (o instanceof Map<?,?> m) &&
        m.containsKey("extraInfoKey") && "extraInfoValue".equals(m.get("extraInfoKey")) &&
        !m.containsKey(RegistryLogger.XML_BODY_KEY) )
    );

    contextData.setIuv(blIuv);
    verify(registryProducerService).notifyPagoPaEvent(
      contextData,
      RegistryEventSubType.RESP,
      RegistryEventCategory.INTERFACCIA,
      RegistryLogger.PU_ID,
      RegistryLogger.NODE_ID,
      RegistryEventOutcome.OK,
      null
    );
  }

  @Test
  void testProduceRespRegistryEvent() {
    // Given
    RegistryContextData contextData = podamFactory.manufacturePojo(RegistryContextData.class);
    contextData.setEventType(RegistryEventType.paSendRTV2);
    String blIuv = "businessLogicIUV";
    String xmlRequest = "<xml>mockRequest</xml>";
    String xmlResponse = "<xml>mockResponse</xml>";
    Object request = new Object();
    Object response = new Object();
    when(jaxbTransformService.marshalling(eq(request), any())).thenReturn(xmlRequest);
    when(jaxbTransformService.marshalling(eq(response), any())).thenReturn(xmlResponse);

    // When
    Object actualResponse = registryLogger.execute(
      contextData, request,
      () -> Triple.of(response, blIuv, RegistryEventOutcome.OK),
      e -> null);

    // Then
    assertEquals(response, actualResponse);

    verify(registryProducerService).notifyPagoPaEvent(
      eq(contextData),
      eq(RegistryEventSubType.REQ),
      eq(RegistryEventCategory.INTERFACCIA),
      eq(RegistryLogger.NODE_ID),
      eq(RegistryLogger.PU_ID),
      eq(RegistryEventOutcome.OK),
      any()
    );

    contextData.setIuv(blIuv);
    verify(registryProducerService).notifyPagoPaEvent(
      contextData,
      RegistryEventSubType.RESP,
      RegistryEventCategory.INTERFACCIA,
      RegistryLogger.PU_ID,
      RegistryLogger.NODE_ID,
      RegistryEventOutcome.OK,
      xmlResponse
    );
  }

  @Test
  void testProduceRespRegistryEventWithExtraInfo() {
    // Given
    RegistryContextData contextData = podamFactory.manufacturePojo(RegistryContextData.class);
    contextData.setEventType(RegistryEventType.paSendRTV2);
    String blIuv = "businessLogicIUV";
    String xmlRequest = "<xml>mockRequest</xml>";
    String xmlResponse = "<xml>mockResponse</xml>";
    Object request = new Object();
    Object response = new Object();
    when(jaxbTransformService.marshalling(eq(request), any())).thenReturn(xmlRequest);
    when(jaxbTransformService.marshalling(eq(response), any())).thenReturn(xmlResponse);

    // When
    Object actualResponse = registryLogger.execute(
      contextData, request,
      () -> Triple.of(response, blIuv, RegistryEventOutcome.OK),
      e -> null,
      null, r -> {
        // Simulate extra info retrieval
        return Map.of("extraInfoKey", "extraInfoValue");
      });

    // Then
    assertEquals(response, actualResponse);

    verify(registryProducerService).notifyPagoPaEvent(
      eq(contextData),
      eq(RegistryEventSubType.REQ),
      eq(RegistryEventCategory.INTERFACCIA),
      eq(RegistryLogger.NODE_ID),
      eq(RegistryLogger.PU_ID),
      eq(RegistryEventOutcome.OK),
      any()
    );

    contextData.setIuv(blIuv);
    verify(registryProducerService).notifyPagoPaEvent(
      eq(contextData),
      eq(RegistryEventSubType.RESP),
      eq(RegistryEventCategory.INTERFACCIA),
      eq(RegistryLogger.PU_ID),
      eq(RegistryLogger.NODE_ID),
      eq(RegistryEventOutcome.OK),
      argThat(o -> (o instanceof Map<?,?> m) &&
        m.containsKey("extraInfoKey") && "extraInfoValue".equals(m.get("extraInfoKey")) &&
        m.containsKey(RegistryLogger.XML_BODY_KEY) && xmlResponse.equals(m.get(RegistryLogger.XML_BODY_KEY)))
    );
  }

  @Test
  void testProduceRespRegistryEventWithExtraInfoSkipXmlBody() {
    // Given
    RegistryContextData contextData = podamFactory.manufacturePojo(RegistryContextData.class);
    contextData.setEventType(RegistryEventType.paSendRTV2);
    String blIuv = "businessLogicIUV";
    String xmlRequest = "<xml>mockRequest</xml>";
    Object request = new Object();
    Object response = new Object();
    when(jaxbTransformService.marshalling(eq(request), any())).thenReturn(xmlRequest);

    // When
    Object actualResponse = registryLogger.execute(
      contextData, request,
      () -> Triple.of(response, blIuv, RegistryEventOutcome.OK),
      e -> null,
      null, r -> {
        // Simulate extra info retrieval
        return Map.of("extraInfoKey", "extraInfoValue:"+r, RegistryLogger.SKIP_XML_BODY_KEY, true);
      });

    // Then
    assertEquals(response, actualResponse);

    verify(registryProducerService).notifyPagoPaEvent(
      eq(contextData),
      eq(RegistryEventSubType.REQ),
      eq(RegistryEventCategory.INTERFACCIA),
      eq(RegistryLogger.NODE_ID),
      eq(RegistryLogger.PU_ID),
      eq(RegistryEventOutcome.OK),
      any()
    );

    contextData.setIuv(blIuv);
    verify(registryProducerService).notifyPagoPaEvent(
      eq(contextData),
      eq(RegistryEventSubType.RESP),
      eq(RegistryEventCategory.INTERFACCIA),
      eq(RegistryLogger.PU_ID),
      eq(RegistryLogger.NODE_ID),
      eq(RegistryEventOutcome.OK),
      argThat(o -> (o instanceof Map<?,?> m) &&
        m.containsKey("extraInfoKey") && ("extraInfoValue:"+response).equals(m.get("extraInfoKey")) &&
        !m.containsKey(RegistryLogger.XML_BODY_KEY))
    );
  }

  @Test
  void testExecuteWithException() {
    // Given
    RegistryContextData contextData = podamFactory.manufacturePojo(RegistryContextData.class);
    contextData.setEventType(RegistryEventType.paSendRTV2);
    String xmlRequest = "<xml>mockRequest</xml>";
    Object request = new Object();
    Object fallbackResponse = new Object();
    String xmlFallbackResponse = "mockFallbackResponse";
    when(jaxbTransformService.marshalling(eq(request), any())).thenReturn(xmlRequest);
    when(jaxbTransformService.marshalling(eq(fallbackResponse), any())).thenReturn(xmlFallbackResponse);

    // When
    Object actualResponse = registryLogger.execute(
      contextData, request,
      () -> {
        throw new RuntimeException("Mock Exception");
      },
      e -> fallbackResponse);

    // Then
    assertEquals(fallbackResponse, actualResponse);

    verify(registryProducerService).notifyPagoPaEvent(
      contextData,
      RegistryEventSubType.REQ,
      RegistryEventCategory.INTERFACCIA,
      RegistryLogger.NODE_ID,
      RegistryLogger.PU_ID,
      RegistryEventOutcome.OK,
      xmlRequest);

    verify(registryProducerService).notifyPagoPaEvent(
      contextData,
      RegistryEventSubType.RESP,
      RegistryEventCategory.INTERFACCIA,
      RegistryLogger.PU_ID,
      RegistryLogger.NODE_ID,
      RegistryEventOutcome.KO,
      xmlFallbackResponse);
  }

  @Test
  void testNotifySilEventException() {
    // Given
    RegistryContextData contextData = podamFactory.manufacturePojo(RegistryContextData.class);
    contextData.setEventType(RegistryEventType.paSendRTV2);
    String blIuv = "businessLogicIUV";
    String xmlRequest = "<xml>mockRequest</xml>";
    Object request = new Object();
    Object response = new Object();
    when(jaxbTransformService.marshalling(eq(request), any())).thenReturn(xmlRequest);
    doThrow(new RuntimeException("simulated exception")).when(registryProducerService).notifyPagoPaEvent(
      any(), any(), any(), any(), any(), any(), any());

    // When
    Object actualResponse = registryLogger.execute(
      contextData,
      request,
      () -> Triple.of(response, blIuv, RegistryEventOutcome.OK),
      e -> null);

    // Then
    assertEquals(response, actualResponse);

    verify(registryProducerService).notifyPagoPaEvent(
      contextData,
      RegistryEventSubType.REQ,
      RegistryEventCategory.INTERFACCIA,
      RegistryLogger.NODE_ID,
      RegistryLogger.PU_ID,
      RegistryEventOutcome.OK,
      xmlRequest);

    contextData.setIuv(blIuv);
    verify(registryProducerService).notifyPagoPaEvent(
      eq(contextData),
      eq(RegistryEventSubType.RESP),
      eq(RegistryEventCategory.INTERFACCIA),
      eq(RegistryLogger.PU_ID),
      eq(RegistryLogger.NODE_ID),
      eq(RegistryEventOutcome.OK),
      any());
  }

  @Test
  void testProduceReqRegistryClientEvent() {
    // Given
    RegistryContextData contextData = podamFactory.manufacturePojo(RegistryContextData.class);
    contextData.setEventType(RegistryEventType.createPosition);
    String blIuv = "businessLogicIUV";
    String xmlRequest = "<xml>mockRequest</xml>";
    Object request = new Object();
    when(jaxbTransformService.marshalling(eq(request), any())).thenReturn(xmlRequest);

    // When
    Object actualResponse = registryLogger.execute(
      contextData, request,
      () -> Triple.of(null, blIuv, RegistryEventOutcome.OK),
      e -> null);

    // Then
    assertNull(actualResponse);

    verify(registryProducerService).notifyPagoPaEvent(
      contextData,
      RegistryEventSubType.REQ,
      RegistryEventCategory.INTERFACCIA,
      RegistryLogger.PU_ID,
      RegistryLogger.NODE_ID,
      RegistryEventOutcome.OK,
      xmlRequest);

    contextData.setIuv(blIuv);
    verify(registryProducerService).notifyPagoPaEvent(
      eq(contextData),
      eq(RegistryEventSubType.RESP),
      eq(RegistryEventCategory.INTERFACCIA),
      eq(RegistryLogger.NODE_ID),
      eq(RegistryLogger.PU_ID),
      eq(RegistryEventOutcome.OK),
      any());
  }
}
