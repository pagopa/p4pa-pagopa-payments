package it.gov.pagopa.pu.pagopapayments.mapper;

import it.gov.pagopa.pagopa_api.pa.pafornode.PaSendRTV2Request;
import it.gov.pagopa.pu.pagopapayments.dto.PaSendRtDTO;
import it.gov.pagopa.pu.pagopapayments.service.JAXBTransformService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PaSendRTMapper {

  private final JAXBTransformService jaxbTransformService;

  public PaSendRTMapper(JAXBTransformService jaxbTransformService) {
    this.jaxbTransformService = jaxbTransformService;
  }

  public PaSendRtDTO paSendRtV2Request2PaSendRtDTO(PaSendRTV2Request request) {

    byte[] receiptBytes = jaxbTransformService.marshallingAsBytes(request, PaSendRTV2Request.class);

    return PaSendRtDTO.builder()
      .idPA(request.getIdPA())
      .idBrokerPA(request.getIdBrokerPA())
      .idStation(request.getIdStation())
      .receiptBytes(receiptBytes)
      .fiscalCode(request.getReceipt().getFiscalCode())
      .noticeNumber(request.getReceipt().getNoticeNumber())
      .build();
  }

}
