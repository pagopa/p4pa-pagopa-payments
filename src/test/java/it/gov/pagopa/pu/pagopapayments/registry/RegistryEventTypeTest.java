package it.gov.pagopa.pu.pagopapayments.registry;

import it.gov.pagopa.pu.registries.dto.generated.RegistryPagoPaEventType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

class RegistryEventTypeTest {

  @Test
  void testAlignmentWithRegistries(){
    Assertions.assertEquals(
      RegistryEventType.values().length,
      RegistryPagoPaEventType.values().length);

    Assertions.assertEquals(
      Arrays.stream(RegistryEventType.values()).map(RegistryEventType::name).sorted().toList(),
      Arrays.stream(RegistryPagoPaEventType.values()).map(RegistryPagoPaEventType::getValue).sorted().toList()
    );
  }
}
