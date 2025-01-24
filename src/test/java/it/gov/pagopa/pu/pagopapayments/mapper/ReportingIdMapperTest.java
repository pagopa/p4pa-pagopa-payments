package it.gov.pagopa.pu.pagopapayments.mapper;

import gov.telematici.pagamenti.ws.TipoIdRendicontazione;
import it.gov.pagopa.pu.pagopapayments.dto.generated.ReportingIdDTO;
import it.gov.pagopa.pu.pagopapayments.util.ConversionUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;

@ExtendWith(MockitoExtension.class)
class ReportingIdMapperTest {

  @Test
  void givenValidTipoIdRendicontazioneWhenMapThenReturnReportingIdDTO() {
    // given
    TipoIdRendicontazione tipoIdRendicontazione = new TipoIdRendicontazione();
    tipoIdRendicontazione.setIdentificativoFlusso("flow1");
    tipoIdRendicontazione.setDataOraFlusso(ConversionUtils.toXMLGregorianCalendar(OffsetDateTime.now()));

    // when
    ReportingIdDTO result = ReportingIdMapper.map(tipoIdRendicontazione);

    // then
    Assertions.assertNotNull(result);
    Assertions.assertEquals("flow1", result.getReportId());
    Assertions.assertNotNull(result.getReportDate());
  }

  @Test
  void givenTipoIdRendicontazioneWithNullFieldsWhenMapThenReturnReportingIdDTOWithNullFields() {
    // given
    TipoIdRendicontazione tipoIdRendicontazione = new TipoIdRendicontazione();

    // when
    ReportingIdDTO result = ReportingIdMapper.map(tipoIdRendicontazione);

    // then
    Assertions.assertNotNull(result);
    Assertions.assertNull(result.getReportId());
    Assertions.assertNull(result.getReportDate());
  }
}
