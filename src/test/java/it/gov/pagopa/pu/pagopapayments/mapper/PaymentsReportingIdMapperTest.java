package it.gov.pagopa.pu.pagopapayments.mapper;

import gov.telematici.pagamenti.ws.TipoIdRendicontazione;
import it.gov.pagopa.pu.pagopapayments.dto.generated.PaymentsReportingIdDTO;
import it.gov.pagopa.pu.pagopapayments.util.ConversionUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;

@ExtendWith(MockitoExtension.class)
class PaymentsReportingIdMapperTest {

  @Test
  void givenValidTipoIdRendicontazioneWhenMapThenReturnReportingIdDTO() {
    // given
    TipoIdRendicontazione tipoIdRendicontazione = new TipoIdRendicontazione();
    tipoIdRendicontazione.setIdentificativoFlusso("flow1");
    tipoIdRendicontazione.setDataOraFlusso(ConversionUtils.toXMLGregorianCalendar(OffsetDateTime.now()));

    // when
    PaymentsReportingIdDTO result = PaymentsReportingIdMapper.map(tipoIdRendicontazione);

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
    PaymentsReportingIdDTO result = PaymentsReportingIdMapper.map(tipoIdRendicontazione);

    // then
    Assertions.assertNotNull(result);
    Assertions.assertNull(result.getPagopaPaymentsReportingId());
    Assertions.assertNull(result.getFlowDateTime());
  }

  @Test
  void givenTipoIdRendicontazioneNullWhenMapThenReturnReportingIdDTOWithNullFields() {
    // given & when
    PaymentsReportingIdDTO result = PaymentsReportingIdMapper.map(null);

    // then
    Assertions.assertNull(result);
  }
}
