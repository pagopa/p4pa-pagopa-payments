package it.gov.pagopa.pu.pagopapayments.mapper;

import it.gov.pagopa.pagopa_api.pa.pafornode.CtTransferListPAReceiptV2;
import it.gov.pagopa.pagopa_api.pa.pafornode.CtTransferPAReceiptV2;
import it.gov.pagopa.pagopa_api.pa.pafornode.PaSendRTV2Request;
import it.gov.pagopa.pagopa_api.xsd.common_types.v1_0.CtMapEntry;
import it.gov.pagopa.pagopa_api.xsd.common_types.v1_0.CtMetadata;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptTransferDTO;
import it.gov.pagopa.pu.pagopapayments.dto.PaSendRtDTO;
import it.gov.pagopa.pu.pagopapayments.service.JAXBTransformService;
import it.gov.pagopa.pu.pagopapayments.util.Utilities;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.utils.Base64;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
      .transferList(mapTransfers(request.getReceipt().getTransferList()))
      .build();
  }

  private List<ReceiptTransferDTO> mapTransfers(CtTransferListPAReceiptV2 transferList) {
    return transferList.getTransfers().stream()
      .map(this::mapSingleTransfer)
      .toList();
  }

  private ReceiptTransferDTO mapSingleTransfer(CtTransferPAReceiptV2 transfer) {
    return new ReceiptTransferDTO()
      .idTransfer(transfer.getIdTransfer())
      .transferAmountCents(Utilities.bigDecimalEuroToLongCentsAmount(transfer.getTransferAmount()))
      .fiscalCodePA(transfer.getFiscalCodePA())
      .companyName(transfer.getCompanyName())
      .mbdAttachment(decodeMdbAttachment(transfer))
      .iban(transfer.getIBAN())
      .remittanceInformation(transfer.getRemittanceInformation())
      .transferCategory(transfer.getTransferCategory())
      .metadata(map(transfer.getMetadata()));
  }

  private static String decodeMdbAttachment(CtTransferPAReceiptV2 transfer) {
    if(transfer.getMBDAttachment()==null){
      return null;
    }
    return new String(Base64.decodeBase64(transfer.getMBDAttachment()), StandardCharsets.UTF_8);
  }

  private Map<String, String> map(CtMetadata metadata) {
    return metadata == null ? null : metadata.getMapEntries().stream().collect(Collectors.toUnmodifiableMap(CtMapEntry::getKey, CtMapEntry::getValue));
  }
}
