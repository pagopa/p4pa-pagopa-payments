package it.gov.pagopa.pu.pagopapayments.service;

import it.gov.pagopa.pagopa_api.pa.pafornode.PaVerifyPaymentNoticeReq;
import it.gov.pagopa.pu.pagopapayments.util.TestUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import uk.co.jemos.podam.api.PodamFactory;

import java.nio.charset.StandardCharsets;

@SpringBootTest(
  classes = {
    JAXBTransformService.class},
  webEnvironment = SpringBootTest.WebEnvironment.NONE)
class JaxbTransformServiceTest {

  @Autowired
  private JAXBTransformService jaxbTransformService;

  private final PodamFactory podamFactory;

  JaxbTransformServiceTest() {
    this.podamFactory = TestUtils.getPodamFactory();
  }

  private static final String EXPECTED_RESPONSE_TEMPLATE =
    "<%s%s>"+
      "<idPA>%s</idPA><idBrokerPA>%s</idBrokerPA><idStation>%s</idStation>"+
      "<qrCode><fiscalCode>%s</fiscalCode><noticeNumber>%s</noticeNumber></qrCode>"+
      "</%s>";
  private static final String EXPECTED_NAMESPACE = " xmlns:ns2=\"http://pagopa-api.pagopa.gov.it/pa/paForNode.xsd\"";

  //region Marshalling

  @Test
  void givenValidObjectWhenMarshallingThenOk() {
    // given
    PaVerifyPaymentNoticeReq request = podamFactory.manufacturePojo(PaVerifyPaymentNoticeReq.class);
    String rootElement = "ns2:paVerifyPaymentNoticeReq";
    String expectedResponse = EXPECTED_RESPONSE_TEMPLATE.formatted(rootElement, EXPECTED_NAMESPACE,
      request.getIdPA(), request.getIdBrokerPA(), request.getIdStation(), request.getQrCode().getFiscalCode(), request.getQrCode().getNoticeNumber(),
      rootElement);

    // when
    String response = jaxbTransformService.marshalling(request, PaVerifyPaymentNoticeReq.class);

    // then
    Assertions.assertEquals(expectedResponse, response);
  }

  @Test
  void givenValidObjectWhenMarshallingAsBytesThenOk() {
    // given
    PaVerifyPaymentNoticeReq request = podamFactory.manufacturePojo(PaVerifyPaymentNoticeReq.class);
    String rootElement = "ns2:paVerifyPaymentNoticeReq";
    byte[] expectedResponse = EXPECTED_RESPONSE_TEMPLATE.formatted(rootElement, EXPECTED_NAMESPACE,
      request.getIdPA(), request.getIdBrokerPA(), request.getIdStation(), request.getQrCode().getFiscalCode(), request.getQrCode().getNoticeNumber(),
      rootElement).getBytes(StandardCharsets.UTF_8);

    // when
    byte[] response = jaxbTransformService.marshallingAsBytes(request, PaVerifyPaymentNoticeReq.class);

    // then
    Assertions.assertArrayEquals(expectedResponse, response);
  }

  @Test
  void givenValidObjectAsBytesWithJaxbElementNameWhenMarshallingThenOk() {
    // given
    PaVerifyPaymentNoticeReq request = podamFactory.manufacturePojo(PaVerifyPaymentNoticeReq.class);
    String rootElement = "XXX";
    byte[] expectedResponse = EXPECTED_RESPONSE_TEMPLATE.formatted(rootElement, EXPECTED_NAMESPACE,
      request.getIdPA(), request.getIdBrokerPA(), request.getIdStation(), request.getQrCode().getFiscalCode(), request.getQrCode().getNoticeNumber(),
      rootElement).getBytes(StandardCharsets.UTF_8);

    // when
    byte[] response = jaxbTransformService.marshallingAsBytes(request, PaVerifyPaymentNoticeReq.class,rootElement);

    // then
    Assertions.assertEquals(new String(expectedResponse, StandardCharsets.UTF_8), new String(response, StandardCharsets.UTF_8));
    Assertions.assertArrayEquals(expectedResponse, response);
  }

  @Test
  void givenNullObjectWhenMarshallingThenOk() {
    // given

    // when
    String response = jaxbTransformService.marshalling(null, PaVerifyPaymentNoticeReq.class);

    // then
    Assertions.assertNull(response);
  }

  @Test
  void givenValidObjectWhenMarshallingNoNamespaceThenOk() {
    // given
    PaVerifyPaymentNoticeReq request = podamFactory.manufacturePojo(PaVerifyPaymentNoticeReq.class);
    String rootElement = "ns2:paVerifyPaymentNoticeReq";
    String expectedResponse = EXPECTED_RESPONSE_TEMPLATE.formatted(rootElement, "",
      request.getIdPA(), request.getIdBrokerPA(), request.getIdStation(), request.getQrCode().getFiscalCode(), request.getQrCode().getNoticeNumber(),
      rootElement);

    // when
    String response = jaxbTransformService.marshallingNoNamespace(request, PaVerifyPaymentNoticeReq.class);

    // then
    Assertions.assertEquals(expectedResponse, response);
  }

  //endregion

  //region Unmarshalling

  @Test
  void givenValidXmlWhenUnmarshallingThenOk() {
    // given
    PaVerifyPaymentNoticeReq expectedResponse = podamFactory.manufacturePojo(PaVerifyPaymentNoticeReq.class);
    String rootElement = "ns2:paVerifyPaymentNoticeReq";
    byte[] request = EXPECTED_RESPONSE_TEMPLATE.formatted(rootElement, EXPECTED_NAMESPACE,
      expectedResponse.getIdPA(), expectedResponse.getIdBrokerPA(), expectedResponse.getIdStation(),
      expectedResponse.getQrCode().getFiscalCode(), expectedResponse.getQrCode().getNoticeNumber(),
      rootElement).getBytes(StandardCharsets.UTF_8);

    // when
    PaVerifyPaymentNoticeReq response = jaxbTransformService.unmarshalling(request, PaVerifyPaymentNoticeReq.class);

    // then
    Assertions.assertTrue(EqualsBuilder.reflectionEquals(expectedResponse, response, true, null, true));
  }

  @Test
  void givenValidXmlWithXsdSchemaWhenUnmarshallingThenOk() {
    PaVerifyPaymentNoticeReq expectedResponse = podamFactory.manufacturePojo(PaVerifyPaymentNoticeReq.class);
    expectedResponse.getQrCode().setFiscalCode("01234567890");
    expectedResponse.getQrCode().setNoticeNumber("123456789012345678");
    String rootElement = "ns2:paVerifyPaymentNoticeReq";
    byte[] request = EXPECTED_RESPONSE_TEMPLATE.formatted(rootElement, EXPECTED_NAMESPACE,
      expectedResponse.getIdPA(), expectedResponse.getIdBrokerPA(), expectedResponse.getIdStation(),
      expectedResponse.getQrCode().getFiscalCode(), expectedResponse.getQrCode().getNoticeNumber(),
      rootElement).getBytes(StandardCharsets.UTF_8);

    // when
    PaVerifyPaymentNoticeReq response = jaxbTransformService.unmarshalling(request, PaVerifyPaymentNoticeReq.class,"/soap/paForNode.xsd");

    // then
    Assertions.assertTrue(EqualsBuilder.reflectionEquals(expectedResponse, response, true, null, true));
  }

  @Test
  void givenValidXmlWithInvalidXmlCharWhenUnmarshallingThenOk() {
    // given
    PaVerifyPaymentNoticeReq expectedResponse = podamFactory.manufacturePojo(PaVerifyPaymentNoticeReq.class);
    String rootElement = "ns2:paVerifyPaymentNoticeReq";
    byte[] request = EXPECTED_RESPONSE_TEMPLATE.formatted(rootElement, EXPECTED_NAMESPACE,
      "\u0000"+expectedResponse.getIdPA(), expectedResponse.getIdBrokerPA(), expectedResponse.getIdStation(),
      expectedResponse.getQrCode().getFiscalCode(), expectedResponse.getQrCode().getNoticeNumber(),
      rootElement).getBytes(StandardCharsets.UTF_8);

    // when
    PaVerifyPaymentNoticeReq response = jaxbTransformService.unmarshalling(request, PaVerifyPaymentNoticeReq.class);

    // then
    Assertions.assertTrue(EqualsBuilder.reflectionEquals(expectedResponse, response, true, null, true));
  }

  @Test
  void givenNullObjectWhenUnmarshallingThenOk() {
    // given

    // when
    PaVerifyPaymentNoticeReq response = jaxbTransformService.unmarshalling(null, PaVerifyPaymentNoticeReq.class);

    // then
    Assertions.assertNull(response);
  }

  //endregion

}
