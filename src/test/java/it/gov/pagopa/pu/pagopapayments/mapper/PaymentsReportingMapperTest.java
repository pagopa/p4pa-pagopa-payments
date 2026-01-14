package it.gov.pagopa.pu.pagopapayments.mapper;

import gov.telematici.pagamenti.ws.TipoIdRendicontazione;
import it.gov.pagopa.pu.pagopapayments.dto.generated.PaymentsReportingIdDTO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

@ExtendWith(MockitoExtension.class)
class PaymentsReportingMapperTest {

  @Test
  void givenValidTipoIdRendicontazioneWhenMapThenReturnReportingIdDTO() throws DatatypeConfigurationException {
    // given
    TipoIdRendicontazione tipoIdRendicontazione = new TipoIdRendicontazione();
    tipoIdRendicontazione.setIdentificativoFlusso("flow1");
    XMLGregorianCalendar xmlGregorianCalendar = DatatypeFactory.newInstance()
      .newXMLGregorianCalendar("2025-04-11T11:16:13");
    tipoIdRendicontazione.setDataOraFlusso(xmlGregorianCalendar);

    // when
    PaymentsReportingIdDTO result = PaymentsReportingMapper.mapIdDto(tipoIdRendicontazione);

    // then
    Assertions.assertNotNull(result);
    Assertions.assertEquals("flow1", result.getPagopaPaymentsReportingId());
    Assertions.assertNotNull(result.getFlowDateTime());
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
}
