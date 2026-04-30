package it.gov.pagopa.pu.pagopapayments.mapper;

import it.gov.pagopa.pagopa_api.pa.pafornode.*;
import it.gov.pagopa.pagopa_api.xsd.common_types.v1_0.CtMapEntry;
import it.gov.pagopa.pagopa_api.xsd.common_types.v1_0.CtMetadata;
import it.gov.pagopa.pagopa_api.xsd.common_types.v1_0.CtRichiestaMarcaDaBollo;
import it.gov.pagopa.pagopa_api.xsd.common_types.v1_0.StOutcome;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.TransferDTO;
import it.gov.pagopa.pu.organization.dto.generated.Broker;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.pagopapayments.dto.RetrievePaymentDTO;
import it.gov.pagopa.pu.pagopapayments.util.ConversionUtils;
import it.gov.pagopa.pu.pagopapayments.util.DebtPositionUtils;
import it.gov.pagopa.pu.pagopapayments.util.Utilities;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;


public class PaGetPaymentMapper {

  private PaGetPaymentMapper() {
  }

  public static RetrievePaymentDTO paPaGetPaymentV2Request2RetrievePaymentDTO(PaGetPaymentV2Request request) {
    return RetrievePaymentDTO.builder()
      .idPA(request.getIdPA())
      .idBrokerPA(request.getIdBrokerPA())
      .idStation(request.getIdStation())
      .fiscalCode(request.getQrCode().getFiscalCode())
      .noticeNumber(request.getQrCode().getNoticeNumber())
      .postalTransfer(request.getTransferType().equals(StTransferType.POSTAL))
      .build();
  }

  public static PaGetPaymentV2Response installmentDto2PaGetPaymentV2Response (InstallmentDTO installmentDTO, Organization organization, Broker broker, StTransferType transferType) {
    CtPaymentPAV2 payment = new CtPaymentPAV2();
    payment.setCreditorReferenceId(installmentDTO.getIuv());
    payment.setDueDate(ConversionUtils.toXMLGregorianCalendar(ConversionUtils.localDate2RomeMaxTime(installmentDTO.getDueDate())));
    payment.setRetentionDate(ConversionUtils.toXMLGregorianCalendar(OffsetDateTime.now().plusMinutes(15))); //the data validity of this response: set to 15 minutes
    payment.setLastPayment(true);
    payment.setDescription(Utilities.truncateRemittanceInformation(installmentDTO.getRemittanceInformation()));

    List<TransferDTO> transfers = Optional.ofNullable(installmentDTO.getTransfers()).orElse(List.of());

    Pair<String, String> orgInfo  = DebtPositionUtils.resolveOrganizationInfo(organization, broker, transfers);
    payment.setCompanyName(orgInfo.getRight());
    payment.setOfficeName(null);
    payment.setPaymentAmount(ConversionUtils.centsAmountToBigDecimalEuroAmount(installmentDTO.getAmountCents()));

    CtSubject debtor = new CtSubject();
    CtEntityUniqueIdentifier debtorId = new CtEntityUniqueIdentifier();
    debtorId.setEntityUniqueIdentifierType(StEntityUniqueIdentifierType.valueOf(installmentDTO.getDebtor().getEntityType().name()));
    debtorId.setEntityUniqueIdentifierValue(installmentDTO.getDebtor().getFiscalCode());
    debtor.setUniqueIdentifier(debtorId);
    debtor.setFullName(Utilities.truncateFullName(installmentDTO.getDebtor().getFullName()));
    debtor.setCountry(installmentDTO.getDebtor().getNation());
    debtor.setStateProvinceRegion(installmentDTO.getDebtor().getProvince());
    debtor.setCity(installmentDTO.getDebtor().getLocation());
    debtor.setPostalCode(installmentDTO.getDebtor().getPostalCode());
    debtor.setStreetName(installmentDTO.getDebtor().getAddress());
    debtor.setCivicNumber(installmentDTO.getDebtor().getCivic());
    debtor.setEMail(installmentDTO.getDebtor().getEmail());
    payment.setDebtor(debtor);

    CtTransferListPAV2 transferList = new CtTransferListPAV2();

    transfers.stream()
      .filter(t -> t.getAmountCents() > 0)
      .forEach(transferDTO -> {
      CtTransferPAV2 transfer = new CtTransferPAV2();
      transfer.setIdTransfer(transferDTO.getTransferIndex());
      transfer.setFiscalCodePA(transferDTO.getOrgFiscalCode());
      transfer.setCompanyName(transferDTO.getOrgName());
      transfer.setTransferAmount(ConversionUtils.centsAmountToBigDecimalEuroAmount(transferDTO.getAmountCents()));
      transfer.setTransferCategory(transferDTO.getCategory());
      transfer.setRemittanceInformation(Utilities.truncateRemittanceInformation(transferDTO.getRemittanceInformation()));

      if(transferDTO.getStampHashDocument() != null) {
        CtRichiestaMarcaDaBollo richiestaMarcaDaBollo = new CtRichiestaMarcaDaBollo();
        richiestaMarcaDaBollo.setTipoBollo(transferDTO.getStampType());
        richiestaMarcaDaBollo.setHashDocumento(transferDTO.getStampHashDocument().getBytes(StandardCharsets.UTF_8));
        richiestaMarcaDaBollo.setProvinciaResidenza(transferDTO.getStampProvincialResidence());
        transfer.setRichiestaMarcaDaBollo(richiestaMarcaDaBollo);
      }

      if(StTransferType.POSTAL.equals(transferType)) {
        transfer.setIBAN(StringUtils.isNotBlank(transferDTO.getPostalIban()) ? transferDTO.getPostalIban() : transferDTO.getIban());
      } else {
        transfer.setIBAN(transferDTO.getIban());
        if(StringUtils.isNotBlank(transferDTO.getPostalIban())){
          createIbanAppoggioMetadata(transfer, transferDTO.getPostalIban());
        }
      }

      transferList.getTransfers().add(transfer);
    });

    payment.setTransferList(transferList);

    if(StringUtils.isNotBlank(installmentDTO.getLegacyPaymentMetadata())) {
      CtMetadata metadata = new CtMetadata();
      CtMapEntry entry = new CtMapEntry();
      entry.setKey("datiSpecificiRiscossione");
      entry.setValue(installmentDTO.getLegacyPaymentMetadata());
      metadata.getMapEntries().add(entry);
      payment.setMetadata(metadata);
    }

    PaGetPaymentV2Response response = new PaGetPaymentV2Response();
    response.setData(payment);
    response.setOutcome(StOutcome.OK);
    return response;
  }

  private static void createIbanAppoggioMetadata(CtTransferPAV2 transferPA, String value) {
    CtMapEntry mapEntry = new CtMapEntry();
    mapEntry.setKey("IBANAPPOGGIO");
    mapEntry.setValue(value);
    CtMetadata ctMetadata = Optional.ofNullable(transferPA.getMetadata()).orElse(new CtMetadata());
    ctMetadata.getMapEntries().add(mapEntry);
    transferPA.setMetadata(ctMetadata);
  }
}
