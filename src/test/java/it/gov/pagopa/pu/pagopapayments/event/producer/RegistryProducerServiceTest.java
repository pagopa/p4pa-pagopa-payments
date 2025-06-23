package it.gov.pagopa.pu.pagopapayments.event.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import it.gov.pagopa.pu.pagopapayments.dto.PaSendRtDTO;
import it.gov.pagopa.pu.pagopapayments.registry.RegistryEventType;
import it.gov.pagopa.pu.pagopapayments.event.producer.dto.RegistryEventDTO;
import it.gov.pagopa.pu.pagopapayments.registry.RegistryContextData;
import it.gov.pagopa.pu.pagopapayments.util.TestUtils;
import it.gov.pagopa.pu.pagopapayments.util.Utilities;
import it.gov.pagopa.pu.registries.dto.generated.RegistryEventCategory;
import it.gov.pagopa.pu.registries.dto.generated.RegistryEventSubType;
import it.gov.pagopa.pu.registries.dto.generated.RegistryOutcome;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.Message;

import java.time.OffsetDateTime;

@ExtendWith(MockitoExtension.class)
class RegistryProducerServiceTest {

  @Mock
  private StreamBridge streamBridge;

  private RegistryProducerService registryProducerService;
  private final ObjectMapper objectMapper;

  public RegistryProducerServiceTest() {
    this.objectMapper = new ObjectMapper();
    this.objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
  }

  @BeforeEach
  void setUp() {
    registryProducerService = new RegistryProducerService(streamBridge, new ObjectMapper());
  }

  @AfterEach
  void clear(){
    MDC.clear();
  }

  @ParameterizedTest
  @ValueSource(strings = {"null", "string", "object"})
  void whenNotifyPagoPaEventThenSendMessage(String bodyType) throws JsonProcessingException {
    // Given
    String orgFiscalCode = "68216521868";
    var eventType = RegistryEventType.paSendRTV2;
    var subType = RegistryEventSubType.REQ;
    String requestorId = "9cbb04c1-627b-4063-a09b-ee8f718bb9bd";
    String grantorId = "2add6c22-75bb-4fad-b186-56620a362def";
    String iuv = "31234567890123456789012345678901234567890";
    String nav = Utilities.iuv2Nav(iuv);
    String brokerStationId = "45428ef5-53ab-4690-a565-d13b0af64d5f";
    String pspId = "23e19e1b-fcb8-43c0-b643-9396394f10ca";
    String pspChannelId = "channel-12345";
    String paymentMethod = "creditCard";
    String ccp = "ccp";
    RegistryOutcome outcome;
    Object body;

    String serializedBody = switch (bodyType) {
      case "null" -> {
        body = null;
        outcome = RegistryOutcome.OK;
        yield null;
      }
      case "string" -> {
        body = "string body";
        outcome = RegistryOutcome.KO;
        yield (String) body;
      }
      case "object" -> {
        body = PaSendRtDTO.builder().build();
        outcome = RegistryOutcome.OK;
        yield objectMapper.writeValueAsString(body);
      }
      default ->
        throw new IllegalArgumentException("Invalid body type: " + bodyType);
    };
    String traceId = "de59ed53-cfdb-450f-acd7-f1054a53b8b0";
    MDC.put("traceId", traceId);

    RegistryContextData contextData = RegistryContextData.builder()
      .orgFiscalCode(orgFiscalCode)
      .brokerStationId(brokerStationId)
      .pspId(pspId)
      .pspChannelId(pspChannelId)
      .paymentMethod(paymentMethod)
      .ccp(ccp)
      .eventType(eventType)
      .iuv(iuv)
      .build();

    // When
    registryProducerService.notifyPagoPaEvent(
      contextData,
      subType,
      RegistryEventCategory.INTERNO,
      requestorId,
      grantorId,
      outcome,
      body
    );

    // Then
    Mockito.verify(streamBridge, Mockito.times(1)).send(
      Mockito.eq("registryProducer-out-0"),
      Mockito.any(),
      Mockito.<Message<?>>argThat(m -> {
        RegistryEventDTO payload = (RegistryEventDTO) m.getPayload();
        String eventIdPrefix = eventType.name();
        Assertions.assertEquals(eventIdPrefix, payload.getRegistryId().substring(0, eventIdPrefix.length()));
        Assertions.assertEquals(traceId, payload.getTraceId());
        Assertions.assertEquals("pagopa-payments", payload.getRegistryOrigin());
        Assertions.assertEquals("REGISTRY_PAGOPA", payload.getRegistryType());

        Assertions.assertEquals(eventType.name(), payload.getEventType().name());
        Assertions.assertEquals(subType.name(), payload.getEventSubType().name());
        Assertions.assertEquals(orgFiscalCode, payload.getOrgFiscalCode());
        Assertions.assertEquals(iuv, payload.getIuv());
        Assertions.assertEquals(nav, payload.getNav());
        Assertions.assertEquals(requestorId, payload.getRequestorId());
        Assertions.assertEquals(grantorId, payload.getGrantorId());
        Assertions.assertEquals(outcome.name(), payload.getOutcome().name());
        Assertions.assertEquals(ccp, payload.getCcp());
        Assertions.assertEquals(serializedBody, payload.getBody());
        Assertions.assertTrue(OffsetDateTime.now().toEpochSecond() - payload.getDateTime().toEpochSecond() < 5);

        String[] ignoredFields = {};
        if(body == null) {
          ignoredFields = new String[]{"body"};
        }
        TestUtils.checkNotNullFields(payload, ignoredFields);

        return true;
      }));
  }

}
