package it.gov.pagopa.pu.pagopapayments.endpoint;

import it.gov.pagopa.pagopa_api.pa.pafornode.*;
import it.gov.pagopa.pagopa_api.xsd.common_types.v1_0.StOutcome;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.pagopapayments.dto.PaSendRtDTO;
import it.gov.pagopa.pu.pagopapayments.dto.RetrievePaymentDTO;
import it.gov.pagopa.pu.pagopapayments.enums.PagoPaNodeFaults;
import it.gov.pagopa.pu.pagopapayments.exception.PagoPaNodeFaultException;
import it.gov.pagopa.pu.pagopapayments.mapper.PaGetPaymentMapper;
import it.gov.pagopa.pu.pagopapayments.mapper.PaSendRTMapper;
import it.gov.pagopa.pu.pagopapayments.mapper.PaVerifyPaymentNoticeMapper;
import it.gov.pagopa.pu.pagopapayments.registry.RegistryContextData;
import it.gov.pagopa.pu.pagopapayments.registry.RegistryEventType;
import it.gov.pagopa.pu.pagopapayments.registry.RegistryLogger;
import it.gov.pagopa.pu.pagopapayments.registry.RegistryLoggerTest;
import it.gov.pagopa.pu.pagopapayments.service.receipt.ReceiptService;
import it.gov.pagopa.pu.pagopapayments.service.synchronouspayments.SynchronousPaymentService;
import it.gov.pagopa.pu.pagopapayments.util.TestUtils;
import it.gov.pagopa.pu.pagopapayments.util.Utilities;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.jemos.podam.api.PodamFactory;

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

  @InjectMocks
  private PaForNodeEndpoint paForNodeEndpoint;

  private final PodamFactory podamFactory = TestUtils.getPodamFactory();

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      synchronousPaymentServiceMock,
      receiptServiceMock,
      paSendRTMapperMock,
      registryLoggerMock);
  }

  private void configureRegistryLoggerMock(RegistryContextData contextData, Object request) {
    RegistryLoggerTest.configureRegistryLoggerMock(registryLoggerMock, contextData, request, false, false);
  }

  //region paDemandPaymentNotice
  @Test
  void givenAnyWhenPaDemandPaymentNoticeThenFault() {
    // given
    PaDemandPaymentNoticeRequest paDemandPaymentNoticeRequest = podamFactory.manufacturePojo(PaDemandPaymentNoticeRequest.class);

    // when
    PaDemandPaymentNoticeResponse response = paForNodeEndpoint.paDemandPaymentNotice(paDemandPaymentNoticeRequest);

    // verify
    Assertions.assertNotNull(response);
    Assertions.assertNotNull(response.getFault());
    Assertions.assertEquals(PagoPaNodeFaults.PAA_SYSTEM_ERROR.code(), response.getFault().getFaultCode());
    Assertions.assertEquals(paDemandPaymentNoticeRequest.getIdBrokerPA(), response.getFault().getId());
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
      PaVerifyPaymentNoticeRes paVerifyPaymentNoticeRes = podamFactory.manufacturePojo(PaVerifyPaymentNoticeRes.class);

      RegistryContextData expectedRegistryContextData = RegistryContextData.builder()
        .eventType(RegistryEventType.paVerifyPaymentNotice)
        .orgFiscalCode(paVerifyPaymentNoticeReq.getIdPA())
        .brokerStationId(paVerifyPaymentNoticeReq.getIdStation())
        .iuv("NAV")
        .build();
      configureRegistryLoggerMock(expectedRegistryContextData, paVerifyPaymentNoticeReq);

      mapperMock.when(() -> PaVerifyPaymentNoticeMapper.paVerifyPaymentNoticeReq2RetrievePaymentDTO(paVerifyPaymentNoticeReq)).thenReturn(retrievePaymentDTO);
      Mockito.when(synchronousPaymentServiceMock.retrievePayment(retrievePaymentDTO)).thenReturn(Pair.of(installmentDTO, organization));
      mapperMock.when(() -> PaVerifyPaymentNoticeMapper.installmentDto2PaVerifyPaymentNoticeRes(installmentDTO, organization)).thenReturn(paVerifyPaymentNoticeRes);

      // when
      PaVerifyPaymentNoticeRes response = paForNodeEndpoint.paVerifyPaymentNotice(paVerifyPaymentNoticeReq);

      // verify
      Assertions.assertEquals(paVerifyPaymentNoticeRes, response);
      Mockito.verify(synchronousPaymentServiceMock, Mockito.times(1)).retrievePayment(retrievePaymentDTO);
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
        .eventType(RegistryEventType.paVerifyPaymentNotice)
        .orgFiscalCode(paVerifyPaymentNoticeReq.getIdPA())
        .brokerStationId(paVerifyPaymentNoticeReq.getIdStation())
        .iuv("NAV")
        .build();
      configureRegistryLoggerMock(expectedRegistryContextData, paVerifyPaymentNoticeReq);

      mapperMock.when(() -> PaVerifyPaymentNoticeMapper.paVerifyPaymentNoticeReq2RetrievePaymentDTO(paVerifyPaymentNoticeReq)).thenReturn(retrievePaymentDTO);
      Mockito.when(synchronousPaymentServiceMock.retrievePayment(retrievePaymentDTO)).thenThrow(new PagoPaNodeFaultException(PagoPaNodeFaults.PAA_SEMANTICA, "EMITTER"));

      // when
      PaVerifyPaymentNoticeRes response = paForNodeEndpoint.paVerifyPaymentNotice(paVerifyPaymentNoticeReq);

      // verify
      Assertions.assertNotNull(response);
      Assertions.assertNotNull(response.getFault());
      Assertions.assertEquals(PagoPaNodeFaults.PAA_SEMANTICA.code(), response.getFault().getFaultCode());
      Assertions.assertEquals("EMITTER", response.getFault().getId());
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
        .eventType(RegistryEventType.paVerifyPaymentNotice)
        .orgFiscalCode(paVerifyPaymentNoticeReq.getIdPA())
        .brokerStationId(paVerifyPaymentNoticeReq.getIdStation())
        .iuv("NAV")
        .build();
      configureRegistryLoggerMock(expectedRegistryContextData, paVerifyPaymentNoticeReq);

      mapperMock.when(() -> PaVerifyPaymentNoticeMapper.paVerifyPaymentNoticeReq2RetrievePaymentDTO(paVerifyPaymentNoticeReq)).thenReturn(retrievePaymentDTO);
      Mockito.when(synchronousPaymentServiceMock.retrievePayment(retrievePaymentDTO)).thenThrow(new RuntimeException("RUNTIME EXCEPTION"));

      // when
      PaVerifyPaymentNoticeRes response = paForNodeEndpoint.paVerifyPaymentNotice(paVerifyPaymentNoticeReq);

      // verify
      Assertions.assertNotNull(response);
      Assertions.assertNotNull(response.getFault());
      Assertions.assertEquals(PagoPaNodeFaults.PAA_SYSTEM_ERROR.code(), response.getFault().getFaultCode());
      Assertions.assertEquals(paVerifyPaymentNoticeReq.getIdPA(), response.getFault().getId());
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
    Assertions.assertNotNull(response);
    Assertions.assertNotNull(response.getFault());
    Assertions.assertEquals(PagoPaNodeFaults.PAA_SYSTEM_ERROR.code(), response.getFault().getFaultCode());
    Assertions.assertEquals(request.getIdBrokerPA(), response.getFault().getId());
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
      PaGetPaymentV2Response paGetPaymentV2Response = podamFactory.manufacturePojo(PaGetPaymentV2Response.class);
      //set specific values for this test
      paGetPaymentV2Request.setTransferType(StTransferType.PAGOPA);
      retrievePaymentDTO.setPostalTransfer(false);
      installmentDTO.getTransfers().forEach(t -> {
        t.setStampHashDocument(null);
        t.setStampProvincialResidence(null);
        t.setStampType(null);
      });

      RegistryContextData expectedRegistryContextData = RegistryContextData.builder()
        .eventType(RegistryEventType.paGetPaymentV2)
        .orgFiscalCode(paGetPaymentV2Request.getIdPA())
        .brokerStationId(paGetPaymentV2Request.getIdStation())
        .iuv("NAV")
        .build();
      configureRegistryLoggerMock(expectedRegistryContextData, paGetPaymentV2Request);

      mapperMock.when(() -> PaGetPaymentMapper.paPaGetPaymentV2Request2RetrievePaymentDTO(paGetPaymentV2Request)).thenReturn(retrievePaymentDTO);
      Mockito.when(synchronousPaymentServiceMock.retrievePayment(retrievePaymentDTO)).thenReturn(Pair.of(installmentDTO, organization));
      mapperMock.when(() -> PaGetPaymentMapper.installmentDto2PaGetPaymentV2Response(installmentDTO, organization, paGetPaymentV2Request.getTransferType())).thenReturn(paGetPaymentV2Response);

      // when
      PaGetPaymentV2Response response = paForNodeEndpoint.paGetPaymentV2(paGetPaymentV2Request);

      // verify
      Assertions.assertEquals(paGetPaymentV2Response, response);
      Mockito.verify(synchronousPaymentServiceMock, Mockito.times(1)).retrievePayment(retrievePaymentDTO);
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
      PaGetPaymentV2Response paGetPaymentV2Response = podamFactory.manufacturePojo(PaGetPaymentV2Response.class);
      //set specific values for this test
      paGetPaymentV2Request.setTransferType(StTransferType.POSTAL);
      retrievePaymentDTO.setPostalTransfer(false);
      installmentDTO.getTransfers().forEach(t -> {
        t.setStampHashDocument(null);
        t.setStampProvincialResidence(null);
        t.setStampType(null);
      });

      RegistryContextData expectedRegistryContextData = RegistryContextData.builder()
        .eventType(RegistryEventType.paGetPaymentV2)
        .orgFiscalCode(paGetPaymentV2Request.getIdPA())
        .brokerStationId(paGetPaymentV2Request.getIdStation())
        .iuv("NAV")
        .build();
      configureRegistryLoggerMock(expectedRegistryContextData, paGetPaymentV2Request);

      mapperMock.when(() -> PaGetPaymentMapper.paPaGetPaymentV2Request2RetrievePaymentDTO(paGetPaymentV2Request)).thenReturn(retrievePaymentDTO);
      Mockito.when(synchronousPaymentServiceMock.retrievePayment(retrievePaymentDTO)).thenReturn(Pair.of(installmentDTO, organization));
      mapperMock.when(() -> PaGetPaymentMapper.installmentDto2PaGetPaymentV2Response(installmentDTO, organization, paGetPaymentV2Request.getTransferType())).thenReturn(paGetPaymentV2Response);

      // when
      PaGetPaymentV2Response response = paForNodeEndpoint.paGetPaymentV2(paGetPaymentV2Request);

      // verify
      Assertions.assertEquals(paGetPaymentV2Response, response);
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
        .eventType(RegistryEventType.paGetPaymentV2)
        .orgFiscalCode(paGetPaymentV2Request.getIdPA())
        .brokerStationId(paGetPaymentV2Request.getIdStation())
        .iuv("NAV")
        .build();
      configureRegistryLoggerMock(expectedRegistryContextData, paGetPaymentV2Request);

      mapperMock.when(() -> PaGetPaymentMapper.paPaGetPaymentV2Request2RetrievePaymentDTO(paGetPaymentV2Request)).thenReturn(retrievePaymentDTO);
      Mockito.when(synchronousPaymentServiceMock.retrievePayment(retrievePaymentDTO)).thenThrow(new PagoPaNodeFaultException(PagoPaNodeFaults.PAA_SEMANTICA, "EMITTER"));

      // when
      PaGetPaymentV2Response response = paForNodeEndpoint.paGetPaymentV2(paGetPaymentV2Request);

      // verify
      Assertions.assertNotNull(response);
      Assertions.assertNotNull(response.getFault());
      Assertions.assertEquals(PagoPaNodeFaults.PAA_SEMANTICA.code(), response.getFault().getFaultCode());
      Assertions.assertEquals("EMITTER", response.getFault().getId());
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
        .eventType(RegistryEventType.paGetPaymentV2)
        .orgFiscalCode(paGetPaymentReq.getIdPA())
        .brokerStationId(paGetPaymentReq.getIdStation())
        .iuv("NAV")
        .build();
      configureRegistryLoggerMock(expectedRegistryContextData, paGetPaymentReq);

      mapperMock.when(() -> PaGetPaymentMapper.paPaGetPaymentV2Request2RetrievePaymentDTO(paGetPaymentReq)).thenReturn(retrievePaymentDTO);
      Mockito.when(synchronousPaymentServiceMock.retrievePayment(retrievePaymentDTO)).thenThrow(new RuntimeException("RUNTIME EXCEPTION"));

      // when
      PaGetPaymentV2Response response = paForNodeEndpoint.paGetPaymentV2(paGetPaymentReq);

      // verify
      Assertions.assertNotNull(response);
      Assertions.assertNotNull(response.getFault());
      Assertions.assertEquals(PagoPaNodeFaults.PAA_SYSTEM_ERROR.code(), response.getFault().getFaultCode());
      Assertions.assertEquals(paGetPaymentReq.getIdPA(), response.getFault().getId());
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
    Assertions.assertNotNull(response);
    Assertions.assertNotNull(response.getFault());
    Assertions.assertEquals(PagoPaNodeFaults.PAA_SYSTEM_ERROR.code(), response.getFault().getFaultCode());
    Assertions.assertEquals(request.getIdBrokerPA(), response.getFault().getId());
  }

  //endregion

  //region paSendRTV2
  private Pair<PaSendRTV2Request, PaSendRtDTO> configurePaSendRTV2Request() {
    PaSendRTV2Request request = podamFactory.manufacturePojo(PaSendRTV2Request.class);
    request.setReceipt(podamFactory.manufacturePojo(CtReceiptV2.class));
    request.getReceipt().setNoticeNumber("3123456");
    PaSendRtDTO paSendRtDTO = podamFactory.manufacturePojo(PaSendRtDTO.class);

    Mockito.when(paSendRTMapperMock.paSendRtV2Request2PaSendRtDTO(request)).thenReturn(paSendRtDTO);

    RegistryContextData expectedContextData = RegistryContextData.builder()
      .orgFiscalCode(request.getReceipt().getFiscalCode())
      .brokerStationId(request.getIdStation())
      .pspId(request.getReceipt().getIdPSP())
      .pspChannelId(request.getReceipt().getIdChannel())
      .paymentMethod(request.getReceipt().getPaymentMethod())
      .ccp(request.getReceipt().getReceiptId())
      .eventType(RegistryEventType.paSendRTV2)
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

    Mockito.when(receiptServiceMock.processReceivedReceipt(requestMapped)).thenReturn(1L);

    // when
    PaSendRTV2Response response = paForNodeEndpoint.paSendRTV2(request);

    // verify
    Assertions.assertNotNull(response);
    Assertions.assertNull(response.getFault());
    Assertions.assertEquals(StOutcome.OK, response.getOutcome());
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
    Assertions.assertNotNull(response);
    Assertions.assertNotNull(response.getFault());
    Assertions.assertEquals(PagoPaNodeFaults.PAA_SEMANTICA.code(), response.getFault().getFaultCode());
    Assertions.assertEquals("EMITTER", response.getFault().getId());
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
    Assertions.assertNotNull(response);
    Assertions.assertNotNull(response.getFault());
    Assertions.assertEquals(PagoPaNodeFaults.PAA_SYSTEM_ERROR.code(), response.getFault().getFaultCode());
    Assertions.assertEquals(request.getIdPA(), response.getFault().getId());
  }

  //endregion

}
