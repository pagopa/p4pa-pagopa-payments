package it.gov.pagopa.pu.pagopapayments.mapper;

import gov.telematici.pagamenti.ws.TipoIdRendicontazione;
import it.gov.pagopa.nodo.fdrorganization.dto.generated.FlowByPSP;
import it.gov.pagopa.pu.organization.dto.generated.Broker;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.pagopapayments.dto.BrokerForNodoPaDTO;
import it.gov.pagopa.pu.pagopapayments.dto.PaPaymentReportingDTO;
import it.gov.pagopa.pu.pagopapayments.dto.generated.PaymentsReportingIdDTO;
import it.gov.pagopa.pu.pagopapayments.util.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
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

  @Test
  void givenValidFlowByPSPListWhenMapThenReturnReportingIdDTO() {
    // given
    FlowByPSP flowByPSP = new FlowByPSP();
    flowByPSP.setFdr(FLOW_ID);
    flowByPSP.setFlowDate(FLOW_DATE);
    flowByPSP.setRevision(1L);
    List<FlowByPSP> flowByPSPList = new ArrayList<>();
    flowByPSPList.add(flowByPSP);

    // when
    List<PaymentsReportingIdDTO> actualResult = PaymentsReportingMapper.mapToIdDtoList(flowByPSPList);

    // then
    Assertions.assertNotNull(actualResult);
    Assertions.assertFalse(actualResult.isEmpty());
    Assertions.assertEquals(1, actualResult.size());
    TestUtils.checkNotNullFields(actualResult.getFirst());
    Assertions.assertEquals(FLOW_ID, actualResult.getFirst().getPagopaPaymentsReportingId());
    Assertions.assertEquals(FLOW_DATE, actualResult.getFirst().getFlowDateTime());
    Assertions.assertEquals(1L, actualResult.getFirst().getRevision().longValue());
  }

  @Test
  void givenValidTipoIdRendicontazioneWhenMapThenReturnReportingIdDTO() throws DatatypeConfigurationException {
    // given
    TipoIdRendicontazione tipoIdRendicontazione = new TipoIdRendicontazione();
    tipoIdRendicontazione.setIdentificativoFlusso(FLOW_ID);
    XMLGregorianCalendar xmlGregorianCalendar = DatatypeFactory.newInstance()
      .newXMLGregorianCalendar(FLOW_DATE_STRING);
    tipoIdRendicontazione.setDataOraFlusso(xmlGregorianCalendar);

    // when
    PaymentsReportingIdDTO actualResult = PaymentsReportingMapper.mapIdDto(tipoIdRendicontazione);

    // then
    Assertions.assertNotNull(actualResult);
    TestUtils.checkNotNullFields(actualResult, "revision");
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
    List<PaymentsReportingIdDTO> actualResult = PaymentsReportingMapper.mapToIdDtoList(flowByPSPList);

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
  void givenTipoIdRendicontazioneWithNullFieldsWhenMapThenReturnReportingIdDTOWithNullFields() {
    // given
    TipoIdRendicontazione tipoIdRendicontazione = new TipoIdRendicontazione();

    // when
    PaymentsReportingIdDTO result = PaymentsReportingMapper.mapIdDto(tipoIdRendicontazione);

    // then
    Assertions.assertNotNull(result);
    Assertions.assertNull(result.getPagopaPaymentsReportingId());
    Assertions.assertNull(result.getFlowDateTime());
  }

  @Test
  void givenTipoIdRendicontazioneNullWhenMapThenReturnReportingIdDTOWithNullFields() {
    // given
    TipoIdRendicontazione tipoIdRendicontazione = null;
    // when
    PaymentsReportingIdDTO result = PaymentsReportingMapper.mapIdDto(tipoIdRendicontazione);

    // then
    Assertions.assertNull(result);
  }

  @Test
  void givenInvalidFlowByPSPListWhenMapThenReturnNull() {
    // given & when
    List<FlowByPSP> flowByPSPListWithNullElement = new ArrayList<>();
    flowByPSPListWithNullElement.add(null);
    List<PaymentsReportingIdDTO> actualResult =
      PaymentsReportingMapper.mapToIdDtoList(flowByPSPListWithNullElement);

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
    List<PaymentsReportingIdDTO> actualResult = PaymentsReportingMapper.mapToIdDtoList(flowByPSPList);

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
    boolean actualResult = PaymentsReportingMapper.isFilenameInvalid(
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
    boolean actualResult = PaymentsReportingMapper.isFilenameInvalid(
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

    // when
    PaPaymentReportingDTO actualResult = PaymentsReportingMapper.mapPaymentsReporting(
      brokerForNodoPaDTO
    );


    // then
    Assertions.assertNotNull(actualResult);
    Assertions.assertEquals("orgFiscalCode", actualResult.getIdPA());
    Assertions.assertEquals("brokerFiscalCode", actualResult.getIdBrokerPA());
    Assertions.assertEquals("stationId", actualResult.getIdStation());
    Assertions.assertEquals("orgFiscalCode", actualResult.getFiscalCode());
    Assertions.assertEquals(0, actualResult.getPaymentReportingBytes().length);
  }

}
