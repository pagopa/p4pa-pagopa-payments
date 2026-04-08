package it.gov.pagopa.pu.pagopapayments.event.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.pu.pagopapayments.event.producer.dto.RegistryEventDTO;
import it.gov.pagopa.pu.pagopapayments.exception.ApplicationException;
import it.gov.pagopa.pu.pagopapayments.registry.RegistryContextData;
import it.gov.pagopa.pu.pagopapayments.util.Utilities;
import it.gov.pagopa.pu.registries.dto.generated.RegistryEventCategory;
import it.gov.pagopa.pu.registries.dto.generated.RegistryEventSubType;
import it.gov.pagopa.pu.registries.dto.generated.RegistryOutcome;
import lombok.extern.slf4j.Slf4j;
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
  public static final String REGISTRY_ORIGIN = "pagopa-payments";
  public static final String REGISTRY_TYPE = "REGISTRY_PAGOPA";

  @Value("${spring.cloud.stream.bindings.registryProducer-out-0.binder}")
  private String binder;

  private final StreamBridge streamBridge;

  private final ObjectMapper objectMapper;

  private final String auxDigit;

  public RegistryProducerService(StreamBridge streamBridge, ObjectMapper objectMapper,
                                 @Value("${nav.aux-digit}") String auxDigit) {
    this.streamBridge = streamBridge;
    this.objectMapper = objectMapper;
    this.auxDigit = auxDigit;
  }

  @Configuration
  static class RegistryProducerConfig {
    @Bean
    public Supplier<Message<RegistryEventDTO>> registryProducer() {
      return () -> null;
    }
  }

  public void notifyPagoPaEvent(
    RegistryContextData contextData,
    RegistryEventSubType subType,
    RegistryEventCategory category,
    String requestorId,
    String grantorId,
    RegistryOutcome outcome,
    Object body
  ) {
    String registryId = String.join("-", contextData.getEventType().name(), String.valueOf(System.currentTimeMillis()), UUID.randomUUID().toString());
    String traceId = Utilities.getTraceId();

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
          .brokerStationId(contextData.getBrokerStationId())
          .orgFiscalCode(contextData.getOrgFiscalCode())
          .pspId(contextData.getPspId())
          .pspChannelId(contextData.getPspChannelId())
          .paymentMethod(contextData.getPaymentMethod())
          .ccp(contextData.getCcp())
          .eventType(contextData.getEventType())
          .eventSubType(subType)
          .eventCategory(category)
          .iuv(contextData.getIuv())
          .nav(Utilities.iuv2Nav(contextData.getIuv(), auxDigit))
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
