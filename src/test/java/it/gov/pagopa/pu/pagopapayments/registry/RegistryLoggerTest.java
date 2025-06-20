package it.gov.pagopa.pu.pagopapayments.registry;

import it.gov.pagopa.pu.pagopapayments.enums.RegistryEventCategory;
import it.gov.pagopa.pu.pagopapayments.enums.RegistryEventOutcome;
import it.gov.pagopa.pu.pagopapayments.enums.RegistryEventSubType;
import it.gov.pagopa.pu.pagopapayments.enums.RegistryEventType;
import it.gov.pagopa.pu.pagopapayments.event.producer.RegistryProducerService;
import it.gov.pagopa.pu.pagopapayments.service.JAXBTransformService;
import it.gov.pagopa.pu.pagopapayments.util.Utilities;
import org.apache.commons.lang3.tuple.Triple;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegistryLoggerTest {

  @Mock
  private JAXBTransformService jaxbTransformService;
  @Mock
  private RegistryProducerService registryProducerService;
  @InjectMocks
  private RegistryLogger registryLogger;

  @Test
  void testProduceReqRegistryEvent() {
    // Given
    String orgFiscalCode = "mockFiscalCode";
    String idBrokerStation = "mockIdBrokerStation";
    String pspId = "mockPspId";
    String pspChannelId = "mockPspChannelId";
    String paymentMethod = "mockPaymentMethod";
    String ccp = "mockCCP";
    RegistryEventType eventType = RegistryEventType.paSendRTV2;
    String iuv = "mockIUV";
    String blIuv = "businessLogicIUV";
    String xmlRequest = "<xml>mockRequest</xml>";
    Object request = new Object();
    when(jaxbTransformService.marshalling(eq(request), any())).thenReturn(xmlRequest);

    // When
    Object actualResponse = registryLogger.execute(
      orgFiscalCode,
      idBrokerStation,
      pspId,
      pspChannelId,
      paymentMethod,
      ccp,
      eventType,
      iuv,
      request,
      () -> Triple.of(null, blIuv, RegistryEventOutcome.OK),
      e -> null
    );

    // Then
    assertNull(actualResponse);

    verify(registryProducerService).notifyPagoPaEvent(
      orgFiscalCode,
      idBrokerStation,
      pspId,
      pspChannelId,
      paymentMethod,
      ccp,
      eventType,
      RegistryEventSubType.REQ,
      RegistryEventCategory.INTERFACCIA,
      RegistryLogger.NODE_ID,
      RegistryLogger.PU_ID,
      iuv,
      Utilities.iuv2Nav(iuv),
      RegistryEventOutcome.OK,
      xmlRequest
    );
  }
}
