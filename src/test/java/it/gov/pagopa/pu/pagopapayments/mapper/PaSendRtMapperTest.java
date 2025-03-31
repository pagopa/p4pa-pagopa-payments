package it.gov.pagopa.pu.pagopapayments.mapper;

import it.gov.pagopa.pagopa_api.pa.pafornode.PaSendRTV2Request;
import it.gov.pagopa.pu.pagopapayments.dto.PaSendRtDTO;
import it.gov.pagopa.pu.pagopapayments.service.JAXBTransformService;
import it.gov.pagopa.pu.pagopapayments.util.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.jemos.podam.api.PodamFactory;

@ExtendWith(MockitoExtension.class)
class PaSendRtMapperTest {

  @Mock
  private JAXBTransformService jaxbTransformService;

  @InjectMocks
  private PaSendRTMapper paSendRTMapper;

  private final PodamFactory podamFactory;

  PaSendRtMapperTest() {
    podamFactory = TestUtils.getPodamFactory();
  }

  //region mapToNewDebtPositionRequest

  @Test
  void givenValidPaSendRTV2RequestWhenPaSendRtV2Request2PaSendRtDTOThenOk() {
    //given
    PaSendRTV2Request request = podamFactory.manufacturePojo(PaSendRTV2Request.class);
    byte[] receiptBytes = podamFactory.manufacturePojo(byte[].class);
    Mockito.when(jaxbTransformService.marshallingAsBytes(request, PaSendRTV2Request.class)).thenReturn(receiptBytes);

    //when
    PaSendRtDTO response = paSendRTMapper.paSendRtV2Request2PaSendRtDTO(request);

    //verify
    Assertions.assertNotNull(response);
    Assertions.assertEquals(request.getIdPA(), response.getIdPA());
    Assertions.assertEquals(request.getIdBrokerPA(), response.getIdBrokerPA());
    Assertions.assertEquals(request.getIdStation(), response.getIdStation());
    Assertions.assertEquals(receiptBytes, response.getReceiptBytes());
    Assertions.assertEquals(request.getReceipt().getFiscalCode(), response.getFiscalCode());
    Assertions.assertEquals(request.getReceipt().getNoticeNumber(), response.getNoticeNumber());
    TestUtils.checkNotNullFields(request);
  }

  //endregion
}
