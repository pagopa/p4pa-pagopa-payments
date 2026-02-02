package it.gov.pagopa.pu.pagopapayments.mapper;

import gov.telematici.pagamenti.ws.TipoIdRendicontazione;
import it.gov.digitpa.schemas._2011.pagamenti.FlussoRiversamento;
import it.gov.pagopa.nodo.fdrorganization.dto.generated.*;
import it.gov.pagopa.pu.organization.dto.generated.Broker;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.pagopapayments.dto.BrokerForNodoPaDTO;
import it.gov.pagopa.pu.pagopapayments.dto.PaPaymentReportingDTO;
import it.gov.pagopa.pu.pagopapayments.dto.generated.PaymentsReportingIdDTO;
import it.gov.pagopa.pu.pagopapayments.service.JAXBTransformService;
import it.gov.pagopa.pu.pagopapayments.util.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static it.gov.pagopa.pu.pagopapayments.util.Constants.ZONEID;

@ExtendWith(MockitoExtension.class)
class PaymentsReportingMapperTest {

  public static final String FLOW_ID = "flow1";

  public static final String FLOW_DATE_STRING = "2025-04-11T11:16:13";
  public static final OffsetDateTime FLOW_DATE =
    LocalDateTime.parse(FLOW_DATE_STRING, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
      .atZone(ZONEID).toOffsetDateTime();

  @Mock
  private JAXBTransformService jaxbTransformService;

  @InjectMocks
  private PaymentsReportingMapper paymentsReportingMapper;

  @Test
  void givenValidFlowByPSPListWhenMapThenReturnReportingIdDTO() {
    // given
    FlowByPSP flowByPSP = new FlowByPSP();
    flowByPSP.setFdr(FLOW_ID);
    flowByPSP.setFlowDate(FLOW_DATE);
    flowByPSP.setRevision(1L);
    flowByPSP.setPspId("pspId");
    List<FlowByPSP> flowByPSPList = new ArrayList<>();
    flowByPSPList.add(flowByPSP);

    // when
    List<PaymentsReportingIdDTO> actualResult = paymentsReportingMapper.mapToIdDtoList(flowByPSPList);

    // then
    Assertions.assertNotNull(actualResult);
    Assertions.assertFalse(actualResult.isEmpty());
    Assertions.assertEquals(1, actualResult.size());
    Assertions.assertNotNull(actualResult.getFirst());
    TestUtils.checkNotNullFields(actualResult.getFirst());
    Assertions.assertEquals(FLOW_ID, actualResult.getFirst().getPagopaPaymentsReportingId());
    Assertions.assertEquals(FLOW_DATE, actualResult.getFirst().getFlowDateTime());
    Assertions.assertEquals(1L, Optional.ofNullable(actualResult.getFirst().getRevision()).map(Integer::longValue).orElse(0L));
  }

  @Test
  void givenValidPaymentsReportingIdTypeWhenMapThenReturnReportingIdDTO() throws DatatypeConfigurationException {
    // given
    TipoIdRendicontazione tipoIdRendicontazione = new TipoIdRendicontazione();
    tipoIdRendicontazione.setIdentificativoFlusso(FLOW_ID);
    XMLGregorianCalendar xmlGregorianCalendar = DatatypeFactory.newInstance()
      .newXMLGregorianCalendar(FLOW_DATE_STRING);
    tipoIdRendicontazione.setDataOraFlusso(xmlGregorianCalendar);

    // when
    PaymentsReportingIdDTO actualResult = paymentsReportingMapper.mapIdDto(tipoIdRendicontazione);

    // then
    Assertions.assertNotNull(actualResult);
    TestUtils.checkNotNullFields(actualResult, "revision", "pspId");
    Assertions.assertEquals(FLOW_ID, actualResult.getPagopaPaymentsReportingId());
    Assertions.assertNotNull(actualResult.getFlowDateTime());
  }

  @ParameterizedTest
  @MethodSource("provideFlowByPSPWithNullFieldScenarios")
  void givenFlowByPSPListWithNullFieldsWhenMapThenReturnReportingIdDTOWithNullFields(
    String flowId,
    OffsetDateTime flowDate,
    Long revision
  ) {
    // given
    FlowByPSP flowByPSP = new FlowByPSP();
    flowByPSP.setFdr(flowId);
    flowByPSP.setFlowDate(flowDate);
    flowByPSP.setRevision(revision);
    List<FlowByPSP> flowByPSPList = new ArrayList<>();
    flowByPSPList.add(flowByPSP);

    // when
    List<PaymentsReportingIdDTO> actualResult = paymentsReportingMapper.mapToIdDtoList(flowByPSPList);

    // then
    Assertions.assertNotNull(actualResult);
    Assertions.assertFalse(actualResult.isEmpty());
    Assertions.assertEquals(1, actualResult.size());
    Assertions.assertEquals(flowId, actualResult.getFirst().getPagopaPaymentsReportingId());
    Assertions.assertEquals(flowDate, actualResult.getFirst().getFlowDateTime());
    Assertions.assertEquals(
      Optional.ofNullable(revision).map(Long::intValue).orElse(null),
      actualResult.getFirst().getRevision()
    );
  }

  private static Stream<Arguments> provideFlowByPSPWithNullFieldScenarios() {
    return Stream.of(
      Arguments.of(null, FLOW_DATE, 1L),
      Arguments.of(FLOW_ID, null, 1L),
      Arguments.of(FLOW_ID, FLOW_DATE, null),
      Arguments.of(null, null, 1L),
      Arguments.of(FLOW_ID, null, null),
      Arguments.of(null, FLOW_DATE, null),
      Arguments.of(null, null, null)
    );
  }

  @Test
  void givenPaymentsReportingIdTypeWithNullFieldsWhenMapThenReturnReportingIdDTOWithNullFields() {
    // given
    TipoIdRendicontazione tipoIdRendicontazione = new TipoIdRendicontazione();

    // when
    PaymentsReportingIdDTO result = paymentsReportingMapper.mapIdDto(tipoIdRendicontazione);

    // then
    Assertions.assertNotNull(result);
    Assertions.assertNull(result.getPagopaPaymentsReportingId());
    Assertions.assertNull(result.getFlowDateTime());
  }

  @Test
  void givenPaymentsReportingIdTypeNullWhenMapThenReturnReportingIdDTOWithNullFields() {
    // given & when
    PaymentsReportingIdDTO result = paymentsReportingMapper.mapIdDto(null);

    // then
    Assertions.assertNull(result);
  }

  @Test
  void givenInvalidFlowByPSPListWhenMapThenReturnNull() {
    // given & when
    List<FlowByPSP> flowByPSPListWithNullElement = new ArrayList<>();
    flowByPSPListWithNullElement.add(null);
    List<PaymentsReportingIdDTO> actualResult =
      paymentsReportingMapper.mapToIdDtoList(flowByPSPListWithNullElement);

    // then
    Assertions.assertNotNull(actualResult);
    Assertions.assertFalse(actualResult.isEmpty());
    Assertions.assertEquals(1, actualResult.size());
    Assertions.assertNull(actualResult.getFirst());
  }

  @ParameterizedTest
  @MethodSource("provideFlowByPSPScenarios")
  void givenInvalidFlowByPSPListWhenMapThenReturnReportingIdDTOEmptyList(
    List<FlowByPSP> flowByPSPList
  ) {
    // given & when
    List<PaymentsReportingIdDTO> actualResult = paymentsReportingMapper.mapToIdDtoList(flowByPSPList);

    // then
    Assertions.assertNotNull(actualResult);
    Assertions.assertTrue(actualResult.isEmpty());
  }

  private static Stream<Arguments> provideFlowByPSPScenarios() {
    return Stream.of(
      null,
      Arguments.of(new ArrayList<>())
    );
  }

  @Test
  void givenValidFilenameWhenIsFilenameInvalidReturnFalse() {
    // given & when
    boolean actualResult = paymentsReportingMapper.isFilenameInvalid(
      "paymentsReportingId_filename.xml",
      "paymentsReportingId"
    );

    // then
    Assertions.assertFalse(actualResult);
  }

  @ParameterizedTest
  @MethodSource("provideInvalidFilenameScenarios")
  void givenInvalidFilenameWhenIsFilenameInvalidReturnTrue(
    String filename, String paymentsReportingId
  ) {
    // given & when
    boolean actualResult = paymentsReportingMapper.isFilenameInvalid(
      filename, paymentsReportingId
    );

    // then
    Assertions.assertTrue(actualResult);
  }

  private static Stream<Arguments> provideInvalidFilenameScenarios() {
    return Stream.of(
      Arguments.of("filename.xml", "paymentsReportingId"),
      Arguments.of("paymentsReportingId_filename.csv", "")
    );
  }

  @Test
  void givenValidInputWhenMapPaymentsReportingReturnPaPaymentReportingDTO() {
    // given
    BrokerForNodoPaDTO brokerForNodoPaDTO = new BrokerForNodoPaDTO();
    Broker broker = new Broker();
    broker.setBrokerFiscalCode("brokerFiscalCode");
    broker.setStationId("stationId");
    brokerForNodoPaDTO.setBroker(broker);
    Organization organization = new Organization();
    organization.setOrgFiscalCode("orgFiscalCode");
    brokerForNodoPaDTO.setOrganization(organization);

    FlussoRiversamento paymentReporting = new FlussoRiversamento();

    Mockito.when(
      jaxbTransformService.marshalling(
        Mockito.any(FlussoRiversamento.class),
        Mockito.eq(FlussoRiversamento.class)
      )
    ).thenReturn("xmlByteString");

    // when
    PaPaymentReportingDTO actualResult = paymentsReportingMapper.mapPaPaymentsReportingDTO(
      brokerForNodoPaDTO, paymentReporting
    );

    // then
    Assertions.assertNotNull(actualResult);
    Assertions.assertEquals("orgFiscalCode", actualResult.getIdPA());
    Assertions.assertEquals("brokerFiscalCode", actualResult.getIdBrokerPA());
    Assertions.assertEquals("stationId", actualResult.getIdStation());
    Assertions.assertEquals("orgFiscalCode", actualResult.getFiscalCode());
    Assertions.assertArrayEquals("xmlByteString".getBytes(StandardCharsets.UTF_8), actualResult.getPaymentReportingBytes());
  }

  @ParameterizedTest
  @EnumSource(Payment.PayStatusEnum.class)
  void givenValidInputWhenMapSingleFlowResponseAndPaymentListReturnPaymentsReporting(Payment.PayStatusEnum payStatusEnum) {
    // given
    Sender sender = getSender(SenderTypeEnum.ABI_CODE);
    Receiver receiver = getReceiver();
    SingleFlowResponse singleFlowResponse = getSingleFlowResponse(sender, receiver);
    List<Payment> paymentList = List.of(getPayment(payStatusEnum));

    // when
    FlussoRiversamento actualResult = paymentsReportingMapper.mapPaymentsReporting(
      singleFlowResponse, paymentList
    );

    // then
    Assertions.assertNotNull(actualResult);
    TestUtils.checkNotNullFields(actualResult, "versioneOggetto");
    Assertions.assertTrue(TestUtils.checkDecimalPlaces(actualResult.getImportoTotalePagamenti(),2));
    TestUtils.checkNotNullFields(actualResult.getIstitutoMittente());
    TestUtils.checkNotNullFields(actualResult.getIstitutoRicevente());
    actualResult.getDatiSingoliPagamentis().forEach(TestUtils::checkNotNullFields);
    actualResult.getDatiSingoliPagamentis().forEach(p -> Assertions.assertTrue(TestUtils.checkDecimalPlaces(p.getSingoloImportoPagato(),2)));
  }

  @ParameterizedTest
  @EnumSource(SenderTypeEnum.class)
  void givenValidInputWhenMapSingleFlowResponseAndPaymentListReturnPaymentsReporting(SenderTypeEnum senderTypeEnum) {
    // given
    Sender sender = getSender(senderTypeEnum);
    Receiver receiver = getReceiver();
    SingleFlowResponse singleFlowResponse = getSingleFlowResponse(sender, receiver);
    List<Payment> paymentList = List.of(getPayment(Payment.PayStatusEnum.EXECUTED));

    // when
    FlussoRiversamento actualResult = paymentsReportingMapper.mapPaymentsReporting(
      singleFlowResponse, paymentList
    );

    // then
    Assertions.assertNotNull(actualResult);
    TestUtils.checkNotNullFields(actualResult, "versioneOggetto");
    Assertions.assertTrue(TestUtils.checkDecimalPlaces(actualResult.getImportoTotalePagamenti(),2));
    TestUtils.checkNotNullFields(actualResult.getIstitutoMittente());
    TestUtils.checkNotNullFields(actualResult.getIstitutoRicevente());
    actualResult.getDatiSingoliPagamentis().forEach(TestUtils::checkNotNullFields);
    actualResult.getDatiSingoliPagamentis().forEach(p -> Assertions.assertTrue(TestUtils.checkDecimalPlaces(p.getSingoloImportoPagato(),2)));
  }

  @Test
  void givenNullSingleFlowWhenMapSingleFlowResponseAndPaymentListReturnNullPaymentsReporting() {
    // given
    List<Payment> paymentList = List.of(getPayment(Payment.PayStatusEnum.EXECUTED));

    // when
    FlussoRiversamento actualResult = paymentsReportingMapper.mapPaymentsReporting(
      null, paymentList
    );

    // then
    Assertions.assertNull(actualResult);
  }

  @ParameterizedTest
  @MethodSource("provideInvalidPaymentList")
  void givenInvalidPaymentListWhenMapSingleFlowResponseAndPaymentListReturnPaymentsReporting(List<Payment> paymentList) {
    // given
    SingleFlowResponse singleFlowResponse = getSingleFlowResponse(null, null);

    // when
    FlussoRiversamento actualResult = paymentsReportingMapper.mapPaymentsReporting(
      singleFlowResponse, paymentList
    );

    // then
    Assertions.assertNotNull(actualResult);
    TestUtils.checkNotNullFields(actualResult, "istitutoMittente", "istitutoRicevente", "versioneOggetto");
    Assertions.assertNull(actualResult.getIstitutoMittente());
    Assertions.assertNull(actualResult.getIstitutoRicevente());
    Assertions.assertEquals(0, actualResult.getDatiSingoliPagamentis().size());
  }

  private static Stream<List<Payment>> provideInvalidPaymentList() {
    return Stream.of(
      new ArrayList<>(),
      null
    );
  }

  private static SingleFlowResponse getSingleFlowResponse(Sender sender, Receiver receiver) {
    SingleFlowResponse singleFlowResponse = new SingleFlowResponse();
    singleFlowResponse.setFdr("fdrId");
    singleFlowResponse.setRevision(1L);
    singleFlowResponse.setFdrDate(OffsetDateTime.now());
    singleFlowResponse.setRegulation("regulation");
    singleFlowResponse.setRegulationDate(OffsetDateTime.now());
    singleFlowResponse.setSender(sender);
    singleFlowResponse.setBicCodePouringBank("bicCodePouringBank");
    singleFlowResponse.setReceiver(receiver);
    singleFlowResponse.setTotPayments(1L);
    singleFlowResponse.setSumPayments(20.0);
    return singleFlowResponse;
  }

  private static Payment getPayment(Payment.PayStatusEnum payStatusEnum) {
    Payment payment = new Payment();
    payment.setIuv("iuv");
    payment.setIur("iur");
    payment.setIndex(1L);
    payment.setPay(20.0);
    payment.setPayDate(OffsetDateTime.now());
    payment.setPayStatus(payStatusEnum);
    return payment;
  }

  private static Receiver getReceiver() {
    Receiver receiver = new Receiver();
    receiver.setOrganizationName("orgName");
    receiver.setId("orgId");
    return receiver;
  }

  private static Sender getSender(SenderTypeEnum senderTypeEnum) {
    Sender sender = new Sender();
    sender.setPspName("pspName");
    sender.setId("pspId");
    sender.setType(senderTypeEnum);
    return sender;
  }

}
