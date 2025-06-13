package it.gov.pagopa.pu.pagopapayments.event.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.pu.pagopapayments.enums.RegistryEventCategory;
import it.gov.pagopa.pu.pagopapayments.enums.RegistryEventOutcome;
import it.gov.pagopa.pu.pagopapayments.enums.RegistryEventSubType;
import it.gov.pagopa.pu.pagopapayments.enums.RegistryEventType;
import it.gov.pagopa.pu.pagopapayments.event.producer.dto.RegistryEventDTO;
import it.gov.pagopa.pu.pagopapayments.exception.ApplicationException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.function.Supplier;

@Component
@Slf4j
public class RegistryProducerService {
  public static final String PU_ID = "piattaformaunitaria";
  public static final String NODE_ID = "NodoDeiPagamentiSPC";
  public static final String REGISTRY_ORIGIN = "pagopa-payments";
  public static final String REGISTRY_TYPE = "REGISTRY_PAGOPA";

  @Value("${spring.cloud.stream.bindings.registryProducer-out-0.binder}")
  private String binder;

  private final StreamBridge streamBridge;

  private final ObjectMapper objectMapper;

  public RegistryProducerService(StreamBridge streamBridge, ObjectMapper objectMapper) {
    this.streamBridge = streamBridge;
    this.objectMapper = objectMapper;
  }

  @Configuration
  static class RegistryProducerConfig {
    @Bean
    public Supplier<Message<RegistryEventDTO>> registryProducer() {
      return () -> null;
    }
  }

  public void notifySilEvent(
    String orgFiscalCode,
    String brokerStationId,
    String pspId,
    String pspChannelId,
    String paymentMethod,
    String ccp,
    RegistryEventType eventType,
    RegistryEventSubType subType,
    RegistryEventCategory category,
    String requestorId,
    String grantorId,
    String iuv,
    String nav,
    RegistryEventOutcome outcome,
    Object body
  ) {
    String registryId = String.join("-", eventType.name(), String.valueOf(System.currentTimeMillis()), UUID.randomUUID().toString());
    String traceId = MDC.get("traceId");

    String bodyString = null;
    if (body instanceof String bodyAsString) {
      bodyString = bodyAsString;
    } else if (body != null) {
      bodyString = serializeObjectToJson(body);
    }

    streamBridge.send("registryProducer-out-0", binder,
      MessageBuilder.withPayload(RegistryEventDTO.builder()
          .registryId(registryId)
          .traceId(traceId)
          .registryOrigin(REGISTRY_ORIGIN)
          .registryType(REGISTRY_TYPE)
          .dateTime(OffsetDateTime.now())
          .brokerStationId(brokerStationId)
          .orgFiscalCode(orgFiscalCode)
          .pspId(pspId)
          .pspChannelId(pspChannelId)
          .paymentMethod(paymentMethod)
          .ccp(ccp)
          .eventType(eventType)
          .eventSubType(subType)
          .eventCategory(category)
          .iuv(iuv)
          .nav(nav)
          .requestorId(requestorId)
          .grantorId(grantorId)
          .outcome(outcome)
          .body(bodyString)
          .build()
        )
        .setHeader(KafkaHeaders.KEY, registryId)
        .build()
    );
  }

  private String serializeObjectToJson(Object object) {
    try {
      return objectMapper.writeValueAsString(object);
    } catch (Exception e) {
      log.error("Error serializing object to JSON", e);
      throw new ApplicationException("Error serializing object to JSON", e);
    }
  }
}
