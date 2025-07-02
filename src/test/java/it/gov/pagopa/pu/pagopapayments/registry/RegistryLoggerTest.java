package it.gov.pagopa.pu.pagopapayments.registry;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.pagopa_api.pa.pafornode.PaSendRTV2Request;
import it.gov.pagopa.pagopa_api.pa.pafornode.PaSendRTV2Response;
import it.gov.pagopa.pu.pagopapayments.event.producer.RegistryProducerService;
import it.gov.pagopa.pu.pagopapayments.service.JAXBTransformService;
import it.gov.pagopa.pu.pagopapayments.util.TestUtils;
import it.gov.pagopa.pu.registries.dto.generated.RegistryEventCategory;
import it.gov.pagopa.pu.registries.dto.generated.RegistryEventSubType;
import it.gov.pagopa.pu.registries.dto.generated.RegistryOutcome;
import org.apache.commons.lang3.tuple.Triple;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import uk.co.jemos.podam.api.PodamFactory;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RegistryLoggerTest {

  @Mock
  private ObjectMapper objectMapperMock;
  @Mock
  private JAXBTransformService jaxbTransformServiceMock;
  @Mock
  private RegistryProducerService registryProducerServiceMock;

  private RegistryLogger registryLogger;

  private final PodamFactory podamFactory = TestUtils.getPodamFactory();

  @BeforeEach
  void init(){
    registryLogger = new RegistryLogger(objectMapperMock, jaxbTransformServiceMock, registryProducerServiceMock);
  }

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(
      objectMapperMock,
      jaxbTransformServiceMock,
      registryProducerServiceMock);
  }

  @Test
  void testProduceRegistryEvent_JSON() throws JsonProcessingException {
    // Given
    RegistryContextData contextData = podamFactory.manufacturePojo(RegistryContextData.class);
    contextData.setEventType(RegistryEventType.paSendRTV2);
    String blIuv = "businessLogicIUV";
    String requestPayload = "REQUEST_PAYLOAD";
    String responsePayload = "RESPONSE_PAYLOAD";
    Object request = new Object();
    Object response = new Object();

    when(objectMapperMock.writeValueAsString(same(request))).thenReturn(requestPayload);
    when(objectMapperMock.writeValueAsString(same(response))).thenReturn(responsePayload);

    // When
    Object actualResponse = registryLogger.execute(
      contextData, request,
      () -> Triple.of(response, blIuv, RegistryOutcome.OK),
      e -> null);

    // Then
    assertSame(response, actualResponse);

    verify(registryProducerServiceMock).notifyPagoPaEvent(
      eq(contextData),
      eq(RegistryEventSubType.REQ),
      eq(RegistryEventCategory.INTERFACCIA),
      eq(RegistryLogger.NODE_ID),
      eq(RegistryLogger.PU_ID),
      eq(RegistryOutcome.OK),
      any()
    );

    contextData.setIuv(blIuv);
    verify(registryProducerServiceMock).notifyPagoPaEvent(
      contextData,
      RegistryEventSubType.RESP,
      RegistryEventCategory.INTERFACCIA,
      RegistryLogger.PU_ID,
      RegistryLogger.NODE_ID,
      RegistryOutcome.OK,
      responsePayload
    );
  }

  @Test
  void testProduceRegistryEvent_XML() {
    // Given
    RegistryContextData contextData = podamFactory.manufacturePojo(RegistryContextData.class);
    contextData.setEventType(RegistryEventType.paSendRTV2);
    String blIuv = "businessLogicIUV";
    String xmlRequest = "<xml>mockRequest</xml>";
    String xmlResponse = "<xml>mockResponse</xml>";
    PaSendRTV2Request request = new PaSendRTV2Request();
    PaSendRTV2Response response = new PaSendRTV2Response();

    when(jaxbTransformServiceMock.marshalling(same(request), eq(PaSendRTV2Request.class))).thenReturn(xmlRequest);
    when(jaxbTransformServiceMock.marshalling(same(response), eq(PaSendRTV2Response.class))).thenReturn(xmlResponse);

    // When
    Object actualResponse = registryLogger.execute(
      contextData, request,
      () -> Triple.of(response, blIuv, RegistryOutcome.OK),
      e -> null);

    // Then
    assertSame(response, actualResponse);

    verify(registryProducerServiceMock).notifyPagoPaEvent(
      eq(contextData),
      eq(RegistryEventSubType.REQ),
      eq(RegistryEventCategory.INTERFACCIA),
      eq(RegistryLogger.NODE_ID),
      eq(RegistryLogger.PU_ID),
      eq(RegistryOutcome.OK),
      any()
    );

    contextData.setIuv(blIuv);
    verify(registryProducerServiceMock).notifyPagoPaEvent(
      contextData,
      RegistryEventSubType.RESP,
      RegistryEventCategory.INTERFACCIA,
      RegistryLogger.PU_ID,
      RegistryLogger.NODE_ID,
      RegistryOutcome.OK,
      xmlResponse
    );
  }

  @Test
  void testProduceRegistryEvent_NoResponsePayload() throws JsonProcessingException {
    // Given
    RegistryContextData contextData = podamFactory.manufacturePojo(RegistryContextData.class);
    contextData.setEventType(RegistryEventType.paSendRTV2);
    String blIuv = "businessLogicIUV";
    String payload = "PAYLOAD";
    Object request = new Object();

    when(objectMapperMock.writeValueAsString(same(request)))
      .thenReturn(payload);

    // When
    Object actualResponse = registryLogger.execute(
      contextData, request,
      () -> Triple.of(null, blIuv, RegistryOutcome.OK),
      e -> null
    );

    // Then
    assertNull(actualResponse);

    verify(registryProducerServiceMock).notifyPagoPaEvent(
      contextData,
      RegistryEventSubType.REQ,
      RegistryEventCategory.INTERFACCIA,
      RegistryLogger.NODE_ID,
      RegistryLogger.PU_ID,
      RegistryOutcome.OK,
      payload
    );

    contextData.setIuv(blIuv);
    verify(registryProducerServiceMock).notifyPagoPaEvent(
      contextData,
      RegistryEventSubType.RESP,
      RegistryEventCategory.INTERFACCIA,
      RegistryLogger.PU_ID,
      RegistryLogger.NODE_ID,
      RegistryOutcome.OK,
      null
    );
  }

  @Test
  void testProduceRegistryEventWithExtraInfo() throws JsonProcessingException {
    // Given
    RegistryContextData contextData = podamFactory.manufacturePojo(RegistryContextData.class);
    contextData.setEventType(RegistryEventType.paSendRTV2);
    String blIuv = "businessLogicIUV";
    String requestPayload = "REQUEST_PAYLOAD";
    String responsePayload = "RESPONSE_PAYLOAD";
    Object request = new Object();
    Object response = new Object();

    when(objectMapperMock.writeValueAsString(same(request))).thenReturn(requestPayload);
    when(objectMapperMock.writeValueAsString(same(response))).thenReturn(responsePayload);

    // When
    Object actualResponse = registryLogger.execute(
      contextData, request,
      () -> Triple.of(response, blIuv, RegistryOutcome.OK),
      e -> null,
      () -> {
        // Simulate extra info retrieval
        return Map.of("extraInfoKey", "extraInfoValue");
      },
      r -> {
        // Simulate extra info retrieval
        return Map.of("extraInfoKey", "extraInfoValue");
      });

    // Then
    assertSame(response, actualResponse);

    verify(registryProducerServiceMock).notifyPagoPaEvent(
      eq(contextData),
      eq(RegistryEventSubType.REQ),
      eq(RegistryEventCategory.INTERFACCIA),
      eq(RegistryLogger.NODE_ID),
      eq(RegistryLogger.PU_ID),
      eq(RegistryOutcome.OK),
      argThat(o -> (o instanceof Map<?, ?> m) &&
        m.containsKey("extraInfoKey") && "extraInfoValue".equals(m.get("extraInfoKey")) &&
        m.containsKey(RegistryLogger.PAYLOAD_KEY) && requestPayload.equals(m.get(RegistryLogger.PAYLOAD_KEY))));

    contextData.setIuv(blIuv);
    verify(registryProducerServiceMock).notifyPagoPaEvent(
      eq(contextData),
      eq(RegistryEventSubType.RESP),
      eq(RegistryEventCategory.INTERFACCIA),
      eq(RegistryLogger.PU_ID),
      eq(RegistryLogger.NODE_ID),
      eq(RegistryOutcome.OK),
      argThat(o -> (o instanceof Map<?, ?> m) &&
        m.containsKey("extraInfoKey") && "extraInfoValue".equals(m.get("extraInfoKey")) &&
        m.containsKey(RegistryLogger.PAYLOAD_KEY) && responsePayload.equals(m.get(RegistryLogger.PAYLOAD_KEY)))
    );
  }

  @Test
  void testProduceRegistryEvent_withEmptyExtraInfo() throws JsonProcessingException {
    // Given
    RegistryContextData contextData = podamFactory.manufacturePojo(RegistryContextData.class);
    contextData.setEventType(RegistryEventType.paSendRTV2);
    String blIuv = "businessLogicIUV";
    String requestPayload = "REQUEST_PAYLOAD";
    String responsePayload = "RESPONSE_PAYLOAD";
    Object request = new Object();
    Object response = new Object();

    when(objectMapperMock.writeValueAsString(same(request))).thenReturn(requestPayload);
    when(objectMapperMock.writeValueAsString(same(response))).thenReturn(responsePayload);

    // When
    Object actualResponse = registryLogger.execute(
      contextData, request,
      () -> Triple.of(response, blIuv, RegistryOutcome.OK),
      e -> null,
      Map::of,
      r -> {
        // Simulate extra info retrieval
        return Map.of();
      });

    // Then
    assertSame(response, actualResponse);

    verify(registryProducerServiceMock).notifyPagoPaEvent(
      contextData,
      RegistryEventSubType.REQ,
      RegistryEventCategory.INTERFACCIA,
      RegistryLogger.NODE_ID,
      RegistryLogger.PU_ID,
      RegistryOutcome.OK,
      requestPayload
    );

    contextData.setIuv(blIuv);
    verify(registryProducerServiceMock).notifyPagoPaEvent(
      contextData,
      RegistryEventSubType.RESP,
      RegistryEventCategory.INTERFACCIA,
      RegistryLogger.PU_ID,
      RegistryLogger.NODE_ID,
      RegistryOutcome.OK,
      responsePayload);
  }

  @Test
  void testProduceRegistryEvent_withExtraInfo_skipXmlBody() {
    // Given
    RegistryContextData contextData = podamFactory.manufacturePojo(RegistryContextData.class);
    contextData.setEventType(RegistryEventType.paSendRTV2);
    String blIuv = "businessLogicIUV";
    Object request = new Object();
    Object response = new Object();

    // When
    Object actualResponse = registryLogger.execute(
      contextData, request,
      () -> Triple.of(response, blIuv, RegistryOutcome.OK),
      e -> null,
      () -> {
        // Simulate extra info retrieval
        return Map.of("extraInfoKey", "extraInfoValue", RegistryLogger.SKIP_PAYLOAD_KEY, true);
      },
      r -> {
        // Simulate extra info retrieval
        return Map.of("extraInfoKey", "extraInfoValue:" + r, RegistryLogger.SKIP_PAYLOAD_KEY, true);
      });

    // Then
    assertSame(response, actualResponse);

    verify(registryProducerServiceMock).notifyPagoPaEvent(
      eq(contextData),
      eq(RegistryEventSubType.REQ),
      eq(RegistryEventCategory.INTERFACCIA),
      eq(RegistryLogger.NODE_ID),
      eq(RegistryLogger.PU_ID),
      eq(RegistryOutcome.OK),
      argThat(o -> (o instanceof Map<?, ?> m) &&
        m.containsKey("extraInfoKey") && "extraInfoValue".equals(m.get("extraInfoKey")) &&
        !m.containsKey(RegistryLogger.PAYLOAD_KEY))
    );

    contextData.setIuv(blIuv);
    verify(registryProducerServiceMock).notifyPagoPaEvent(
      eq(contextData),
      eq(RegistryEventSubType.RESP),
      eq(RegistryEventCategory.INTERFACCIA),
      eq(RegistryLogger.PU_ID),
      eq(RegistryLogger.NODE_ID),
      eq(RegistryOutcome.OK),
      argThat(o -> (o instanceof Map<?, ?> m) &&
        m.containsKey("extraInfoKey") && ("extraInfoValue:" + response).equals(m.get("extraInfoKey")) &&
        !m.containsKey(RegistryLogger.PAYLOAD_KEY))
    );
  }

  @Test
  void testProduceRegistryEvent_withExtraInfo_noResponsePayload() throws JsonProcessingException {
    // Given
    RegistryContextData contextData = podamFactory.manufacturePojo(RegistryContextData.class);
    contextData.setEventType(RegistryEventType.paSendRTV2);
    String blIuv = "businessLogicIUV";
    String requestPayload = "REQUEST_PAYLOAD";
    Object request = new Object();

    when(objectMapperMock.writeValueAsString(same(request))).thenReturn(requestPayload);

    // When
    Object actualResponse = registryLogger.execute(
      contextData, request,
      () -> Triple.of(null, blIuv, RegistryOutcome.OK),
      e -> null,
      () -> {
        // Simulate extra info retrieval
        return Map.of("extraInfoKey", "extraInfoValue");
      },
      null);

    // Then
    assertNull(actualResponse);

    verify(registryProducerServiceMock).notifyPagoPaEvent(
      eq(contextData),
      eq(RegistryEventSubType.REQ),
      eq(RegistryEventCategory.INTERFACCIA),
      eq(RegistryLogger.NODE_ID),
      eq(RegistryLogger.PU_ID),
      eq(RegistryOutcome.OK),
      argThat(o -> (o instanceof Map<?, ?> m) &&
        m.containsKey("extraInfoKey") && "extraInfoValue".equals(m.get("extraInfoKey")) &&
        m.containsKey(RegistryLogger.PAYLOAD_KEY) && requestPayload.equals(m.get(RegistryLogger.PAYLOAD_KEY))));

    contextData.setIuv(blIuv);
    verify(registryProducerServiceMock).notifyPagoPaEvent(
      contextData,
      RegistryEventSubType.RESP,
      RegistryEventCategory.INTERFACCIA,
      RegistryLogger.PU_ID,
      RegistryLogger.NODE_ID,
      RegistryOutcome.OK,
      null
    );
  }

  @Test
  void testProduceRegistryEvent_withJustRequestExtraInfo() throws JsonProcessingException {
    // Given
    RegistryContextData contextData = podamFactory.manufacturePojo(RegistryContextData.class);
    contextData.setEventType(RegistryEventType.paSendRTV2);
    String blIuv = "businessLogicIUV";
    String requestPayload = "REQUEST_PAYLOAD";
    String responsePayload = "RESPONSE_PAYLOAD";
    Object request = new Object();
    Object response = new Object();

    when(objectMapperMock.writeValueAsString(same(request))).thenReturn(requestPayload);
    when(objectMapperMock.writeValueAsString(same(response))).thenReturn(responsePayload);

    // When
    Object actualResponse = registryLogger.execute(
      contextData, request,
      () -> Triple.of(response, blIuv, RegistryOutcome.OK),
      e -> null,
      () -> {
        // Simulate extra info retrieval
        return Map.of("extraInfoKey", "extraInfoValue");
      },
      null);

    // Then
    assertSame(response, actualResponse);

    verify(registryProducerServiceMock).notifyPagoPaEvent(
      eq(contextData),
      eq(RegistryEventSubType.REQ),
      eq(RegistryEventCategory.INTERFACCIA),
      eq(RegistryLogger.NODE_ID),
      eq(RegistryLogger.PU_ID),
      eq(RegistryOutcome.OK),
      argThat(o -> (o instanceof Map<?, ?> m) &&
        m.containsKey("extraInfoKey") && "extraInfoValue".equals(m.get("extraInfoKey")) &&
        m.containsKey(RegistryLogger.PAYLOAD_KEY) && requestPayload.equals(m.get(RegistryLogger.PAYLOAD_KEY))));

    contextData.setIuv(blIuv);
    verify(registryProducerServiceMock).notifyPagoPaEvent(
      contextData,
      RegistryEventSubType.RESP,
      RegistryEventCategory.INTERFACCIA,
      RegistryLogger.PU_ID,
      RegistryLogger.NODE_ID,
      RegistryOutcome.OK,
      responsePayload
    );
  }

  @Test
  void testExecuteWithException() throws JsonProcessingException {
    // Given
    RegistryContextData contextData = podamFactory.manufacturePojo(RegistryContextData.class);
    contextData.setEventType(RegistryEventType.paSendRTV2);
    String requestPayload = "REQUESTPAYLOAD";
    Object request = new Object();
    Object fallbackResponse = new Object();
    String fallbackPayload = "FALLBACKPAYLOAD";

    when(objectMapperMock.writeValueAsString(same(request))).thenReturn(requestPayload);
    when(objectMapperMock.writeValueAsString(same(fallbackResponse))).thenReturn(fallbackPayload);

    // When
    Object actualResponse = registryLogger.execute(
      contextData, request,
      () -> {
        throw new RuntimeException("Mock Exception");
      },
      e -> fallbackResponse);

    // Then
    assertSame(fallbackResponse, actualResponse);

    verify(registryProducerServiceMock).notifyPagoPaEvent(
      contextData,
      RegistryEventSubType.REQ,
      RegistryEventCategory.INTERFACCIA,
      RegistryLogger.NODE_ID,
      RegistryLogger.PU_ID,
      RegistryOutcome.OK,
      requestPayload);

    verify(registryProducerServiceMock).notifyPagoPaEvent(
      contextData,
      RegistryEventSubType.RESP,
      RegistryEventCategory.INTERFACCIA,
      RegistryLogger.PU_ID,
      RegistryLogger.NODE_ID,
      RegistryOutcome.KO,
      fallbackPayload);
  }

  @Test
  void testExecuteWithException_noExceptionHandler() throws JsonProcessingException {
    // Given
    RegistryContextData contextData = podamFactory.manufacturePojo(RegistryContextData.class);
    contextData.setEventType(RegistryEventType.paSendRTV2);
    String requestPayload = "REQUESTPAYLOAD";
    Object request = new Object();

    when(objectMapperMock.writeValueAsString(same(request))).thenReturn(requestPayload);

    RuntimeException expectedException = new RuntimeException("Mock Exception");
    // When
    RuntimeException exception = Assertions.assertThrows(RuntimeException.class, () -> registryLogger.execute(
      contextData, request,
      () -> {
        throw expectedException;
      },
      null));

    // Then
    assertSame(expectedException, exception);

    verify(registryProducerServiceMock).notifyPagoPaEvent(
      contextData,
      RegistryEventSubType.REQ,
      RegistryEventCategory.INTERFACCIA,
      RegistryLogger.NODE_ID,
      RegistryLogger.PU_ID,
      RegistryOutcome.OK,
      requestPayload);

    verify(registryProducerServiceMock).notifyPagoPaEvent(
      contextData,
      RegistryEventSubType.RESP,
      RegistryEventCategory.INTERFACCIA,
      RegistryLogger.PU_ID,
      RegistryLogger.NODE_ID,
      RegistryOutcome.KO,
      null);
  }

  @Test
  void testExceptionDuringEventProducer() throws JsonProcessingException {
    // Given
    RegistryContextData contextData = podamFactory.manufacturePojo(RegistryContextData.class);
    contextData.setEventType(RegistryEventType.paSendRTV2);
    String blIuv = "businessLogicIUV";
    String requestPayload = "REQUEST_PAYLOAD";
    String responsePayload = "RESPONSE_PAYLOAD";
    Object request = new Object();
    Object response = new Object();

    when(objectMapperMock.writeValueAsString(same(request))).thenReturn(requestPayload);
    when(objectMapperMock.writeValueAsString(same(response))).thenReturn(responsePayload);

    doThrow(new RuntimeException("simulated exception")).when(registryProducerServiceMock).notifyPagoPaEvent(
      any(), any(), any(), any(), any(), any(), any());

    // When
    Object actualResponse = registryLogger.execute(
      contextData,
      request,
      () -> Triple.of(response, blIuv, RegistryOutcome.OK),
      e -> null);

    // Then
    assertEquals(response, actualResponse);

    verify(registryProducerServiceMock).notifyPagoPaEvent(
      contextData,
      RegistryEventSubType.REQ,
      RegistryEventCategory.INTERFACCIA,
      RegistryLogger.NODE_ID,
      RegistryLogger.PU_ID,
      RegistryOutcome.OK,
      requestPayload);

    contextData.setIuv(blIuv);
    verify(registryProducerServiceMock).notifyPagoPaEvent(
      contextData,
      RegistryEventSubType.RESP,
      RegistryEventCategory.INTERFACCIA,
      RegistryLogger.PU_ID,
      RegistryLogger.NODE_ID,
      RegistryOutcome.OK,
      responsePayload);
  }

  public static void configureRegistryLoggerMock(RegistryLogger registryLoggerMock, RegistryContextData contextData, Object request, boolean withExtraInfoReq, boolean withExtraInfoResp) {
    Object[] result = new Object[1];
    Exception[] exception = new Exception[1];
    ArgumentMatcher<Supplier<Triple<Object, String, RegistryOutcome>>> requestHandler = i -> {
      try {
        result[0] = i.get().getLeft();
      } catch (Exception e) {
        exception[0] = e;
      }
      return true;
    };
    ArgumentMatcher<Function<Exception, Object>> exceptionHandler = i -> {
      if (exception[0] != null && i != null) {
        result[0] = i.apply(exception[0]);
        exception[0] = null;
      }
      return true;
    };

    Answer<Object> answer = i -> {
      if(exception[0] != null) {
        throw exception[0];
      } else {
        return result[0];
      }
    };

    if (withExtraInfoReq || withExtraInfoResp) {
      when(registryLoggerMock.execute(
        eq(contextData),
        same(request),
        argThat(requestHandler),
        argThat(exceptionHandler),
        withExtraInfoReq
          ? argThat(requestExtraInfoRetriever -> {
          requestExtraInfoRetriever.get();
          return true;
        })
          : isNull(),
        withExtraInfoResp
          ? argThat(responseExtraInfoExtractor -> {
          if (result[0] != null) {
            responseExtraInfoExtractor.apply(result[0]);
          }
          return true;
        })
          : isNull()
      )).thenAnswer(answer);
    } else {
      Mockito.when(registryLoggerMock.execute(
        Mockito.eq(contextData),
        Mockito.same(request),
        Mockito.argThat(requestHandler),
        Mockito.argThat(exceptionHandler)
      )).thenAnswer(answer);
    }
  }
}
