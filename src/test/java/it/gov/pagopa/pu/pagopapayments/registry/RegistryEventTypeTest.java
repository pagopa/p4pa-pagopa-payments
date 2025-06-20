package it.gov.pagopa.pu.pagopapayments.registry;

import it.gov.pagopa.pu.registries.dto.generated.RegistryPagopaEventType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

class RegistryEventTypeTest {

  @Test
  void testAlignmentWithRegistries(){
    Assertions.assertEquals(
      RegistryEventType.values().length,
      RegistryPagopaEventType.values().length);

    Assertions.assertEquals(
      Arrays.stream(RegistryEventType.values()).map(RegistryEventType::name).sorted().toList(),
      Arrays.stream(RegistryPagopaEventType.values()).map(RegistryPagopaEventType::getValue).sorted().toList()
    );
  }
}
