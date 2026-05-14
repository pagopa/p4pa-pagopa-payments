package it.gov.pagopa.pu.pagopapayments.connector.soap.mapper;

import gov.telematici.pagamenti.ws.NodoChiediFlussoRendicontazione;
import it.gov.pagopa.pu.pagopapayments.dto.BrokerForNodoPaDTO;
import it.gov.pagopa.pu.pagopapayments.util.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.jemos.podam.api.PodamFactory;

class NodoChiediFlussoRendicontazioneMapperTest {

  private final NodoChiediFlussoRendicontazioneMapper mapper = new NodoChiediFlussoRendicontazioneMapper();
  private final PodamFactory podamFactory = TestUtils.getPodamFactory();

  @Test
  void test(){
    // Given
    String paymentReportingId = "paymentReportingId";
    BrokerForNodoPaDTO brokerForNodoPaDTO = podamFactory.manufacturePojo(BrokerForNodoPaDTO.class);

    NodoChiediFlussoRendicontazione expectedResult = new NodoChiediFlussoRendicontazione();
    expectedResult.setIdentificativoDominio(brokerForNodoPaDTO.getOrganization().getOrgFiscalCode());
    expectedResult.setPassword("password");
    expectedResult.setIdentificativoIntermediarioPA(brokerForNodoPaDTO.getBroker().getBrokerFiscalCode());
    expectedResult.setIdentificativoStazioneIntermediarioPA(brokerForNodoPaDTO.getBroker().getDefaultStationId());
    expectedResult.setIdentificativoFlusso(paymentReportingId);

    // When
    NodoChiediFlussoRendicontazione result = mapper.createFlussoRendicontazioneRequest(brokerForNodoPaDTO, paymentReportingId);

    // Then
    Assertions.assertNotNull(result);
    TestUtils.reflectionEqualsByName(expectedResult, result);
  }
}
