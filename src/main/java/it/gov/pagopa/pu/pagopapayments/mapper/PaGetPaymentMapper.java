package it.gov.pagopa.pu.pagopapayments.mapper;

import it.gov.pagopa.pagopa_api.pa.pafornode.*;
import it.gov.pagopa.pagopa_api.xsd.common_types.v1_0.CtMapEntry;
import it.gov.pagopa.pagopa_api.xsd.common_types.v1_0.CtMetadata;
import it.gov.pagopa.pagopa_api.xsd.common_types.v1_0.CtRichiestaMarcaDaBollo;
import it.gov.pagopa.pagopa_api.xsd.common_types.v1_0.StOutcome;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.pagopapayments.dto.RetrievePaymentDTO;
import it.gov.pagopa.pu.pagopapayments.util.Constants;
import it.gov.pagopa.pu.pagopapayments.util.ConversionUtils;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
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

  public static PaGetPaymentV2Response installmentDto2PaGetPaymentV2Response (InstallmentDTO installmentDTO, Organization organization, StTransferType transferType) {
    CtPaymentPAV2 payment = new CtPaymentPAV2();
    payment.setCreditorReferenceId(installmentDTO.getIuv());
    payment.setDueDate(ConversionUtils.toXMLGregorianCalendar(Optional.ofNullable(installmentDTO.getDueDate()).orElse(Constants.MAX_EXPIRATION_DATE)));
    payment.setRetentionDate(ConversionUtils.toXMLGregorianCalendar(OffsetDateTime.now().plusMinutes(15))); //the data validity of this response: set to 15 minutes
    payment.setLastPayment(true);
    payment.setDescription(StringUtils.firstNonBlank(installmentDTO.getHumanFriendlyRemittanceInformation(), installmentDTO.getRemittanceInformation()));
    payment.setCompanyName(organization.getOrgName());
    payment.setOfficeName(null);
    payment.setPaymentAmount(ConversionUtils.centsAmountToBigDecimalEuroAmount(installmentDTO.getAmountCents()));
    CtSubject debtor = new CtSubject();
    CtEntityUniqueIdentifier debtorId = new CtEntityUniqueIdentifier();
    debtorId.setEntityUniqueIdentifierType(StEntityUniqueIdentifierType.valueOf(installmentDTO.getDebtor().getEntityType()));
    debtorId.setEntityUniqueIdentifierValue(installmentDTO.getDebtor().getFiscalCode());
    debtor.setUniqueIdentifier(debtorId);
    debtor.setFullName(installmentDTO.getDebtor().getFullName());
    debtor.setCountry(installmentDTO.getDebtor().getNation());
    debtor.setStateProvinceRegion(installmentDTO.getDebtor().getProvince());
    debtor.setCity(installmentDTO.getDebtor().getLocation());
    debtor.setPostalCode(installmentDTO.getDebtor().getPostalCode());
    debtor.setStreetName(installmentDTO.getDebtor().getAddress());
    debtor.setCivicNumber(installmentDTO.getDebtor().getCivic());
    debtor.setEMail(installmentDTO.getDebtor().getEmail());
    payment.setDebtor(debtor);
    CtTransferListPAV2 transferList = new CtTransferListPAV2();
    installmentDTO.getTransfers().forEach(transferDTO -> {
      CtTransferPAV2 transfer = new CtTransferPAV2();
      transfer.setIdTransfer(transferDTO.getTransferIndex() != null ? transferDTO.getTransferIndex().intValue() : 0);
      transfer.setFiscalCodePA(transferDTO.getOrgFiscalCode());
      transfer.setCompanyName(transferDTO.getOrgName());
      transfer.setTransferAmount(ConversionUtils.centsAmountToBigDecimalEuroAmount(transferDTO.getAmountCents()));
      transfer.setTransferCategory(transferDTO.getCategory());
      transfer.setRemittanceInformation(transferDTO.getRemittanceInformation());
      transfer.setIBAN(transferType.equals(StTransferType.POSTAL) ? transferDTO.getPostalIban() : transferDTO.getIban());
      if(transferDTO.getStampHashDocument() != null) {
        CtRichiestaMarcaDaBollo richiestaMarcaDaBollo = new CtRichiestaMarcaDaBollo();
        richiestaMarcaDaBollo.setTipoBollo(transferDTO.getStampType());
        richiestaMarcaDaBollo.setHashDocumento(transferDTO.getStampHashDocument().getBytes(StandardCharsets.UTF_8));
        richiestaMarcaDaBollo.setProvinciaResidenza(transferDTO.getStampProvincialResidence());
        transfer.setRichiestaMarcaDaBollo(richiestaMarcaDaBollo);
      }
      transferList.getTransfers().add(transfer);
    });
    payment.setTransferList(transferList);
    CtMetadata metadata = new CtMetadata();
    CtMapEntry entry = new CtMapEntry();
    entry.setKey("datiSpecificiRiscossione");
    entry.setValue(installmentDTO.getLegacyPaymentMetadata());
    metadata.getMapEntries().add(entry);
    payment.setMetadata(metadata);
    PaGetPaymentV2Response response = new PaGetPaymentV2Response();
    response.setData(payment);
    response.setOutcome(StOutcome.OK);
    return response;
  }
}
