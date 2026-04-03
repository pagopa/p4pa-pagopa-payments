package it.gov.pagopa.pu.pagopapayments.endpoint;

import it.gov.pagopa.pagopa_api.pa.pafornode.*;
import it.gov.pagopa.pagopa_api.xsd.common_types.v1_0.StOutcome;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.organization.dto.generated.Broker;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.pagopapayments.dto.PaSendRtDTO;
import it.gov.pagopa.pu.pagopapayments.dto.RetrievePaymentDTO;
import it.gov.pagopa.pu.pagopapayments.enums.PagoPaNodeFaults;
import it.gov.pagopa.pu.pagopapayments.exception.PagoPaNodeFaultException;
import it.gov.pagopa.pu.pagopapayments.mapper.PaDemandPaymentNoticeMapper;
import it.gov.pagopa.pu.pagopapayments.mapper.PaGetPaymentMapper;
import it.gov.pagopa.pu.pagopapayments.mapper.PaSendRTMapper;
import it.gov.pagopa.pu.pagopapayments.mapper.PaVerifyPaymentNoticeMapper;
import it.gov.pagopa.pu.pagopapayments.registry.RegistryContextData;
import it.gov.pagopa.pu.pagopapayments.registry.RegistryEventType;
import it.gov.pagopa.pu.pagopapayments.registry.RegistryLogger;
import it.gov.pagopa.pu.pagopapayments.registry.RegistryLoggerTest;
import it.gov.pagopa.pu.pagopapayments.service.demandpaymentnotice.DemandPaymentNoticeService;
import it.gov.pagopa.pu.pagopapayments.service.receipt.ReceiptService;
import it.gov.pagopa.pu.pagopapayments.service.synchronouspayments.SynchronousPaymentService;
import it.gov.pagopa.pu.pagopapayments.util.TestUtils;
import it.gov.pagopa.pu.pagopapayments.util.Utilities;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.jemos.podam.api.PodamFactory;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaForNodeEndpointTest {

  @Mock
  private SynchronousPaymentService synchronousPaymentServiceMock;
  @Mock
  private ReceiptService receiptServiceMock;
  @Mock
  private PaSendRTMapper paSendRTMapperMock;
  @Mock
  private RegistryLogger registryLoggerMock;
  @Mock
  private DemandPaymentNoticeService demandPaymentNoticeServiceMock;

  @InjectMocks
  private PaForNodeEndpoint paForNodeEndpoint;

  private final PodamFactory podamFactory = TestUtils.getPodamFactory();

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      synchronousPaymentServiceMock,
      receiptServiceMock,
      paSendRTMapperMock,
      registryLoggerMock,
      demandPaymentNoticeServiceMock);
  }

  private void configureRegistryLoggerMock(RegistryContextData contextData, Object request) {
    RegistryLoggerTest.configureRegistryLoggerMock(registryLoggerMock, contextData, request, false, false);
  }

  //region paDemandPaymentNotice
  @Test
  void givenValidRequestWhenPaDemandPaymentNoticeThenSuccess() {
    try (MockedStatic<PaDemandPaymentNoticeMapper> mapperMock = Mockito.mockStatic(PaDemandPaymentNoticeMapper.class)) {
      // given
      PaDemandPaymentNoticeRequest request = podamFactory.manufacturePojo(PaDemandPaymentNoticeRequest.class);
      DebtPositionDTO debtPositionDTO = podamFactory.manufacturePojo(DebtPositionDTO.class);
      debtPositionDTO.setDescription("Test Description");
      Organization organization = podamFactory.manufacturePojo(Organization.class);
      Broker broker = podamFactory.manufacturePojo(Broker.class);
      PaDemandPaymentNoticeResponse paDemandPaymentNoticeResponse = podamFactory.manufacturePojo(PaDemandPaymentNoticeResponse.class);

      RegistryContextData expectedRegistryContextData = RegistryContextData.builder()
        .eventType(RegistryEventType.PaForNode_paDemandPaymentNotice)
        .orgFiscalCode(request.getIdPA())
        .brokerStationId(request.getIdStation())
        .build();
      configureRegistryLoggerMock(expectedRegistryContextData, request);

      when(demandPaymentNoticeServiceMock.handleRequest(request))
        .thenReturn(Triple.of(debtPositionDTO, organization, broker));
      mapperMock.when(() -> PaDemandPaymentNoticeMapper.debtPositionDto2PaVerifyPaymentNoticeRes(debtPositionDTO, organization, broker))
        .thenReturn(paDemandPaymentNoticeResponse);

      // when
      PaDemandPaymentNoticeResponse response = paForNodeEndpoint.paDemandPaymentNotice(request);

      // verify
      assertNotNull(response);
      assertEquals(paDemandPaymentNoticeResponse, response);
    }
  }

  @Test
  void givenAnyWhenPaDemandPaymentNoticeThenFault() {
    // given
    PaDemandPaymentNoticeRequest paDemandPaymentNoticeRequest = podamFactory.manufacturePojo(PaDemandPaymentNoticeRequest.class);

    RegistryContextData expectedRegistryContextData = RegistryContextData.builder()
      .eventType(RegistryEventType.PaForNode_paDemandPaymentNotice)
      .orgFiscalCode(paDemandPaymentNoticeRequest.getIdPA())
      .brokerStationId(paDemandPaymentNoticeRequest.getIdStation())
      .build();
    configureRegistryLoggerMock(expectedRegistryContextData, paDemandPaymentNoticeRequest);

    when(demandPaymentNoticeServiceMock.handleRequest(paDemandPaymentNoticeRequest))
      .thenThrow(new RuntimeException("Simulated System Error"));

    // when
    PaDemandPaymentNoticeResponse response = paForNodeEndpoint.paDemandPaymentNotice(paDemandPaymentNoticeRequest);

    // verify
    assertNotNull(response);
    assertNotNull(response.getFault());
    assertEquals(PagoPaNodeFaults.PAA_SYSTEM_ERROR.code(), response.getFault().getFaultCode());
    assertEquals(paDemandPaymentNoticeRequest.getIdBrokerPA(), response.getFault().getId());
  }

  @Test
  void givenPagoPaNodeFaultExceptionWhenPaDemandPaymentNoticeThenSpecificFault() {
    PaDemandPaymentNoticeRequest request = podamFactory.manufacturePojo(PaDemandPaymentNoticeRequest.class);
    PagoPaNodeFaults specificError = PagoPaNodeFaults.PAA_ID_DOMINIO_ERRATO;
    String specificEmitter = "SPECIFIC_EMITTER";
    PagoPaNodeFaultException exceptionToThrow = new PagoPaNodeFaultException(specificError, specificEmitter);

    RegistryContextData expectedContextData = RegistryContextData.builder()
      .eventType(RegistryEventType.PaForNode_paDemandPaymentNotice)
      .orgFiscalCode(request.getIdPA())
      .brokerStationId(request.getIdStation())
      .build();
    configureRegistryLoggerMock(expectedContextData, request);

    when(demandPaymentNoticeServiceMock.handleRequest(request))
      .thenThrow(exceptionToThrow);

    // WHEN
    PaDemandPaymentNoticeResponse response = paForNodeEndpoint.paDemandPaymentNotice(request);

    // THEN
    assertNotNull(response);
    assertNotNull(response.getFault());
    assertEquals(specificError.code(), response.getFault().getFaultCode());
    assertEquals(specificEmitter, response.getFault().getId());
    assertNotEquals(StOutcome.OK, response.getOutcome());
  }
  //endregion


  //region paVerifyPaymentNotice
  @Test
  void givenValidPaVerifyPaymentNoticeReqWhenPaVerifyPaymentNoticeThenOk() {
    try (MockedStatic<PaVerifyPaymentNoticeMapper> mapperMock = Mockito.mockStatic(PaVerifyPaymentNoticeMapper.class)) {
      // given
      PaVerifyPaymentNoticeReq paVerifyPaymentNoticeReq = podamFactory.manufacturePojo(PaVerifyPaymentNoticeReq.class);
      paVerifyPaymentNoticeReq.getQrCode().setNoticeNumber("3NAV");

      RetrievePaymentDTO retrievePaymentDTO = podamFactory.manufacturePojo(RetrievePaymentDTO.class);
      InstallmentDTO installmentDTO = podamFactory.manufacturePojo(InstallmentDTO.class);
      Organization organization = podamFactory.manufacturePojo(Organization.class);
      Broker broker = podamFactory.manufacturePojo(Broker.class);
      PaVerifyPaymentNoticeRes paVerifyPaymentNoticeRes = podamFactory.manufacturePojo(PaVerifyPaymentNoticeRes.class);

      RegistryContextData expectedRegistryContextData = RegistryContextData.builder()
        .eventType(RegistryEventType.PaForNode_paVerifyPaymentNotice)
        .orgFiscalCode(paVerifyPaymentNoticeReq.getIdPA())
        .brokerStationId(paVerifyPaymentNoticeReq.getIdStation())
        .iuv("NAV")
        .build();
      configureRegistryLoggerMock(expectedRegistryContextData, paVerifyPaymentNoticeReq);

      mapperMock.when(() -> PaVerifyPaymentNoticeMapper.paVerifyPaymentNoticeReq2RetrievePaymentDTO(paVerifyPaymentNoticeReq))
        .thenReturn(retrievePaymentDTO);

      when(synchronousPaymentServiceMock.retrievePayment(retrievePaymentDTO))
        .thenReturn(Triple.of(installmentDTO, organization, broker));

      mapperMock.when(() -> PaVerifyPaymentNoticeMapper.installmentDto2PaVerifyPaymentNoticeRes(installmentDTO, organization, broker))
        .thenReturn(paVerifyPaymentNoticeRes);

      // when
      PaVerifyPaymentNoticeRes response = paForNodeEndpoint.paVerifyPaymentNotice(paVerifyPaymentNoticeReq);

      // verify
      assertEquals(paVerifyPaymentNoticeRes, response);
    }
  }

  @Test
  void givenInvalidPaVerifyPaymentNoticeReqWhenPaVerifyPaymentNoticeThenFault() {
    try (MockedStatic<PaVerifyPaymentNoticeMapper> mapperMock = Mockito.mockStatic(PaVerifyPaymentNoticeMapper.class)) {
      // given
      PaVerifyPaymentNoticeReq paVerifyPaymentNoticeReq = podamFactory.manufacturePojo(PaVerifyPaymentNoticeReq.class);
      paVerifyPaymentNoticeReq.getQrCode().setNoticeNumber("3NAV");
      RetrievePaymentDTO retrievePaymentDTO = podamFactory.manufacturePojo(RetrievePaymentDTO.class);

      RegistryContextData expectedRegistryContextData = RegistryContextData.builder()
        .eventType(RegistryEventType.PaForNode_paVerifyPaymentNotice)
        .orgFiscalCode(paVerifyPaymentNoticeReq.getIdPA())
        .brokerStationId(paVerifyPaymentNoticeReq.getIdStation())
        .iuv("NAV")
        .build();
      configureRegistryLoggerMock(expectedRegistryContextData, paVerifyPaymentNoticeReq);

      mapperMock.when(() -> PaVerifyPaymentNoticeMapper.paVerifyPaymentNoticeReq2RetrievePaymentDTO(paVerifyPaymentNoticeReq)).thenReturn(retrievePaymentDTO);
      when(synchronousPaymentServiceMock.retrievePayment(retrievePaymentDTO)).thenThrow(new PagoPaNodeFaultException(PagoPaNodeFaults.PAA_SEMANTICA, "EMITTER"));

      // when
      PaVerifyPaymentNoticeRes response = paForNodeEndpoint.paVerifyPaymentNotice(paVerifyPaymentNoticeReq);

      // verify
      assertNotNull(response);
      assertNotNull(response.getFault());
      assertEquals(PagoPaNodeFaults.PAA_SEMANTICA.code(), response.getFault().getFaultCode());
      assertEquals("EMITTER", response.getFault().getId());
    }
  }

  @Test
  void givenSystemErrorWhenPaVerifyPaymentNoticeThenFault() {
    try (MockedStatic<PaVerifyPaymentNoticeMapper> mapperMock = Mockito.mockStatic(PaVerifyPaymentNoticeMapper.class)) {
      // given
      PaVerifyPaymentNoticeReq paVerifyPaymentNoticeReq = podamFactory.manufacturePojo(PaVerifyPaymentNoticeReq.class);
      paVerifyPaymentNoticeReq.getQrCode().setNoticeNumber("3NAV");
      RetrievePaymentDTO retrievePaymentDTO = podamFactory.manufacturePojo(RetrievePaymentDTO.class);

      RegistryContextData expectedRegistryContextData = RegistryContextData.builder()
        .eventType(RegistryEventType.PaForNode_paVerifyPaymentNotice)
        .orgFiscalCode(paVerifyPaymentNoticeReq.getIdPA())
        .brokerStationId(paVerifyPaymentNoticeReq.getIdStation())
        .iuv("NAV")
        .build();
      configureRegistryLoggerMock(expectedRegistryContextData, paVerifyPaymentNoticeReq);

      mapperMock.when(() -> PaVerifyPaymentNoticeMapper.paVerifyPaymentNoticeReq2RetrievePaymentDTO(paVerifyPaymentNoticeReq)).thenReturn(retrievePaymentDTO);
      when(synchronousPaymentServiceMock.retrievePayment(retrievePaymentDTO)).thenThrow(new RuntimeException("RUNTIME EXCEPTION"));

      // when
      PaVerifyPaymentNoticeRes response = paForNodeEndpoint.paVerifyPaymentNotice(paVerifyPaymentNoticeReq);

      // verify
      assertNotNull(response);
      assertNotNull(response.getFault());
      assertEquals(PagoPaNodeFaults.PAA_SYSTEM_ERROR.code(), response.getFault().getFaultCode());
      assertEquals(paVerifyPaymentNoticeReq.getIdPA(), response.getFault().getId());
    }
  }
  //endregion

  //region paGetPayment
  @Test
  void givenAnyWhenPaGetPaymentThenFault() {
    // given
    PaGetPaymentReq request = podamFactory.manufacturePojo(PaGetPaymentReq.class);

    // when
    PaGetPaymentRes response = paForNodeEndpoint.paGetPayment(request);

    // verify
    assertNotNull(response);
    assertNotNull(response.getFault());
    assertEquals(PagoPaNodeFaults.PAA_SYSTEM_ERROR.code(), response.getFault().getFaultCode());
    assertEquals(request.getIdBrokerPA(), response.getFault().getId());
  }
  //endregion

  //region paGetPaymentV2
  @Test
  void givenValidPaGetPaymentV2RequestNonPostalWhenPaGetPaymentV2ThenOk() {
    try (MockedStatic<PaGetPaymentMapper> mapperMock = Mockito.mockStatic(PaGetPaymentMapper.class)) {
      // given
      PaGetPaymentV2Request paGetPaymentV2Request = podamFactory.manufacturePojo(PaGetPaymentV2Request.class);
      paGetPaymentV2Request.getQrCode().setNoticeNumber("3NAV");

      RetrievePaymentDTO retrievePaymentDTO = podamFactory.manufacturePojo(RetrievePaymentDTO.class);
      InstallmentDTO installmentDTO = podamFactory.manufacturePojo(InstallmentDTO.class);
      Organization organization = podamFactory.manufacturePojo(Organization.class);
      Broker broker = podamFactory.manufacturePojo(Broker.class);
      PaGetPaymentV2Response paGetPaymentV2Response = podamFactory.manufacturePojo(PaGetPaymentV2Response.class);

      paGetPaymentV2Request.setTransferType(StTransferType.PAGOPA);
      retrievePaymentDTO.setPostalTransfer(false);
      installmentDTO.getTransfers().forEach(t -> {
        t.setStampHashDocument(null);
        t.setStampProvincialResidence(null);
        t.setStampType(null);
      });

      RegistryContextData expectedRegistryContextData = RegistryContextData.builder()
        .eventType(RegistryEventType.PaForNode_paGetPaymentV2)
        .orgFiscalCode(paGetPaymentV2Request.getIdPA())
        .brokerStationId(paGetPaymentV2Request.getIdStation())
        .iuv("NAV")
        .build();
      configureRegistryLoggerMock(expectedRegistryContextData, paGetPaymentV2Request);

      mapperMock.when(() -> PaGetPaymentMapper.paPaGetPaymentV2Request2RetrievePaymentDTO(paGetPaymentV2Request))
        .thenReturn(retrievePaymentDTO);

      when(synchronousPaymentServiceMock.retrievePayment(retrievePaymentDTO))
        .thenReturn(Triple.of(installmentDTO, organization, broker));

      mapperMock.when(() -> PaGetPaymentMapper.installmentDto2PaGetPaymentV2Response(installmentDTO, organization, broker, paGetPaymentV2Request.getTransferType()))
        .thenReturn(paGetPaymentV2Response);

      // when
      PaGetPaymentV2Response response = paForNodeEndpoint.paGetPaymentV2(paGetPaymentV2Request);

      // verify
      assertEquals(paGetPaymentV2Response, response);
    }
  }

  @Test
  void givenValidPaGetPaymentV2RequestPostalWhenPaGetPaymentV2ThenOk() {
    try (MockedStatic<PaGetPaymentMapper> mapperMock = Mockito.mockStatic(PaGetPaymentMapper.class)) {
      // given
      PaGetPaymentV2Request paGetPaymentV2Request = podamFactory.manufacturePojo(PaGetPaymentV2Request.class);
      paGetPaymentV2Request.getQrCode().setNoticeNumber("3NAV");

      RetrievePaymentDTO retrievePaymentDTO = podamFactory.manufacturePojo(RetrievePaymentDTO.class);
      InstallmentDTO installmentDTO = podamFactory.manufacturePojo(InstallmentDTO.class);
      Organization organization = podamFactory.manufacturePojo(Organization.class);
      Broker broker = podamFactory.manufacturePojo(Broker.class);
      PaGetPaymentV2Response paGetPaymentV2Response = podamFactory.manufacturePojo(PaGetPaymentV2Response.class);

      paGetPaymentV2Request.setTransferType(StTransferType.POSTAL);
      retrievePaymentDTO.setPostalTransfer(false);
      installmentDTO.getTransfers().forEach(t -> {
        t.setStampHashDocument(null);
        t.setStampProvincialResidence(null);
        t.setStampType(null);
      });

      RegistryContextData expectedRegistryContextData = RegistryContextData.builder()
        .eventType(RegistryEventType.PaForNode_paGetPaymentV2)
        .orgFiscalCode(paGetPaymentV2Request.getIdPA())
        .brokerStationId(paGetPaymentV2Request.getIdStation())
        .iuv("NAV")
        .build();
      configureRegistryLoggerMock(expectedRegistryContextData, paGetPaymentV2Request);

      mapperMock.when(() -> PaGetPaymentMapper.paPaGetPaymentV2Request2RetrievePaymentDTO(paGetPaymentV2Request))
        .thenReturn(retrievePaymentDTO);

      when(synchronousPaymentServiceMock.retrievePayment(retrievePaymentDTO))
        .thenReturn(Triple.of(installmentDTO, organization, broker));

      mapperMock.when(() -> PaGetPaymentMapper.installmentDto2PaGetPaymentV2Response(installmentDTO, organization, broker, paGetPaymentV2Request.getTransferType()))
        .thenReturn(paGetPaymentV2Response);

      // when
      PaGetPaymentV2Response response = paForNodeEndpoint.paGetPaymentV2(paGetPaymentV2Request);

      // verify
      assertEquals(paGetPaymentV2Response, response);
    }
  }

  @Test
  void givenInvalidPaGetPaymentV2RequestNonPostalWhenPaGetPaymentV2ThenFault() {
    try (MockedStatic<PaGetPaymentMapper> mapperMock = Mockito.mockStatic(PaGetPaymentMapper.class)) {
      // given
      PaGetPaymentV2Request paGetPaymentV2Request = podamFactory.manufacturePojo(PaGetPaymentV2Request.class);
      paGetPaymentV2Request.getQrCode().setNoticeNumber("3NAV");
      RetrievePaymentDTO retrievePaymentDTO = podamFactory.manufacturePojo(RetrievePaymentDTO.class);

      RegistryContextData expectedRegistryContextData = RegistryContextData.builder()
        .eventType(RegistryEventType.PaForNode_paGetPaymentV2)
        .orgFiscalCode(paGetPaymentV2Request.getIdPA())
        .brokerStationId(paGetPaymentV2Request.getIdStation())
        .iuv("NAV")
        .build();
      configureRegistryLoggerMock(expectedRegistryContextData, paGetPaymentV2Request);

      mapperMock.when(() -> PaGetPaymentMapper.paPaGetPaymentV2Request2RetrievePaymentDTO(paGetPaymentV2Request)).thenReturn(retrievePaymentDTO);
      when(synchronousPaymentServiceMock.retrievePayment(retrievePaymentDTO)).thenThrow(new PagoPaNodeFaultException(PagoPaNodeFaults.PAA_SEMANTICA, "EMITTER"));

      // when
      PaGetPaymentV2Response response = paForNodeEndpoint.paGetPaymentV2(paGetPaymentV2Request);

      // verify
      assertNotNull(response);
      assertNotNull(response.getFault());
      assertEquals(PagoPaNodeFaults.PAA_SEMANTICA.code(), response.getFault().getFaultCode());
      assertEquals("EMITTER", response.getFault().getId());
    }
  }

  @Test
  void givenSystemErrorWhenPaGetPaymentV2ThenError() {
    try (MockedStatic<PaGetPaymentMapper> mapperMock = Mockito.mockStatic(PaGetPaymentMapper.class)) {
      // given
      PaGetPaymentV2Request paGetPaymentReq = podamFactory.manufacturePojo(PaGetPaymentV2Request.class);
      paGetPaymentReq.getQrCode().setNoticeNumber("3NAV");
      RetrievePaymentDTO retrievePaymentDTO = podamFactory.manufacturePojo(RetrievePaymentDTO.class);

      RegistryContextData expectedRegistryContextData = RegistryContextData.builder()
        .eventType(RegistryEventType.PaForNode_paGetPaymentV2)
        .orgFiscalCode(paGetPaymentReq.getIdPA())
        .brokerStationId(paGetPaymentReq.getIdStation())
        .iuv("NAV")
        .build();
      configureRegistryLoggerMock(expectedRegistryContextData, paGetPaymentReq);

      mapperMock.when(() -> PaGetPaymentMapper.paPaGetPaymentV2Request2RetrievePaymentDTO(paGetPaymentReq)).thenReturn(retrievePaymentDTO);
      when(synchronousPaymentServiceMock.retrievePayment(retrievePaymentDTO)).thenThrow(new RuntimeException("RUNTIME EXCEPTION"));

      // when
      PaGetPaymentV2Response response = paForNodeEndpoint.paGetPaymentV2(paGetPaymentReq);

      // verify
      assertNotNull(response);
      assertNotNull(response.getFault());
      assertEquals(PagoPaNodeFaults.PAA_SYSTEM_ERROR.code(), response.getFault().getFaultCode());
      assertEquals(paGetPaymentReq.getIdPA(), response.getFault().getId());
    }
  }
  //endregion

  //region paSendRT
  @Test
  void givenAnyWhenPaSendRTThenFault() {
    // given
    PaSendRTReq request = podamFactory.manufacturePojo(PaSendRTReq.class);

    // when
    PaSendRTRes response = paForNodeEndpoint.paSendRT(request);

    // verify
    assertNotNull(response);
    assertNotNull(response.getFault());
    assertEquals(PagoPaNodeFaults.PAA_SYSTEM_ERROR.code(), response.getFault().getFaultCode());
    assertEquals(request.getIdBrokerPA(), response.getFault().getId());
  }
  //endregion

  //region paSendRTV2
  private Pair<PaSendRTV2Request, PaSendRtDTO> configurePaSendRTV2Request() {
    PaSendRTV2Request request = podamFactory.manufacturePojo(PaSendRTV2Request.class);
    request.setReceipt(podamFactory.manufacturePojo(CtReceiptV2.class));
    request.getReceipt().setNoticeNumber("3123456");
    PaSendRtDTO paSendRtDTO = podamFactory.manufacturePojo(PaSendRtDTO.class);

    when(paSendRTMapperMock.paSendRtV2Request2PaSendRtDTO(request)).thenReturn(paSendRtDTO);

    RegistryContextData expectedContextData = RegistryContextData.builder()
      .orgFiscalCode(request.getReceipt().getFiscalCode())
      .brokerStationId(request.getIdStation())
      .pspId(request.getReceipt().getIdPSP())
      .pspChannelId(request.getReceipt().getIdChannel())
      .paymentMethod(request.getReceipt().getPaymentMethod())
      .ccp(request.getReceipt().getReceiptId())
      .eventType(RegistryEventType.PaForNode_paSendRTV2)
      .iuv(Utilities.nav2Iuv(request.getReceipt().getNoticeNumber()))
      .build();

    configureRegistryLoggerMock(expectedContextData, request);

    return Pair.of(request, paSendRtDTO);
  }

  @Test
  void givenValidPaSendRTV2RequestWhenPaSendRTV2ThenOk() {
    // given
    Pair<PaSendRTV2Request, PaSendRtDTO> request2mapped = configurePaSendRTV2Request();
    PaSendRTV2Request request = request2mapped.getLeft();
    PaSendRtDTO requestMapped = request2mapped.getValue();

    when(receiptServiceMock.processReceivedReceipt(requestMapped)).thenReturn(1L);

    // when
    PaSendRTV2Response response = paForNodeEndpoint.paSendRTV2(request);

    // verify
    assertNotNull(response);
    assertNull(response.getFault());
    assertEquals(StOutcome.OK, response.getOutcome());
  }

  @Test
  void givenInvalidPaSendRTV2RequestWhenPaSendRTV2ThenFault() {
    // given
    Pair<PaSendRTV2Request, PaSendRtDTO> request2mapped = configurePaSendRTV2Request();
    PaSendRTV2Request request = request2mapped.getLeft();
    PaSendRtDTO requestMapped = request2mapped.getValue();

    Mockito.doThrow(new PagoPaNodeFaultException(PagoPaNodeFaults.PAA_SEMANTICA, "EMITTER")).when(receiptServiceMock).processReceivedReceipt(requestMapped);

    // when
    PaSendRTV2Response response = paForNodeEndpoint.paSendRTV2(request);

    // verify
    assertNotNull(response);
    assertNotNull(response.getFault());
    assertEquals(PagoPaNodeFaults.PAA_SEMANTICA.code(), response.getFault().getFaultCode());
    assertEquals("EMITTER", response.getFault().getId());
  }

  @Test
  void givenSystemErrorWhenPaSendRTV2ThenFault() {
    // given
    Pair<PaSendRTV2Request, PaSendRtDTO> request2mapped = configurePaSendRTV2Request();
    PaSendRTV2Request request = request2mapped.getLeft();
    PaSendRtDTO requestMapped = request2mapped.getValue();

    Mockito.doThrow(new RuntimeException("RUNTIME EXCEPTION")).when(receiptServiceMock).processReceivedReceipt(requestMapped);

    // when
    PaSendRTV2Response response = paForNodeEndpoint.paSendRTV2(request);

    // verify
    assertNotNull(response);
    assertNotNull(response.getFault());
    assertEquals(PagoPaNodeFaults.PAA_SYSTEM_ERROR.code(), response.getFault().getFaultCode());
    assertEquals(request.getIdPA(), response.getFault().getId());
  }
  //endregion

}
