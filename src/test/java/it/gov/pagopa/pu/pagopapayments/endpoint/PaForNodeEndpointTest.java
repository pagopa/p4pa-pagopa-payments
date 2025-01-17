package it.gov.pagopa.pu.pagopapayments.endpoint;

import it.gov.pagopa.pagopa_api.pa.pafornode.*;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.pagopapayments.dto.RetrievePaymentDTO;
import it.gov.pagopa.pu.pagopapayments.enums.PagoPaNodeFaults;
import it.gov.pagopa.pu.pagopapayments.exception.SynchronousPaymentException;
import it.gov.pagopa.pu.pagopapayments.mapper.PaGetPaymentMapper;
import it.gov.pagopa.pu.pagopapayments.mapper.PaVerifyPaymentNoticeMapper;
import it.gov.pagopa.pu.pagopapayments.service.synchronouspayments.PaymentService;
import it.gov.pagopa.pu.pagopapayments.util.TestUtils;
import org.apache.commons.lang3.tuple.Pair;
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
  private PaymentService paymentServiceMock;

  @InjectMocks
  private PaForNodeEndpoint paForNodeEndpoint;

  private final PodamFactory podamFactory;

  PaForNodeEndpointTest() {
    podamFactory = TestUtils.getPodamFactory();
  }

  //region paDemandPaymentNotice

  @Test
  void givenAnyWhenPaDemandPaymentNoticeThenUnsupportedOperationException() {
    // given
    PaDemandPaymentNoticeRequest paDemandPaymentNoticeRequest = podamFactory.manufacturePojo(PaDemandPaymentNoticeRequest.class);

    //when
    UnsupportedOperationException exception = Assertions.assertThrows(UnsupportedOperationException.class, () -> paForNodeEndpoint.paDemandPaymentNotice(paDemandPaymentNoticeRequest));

    //verify
    Assertions.assertEquals("paDemandPaymentNotice is not supported", exception.getMessage());
  }

  //endregion


  //region paVerifyPaymentNotice

  @Test
  void givenValidPaVerifyPaymentNoticeReqWhenPaVerifyPaymentNoticeThenOk() {
    try (MockedStatic<PaVerifyPaymentNoticeMapper> mapperMock = Mockito.mockStatic(PaVerifyPaymentNoticeMapper.class)) {
      // given
      PaVerifyPaymentNoticeReq paVerifyPaymentNoticeReq = podamFactory.manufacturePojo(PaVerifyPaymentNoticeReq.class);
      RetrievePaymentDTO retrievePaymentDTO = podamFactory.manufacturePojo(RetrievePaymentDTO.class);
      InstallmentDTO installmentDTO = podamFactory.manufacturePojo(InstallmentDTO.class);
      Organization organization = podamFactory.manufacturePojo(Organization.class);
      PaVerifyPaymentNoticeRes paVerifyPaymentNoticeRes = podamFactory.manufacturePojo(PaVerifyPaymentNoticeRes.class);

      mapperMock.when(() -> PaVerifyPaymentNoticeMapper.paVerifyPaymentNoticeReq2RetrievePaymentDTO(paVerifyPaymentNoticeReq)).thenReturn(retrievePaymentDTO);
      Mockito.when(paymentServiceMock.retrievePayment(retrievePaymentDTO)).thenReturn(Pair.of(installmentDTO, organization));
      mapperMock.when(() -> PaVerifyPaymentNoticeMapper.installmentDto2PaVerifyPaymentNoticeRes(installmentDTO, organization)).thenReturn(paVerifyPaymentNoticeRes);

      // when
      PaVerifyPaymentNoticeRes response = paForNodeEndpoint.paVerifyPaymentNotice(paVerifyPaymentNoticeReq);

      // verify
      Assertions.assertEquals(paVerifyPaymentNoticeRes, response);
      Mockito.verify(paymentServiceMock, Mockito.times(1)).retrievePayment(retrievePaymentDTO);
      mapperMock.verify(() -> PaVerifyPaymentNoticeMapper.paVerifyPaymentNoticeReq2RetrievePaymentDTO(paVerifyPaymentNoticeReq), Mockito.times(1));
      mapperMock.verify(() -> PaVerifyPaymentNoticeMapper.installmentDto2PaVerifyPaymentNoticeRes(installmentDTO, organization), Mockito.times(1));
    }
  }

  @Test
  void givenInvalidPaVerifyPaymentNoticeReqWhenPaVerifyPaymentNoticeThenFault() {
    try (MockedStatic<PaVerifyPaymentNoticeMapper> mapperMock = Mockito.mockStatic(PaVerifyPaymentNoticeMapper.class)) {
      // given
      PaVerifyPaymentNoticeReq paVerifyPaymentNoticeReq = podamFactory.manufacturePojo(PaVerifyPaymentNoticeReq.class);
      RetrievePaymentDTO retrievePaymentDTO = podamFactory.manufacturePojo(RetrievePaymentDTO.class);

      mapperMock.when(() -> PaVerifyPaymentNoticeMapper.paVerifyPaymentNoticeReq2RetrievePaymentDTO(paVerifyPaymentNoticeReq)).thenReturn(retrievePaymentDTO);
      Mockito.when(paymentServiceMock.retrievePayment(retrievePaymentDTO)).thenThrow(new SynchronousPaymentException(PagoPaNodeFaults.PAA_SYSTEM_ERROR.code(), "EMITTER"));

      // when
      PaVerifyPaymentNoticeRes response = paForNodeEndpoint.paVerifyPaymentNotice(paVerifyPaymentNoticeReq);

      // verify
      Assertions.assertNotNull(response);
      Assertions.assertNotNull(response.getFault());
      Assertions.assertEquals(PagoPaNodeFaults.PAA_SYSTEM_ERROR.code(), response.getFault().getFaultCode());
      Assertions.assertEquals("EMITTER", response.getFault().getId());
      Mockito.verify(paymentServiceMock, Mockito.times(1)).retrievePayment(retrievePaymentDTO);
      mapperMock.verify(() -> PaVerifyPaymentNoticeMapper.paVerifyPaymentNoticeReq2RetrievePaymentDTO(paVerifyPaymentNoticeReq), Mockito.times(1));
    }
  }

  //endregion

  //region paGetPayment

  @Test
  void givenValidPaGetPaymentReqNonPostalWhenPaGetPaymentThenOk() {
    try (MockedStatic<PaGetPaymentMapper> mapperMock = Mockito.mockStatic(PaGetPaymentMapper.class)) {
      // given
      PaGetPaymentReq paGetPaymentReq = podamFactory.manufacturePojo(PaGetPaymentReq.class);
      RetrievePaymentDTO retrievePaymentDTO = podamFactory.manufacturePojo(RetrievePaymentDTO.class);
      InstallmentDTO installmentDTO = podamFactory.manufacturePojo(InstallmentDTO.class);
      Organization organization = podamFactory.manufacturePojo(Organization.class);
      PaGetPaymentRes paGetPaymentRes = podamFactory.manufacturePojo(PaGetPaymentRes.class);
      //set specific values for this test
      paGetPaymentReq.setTransferType(StTransferType.PAGOPA);
      retrievePaymentDTO.setPostalTransfer(false);
      installmentDTO.getTransfers().forEach(t -> {
        t.setStampHashDocument(null);
        t.setStampProvincialResidence(null);
        t.setStampType(null);
      });

      mapperMock.when(() -> PaGetPaymentMapper.paPaGetPaymentReq2RetrievePaymentDTO(paGetPaymentReq)).thenReturn(retrievePaymentDTO);
      Mockito.when(paymentServiceMock.retrievePayment(retrievePaymentDTO)).thenReturn(Pair.of(installmentDTO, organization));
      mapperMock.when(() -> PaGetPaymentMapper.installmentDto2PaGetPaymentRes(installmentDTO, organization, paGetPaymentReq.getTransferType())).thenReturn(paGetPaymentRes);

      // when
      PaGetPaymentRes response = paForNodeEndpoint.paGetPayment(paGetPaymentReq);

      // verify
      Assertions.assertEquals(paGetPaymentRes, response);
      Mockito.verify(paymentServiceMock, Mockito.times(1)).retrievePayment(retrievePaymentDTO);
      mapperMock.verify(() -> PaGetPaymentMapper.paPaGetPaymentReq2RetrievePaymentDTO(paGetPaymentReq), Mockito.times(1));
      mapperMock.verify(() -> PaGetPaymentMapper.installmentDto2PaGetPaymentRes(installmentDTO, organization, paGetPaymentReq.getTransferType()), Mockito.times(1));
    }
  }

  @Test
  void givenValidPaGetPaymentReqPostalWhenPaGetPaymentThenOk() {
    try (MockedStatic<PaGetPaymentMapper> mapperMock = Mockito.mockStatic(PaGetPaymentMapper.class)) {
      // given
      PaGetPaymentReq paGetPaymentReq = podamFactory.manufacturePojo(PaGetPaymentReq.class);
      RetrievePaymentDTO retrievePaymentDTO = podamFactory.manufacturePojo(RetrievePaymentDTO.class);
      InstallmentDTO installmentDTO = podamFactory.manufacturePojo(InstallmentDTO.class);
      Organization organization = podamFactory.manufacturePojo(Organization.class);
      PaGetPaymentRes paGetPaymentRes = podamFactory.manufacturePojo(PaGetPaymentRes.class);
      //set specific values for this test
      paGetPaymentReq.setTransferType(StTransferType.POSTAL);
      retrievePaymentDTO.setPostalTransfer(true);
      installmentDTO.getTransfers().forEach(t -> {
        t.setStampHashDocument(null);
        t.setStampProvincialResidence(null);
        t.setStampType(null);
      });

      mapperMock.when(() -> PaGetPaymentMapper.paPaGetPaymentReq2RetrievePaymentDTO(paGetPaymentReq)).thenReturn(retrievePaymentDTO);
      Mockito.when(paymentServiceMock.retrievePayment(retrievePaymentDTO)).thenReturn(Pair.of(installmentDTO, organization));
      mapperMock.when(() -> PaGetPaymentMapper.installmentDto2PaGetPaymentRes(installmentDTO, organization, paGetPaymentReq.getTransferType())).thenReturn(paGetPaymentRes);

      // when
      PaGetPaymentRes response = paForNodeEndpoint.paGetPayment(paGetPaymentReq);

      // verify
      Assertions.assertEquals(paGetPaymentRes, response);
      Mockito.verify(paymentServiceMock, Mockito.times(1)).retrievePayment(retrievePaymentDTO);
      mapperMock.verify(() -> PaGetPaymentMapper.paPaGetPaymentReq2RetrievePaymentDTO(paGetPaymentReq), Mockito.times(1));
      mapperMock.verify(() -> PaGetPaymentMapper.installmentDto2PaGetPaymentRes(installmentDTO, organization, paGetPaymentReq.getTransferType()), Mockito.times(1));
    }
  }

  @Test
  void givenInvalidPaGetPaymentReqWhenPaGetPaymentThenFault() {
    try (MockedStatic<PaGetPaymentMapper> mapperMock = Mockito.mockStatic(PaGetPaymentMapper.class)) {
      // given
      PaGetPaymentReq paGetPaymentReq = podamFactory.manufacturePojo(PaGetPaymentReq.class);
      RetrievePaymentDTO retrievePaymentDTO = podamFactory.manufacturePojo(RetrievePaymentDTO.class);

      mapperMock.when(() -> PaGetPaymentMapper.paPaGetPaymentReq2RetrievePaymentDTO(paGetPaymentReq)).thenReturn(retrievePaymentDTO);
      Mockito.when(paymentServiceMock.retrievePayment(retrievePaymentDTO)).thenThrow(new SynchronousPaymentException(PagoPaNodeFaults.PAA_SYSTEM_ERROR.code(), "EMITTER"));

      // when
      PaGetPaymentRes response = paForNodeEndpoint.paGetPayment(paGetPaymentReq);

      // verify
      Assertions.assertNotNull(response);
      Assertions.assertNotNull(response.getFault());
      Assertions.assertEquals(PagoPaNodeFaults.PAA_SYSTEM_ERROR.code(), response.getFault().getFaultCode());
      Assertions.assertEquals("EMITTER", response.getFault().getId());
      Mockito.verify(paymentServiceMock, Mockito.times(1)).retrievePayment(retrievePaymentDTO);
      mapperMock.verify(() -> PaGetPaymentMapper.paPaGetPaymentReq2RetrievePaymentDTO(paGetPaymentReq), Mockito.times(1));
    }
  }

  @Test
  void givenInvalidPaGetPaymentReqWithMarcadabolloWhenPaGetPaymentThenFault() {
    try (MockedStatic<PaGetPaymentMapper> mapperMock = Mockito.mockStatic(PaGetPaymentMapper.class)) {
      // given
      PaGetPaymentReq paGetPaymentReq = podamFactory.manufacturePojo(PaGetPaymentReq.class);
      RetrievePaymentDTO retrievePaymentDTO = podamFactory.manufacturePojo(RetrievePaymentDTO.class);
      InstallmentDTO installmentDTO = podamFactory.manufacturePojo(InstallmentDTO.class);
      Organization organization = podamFactory.manufacturePojo(Organization.class);
      PaGetPaymentRes paGetPaymentRes = podamFactory.manufacturePojo(PaGetPaymentRes.class);
      //set specific values for this test
      installmentDTO.getTransfers().getFirst().setIban(null);
      installmentDTO.getTransfers().getFirst().setPostalIban(null);

      mapperMock.when(() -> PaGetPaymentMapper.paPaGetPaymentReq2RetrievePaymentDTO(paGetPaymentReq)).thenReturn(retrievePaymentDTO);
      Mockito.when(paymentServiceMock.retrievePayment(retrievePaymentDTO)).thenReturn(Pair.of(installmentDTO, organization));

      // when
      PaGetPaymentRes response = paForNodeEndpoint.paGetPayment(paGetPaymentReq);

      // verify
      Assertions.assertNotNull(response);
      Assertions.assertNotNull(response.getFault());
      Assertions.assertEquals(PagoPaNodeFaults.PAA_SEMANTICA.code(), response.getFault().getFaultCode());
      Assertions.assertEquals(retrievePaymentDTO.getIdPA(), response.getFault().getId());
      Mockito.verify(paymentServiceMock, Mockito.times(1)).retrievePayment(retrievePaymentDTO);
      mapperMock.verify(() -> PaGetPaymentMapper.paPaGetPaymentReq2RetrievePaymentDTO(paGetPaymentReq), Mockito.times(1));
    }
  }

  //endregion

  //region paGetPaymentV2

  @Test
  void givenValidPaGetPaymentV2RequestNonPostalWhenPaGetPaymentV2ThenOk() {
    try (MockedStatic<PaGetPaymentMapper> mapperMock = Mockito.mockStatic(PaGetPaymentMapper.class)) {
      // given
      PaGetPaymentV2Request paGetPaymentV2Request = podamFactory.manufacturePojo(PaGetPaymentV2Request.class);
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

      mapperMock.when(() -> PaGetPaymentMapper.paPaGetPaymentV2Request2RetrievePaymentDTO(paGetPaymentV2Request)).thenReturn(retrievePaymentDTO);
      Mockito.when(paymentServiceMock.retrievePayment(retrievePaymentDTO)).thenReturn(Pair.of(installmentDTO, organization));
      mapperMock.when(() -> PaGetPaymentMapper.installmentDto2PaGetPaymentV2Response(installmentDTO, organization, paGetPaymentV2Request.getTransferType())).thenReturn(paGetPaymentV2Response);

      // when
      PaGetPaymentV2Response response = paForNodeEndpoint.paGetPaymentV2(paGetPaymentV2Request);

      // verify
      Assertions.assertEquals(paGetPaymentV2Response, response);
      Mockito.verify(paymentServiceMock, Mockito.times(1)).retrievePayment(retrievePaymentDTO);
      mapperMock.verify(() -> PaGetPaymentMapper.paPaGetPaymentV2Request2RetrievePaymentDTO(paGetPaymentV2Request), Mockito.times(1));
      mapperMock.verify(() -> PaGetPaymentMapper.installmentDto2PaGetPaymentV2Response(installmentDTO, organization, paGetPaymentV2Request.getTransferType()), Mockito.times(1));
    }
  }

  //endregion

}
