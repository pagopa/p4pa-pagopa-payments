package it.gov.pagopa.pu.pagopapayments.mapper;

import it.gov.pagopa.pagopa_api.pa.pafornode.*;
import it.gov.pagopa.pagopa_api.xsd.common_types.v1_0.CtMapEntry;
import it.gov.pagopa.pagopa_api.xsd.common_types.v1_0.CtMetadata;
import it.gov.pagopa.pagopa_api.xsd.common_types.v1_0.CtRichiestaMarcaDaBollo;
import it.gov.pagopa.pagopa_api.xsd.common_types.v1_0.StOutcome;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.pagopapayments.util.Constants;
import it.gov.pagopa.pu.pagopapayments.util.ConversionUtils;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Optional;

public class PaGetPaymentMapper {

  private PaGetPaymentMapper() {
  }

  public static PaGetPaymentV2Request paGetPaymentReq2V2(PaGetPaymentReq paGetPaymentReq) {
    PaGetPaymentV2Request requestV2 = new PaGetPaymentV2Request();
    requestV2.setAmount(paGetPaymentReq.getAmount());
    requestV2.setDueDate(paGetPaymentReq.getDueDate());
    requestV2.setIdBrokerPA(paGetPaymentReq.getIdBrokerPA());
    requestV2.setIdPA(paGetPaymentReq.getIdPA());
    requestV2.setIdStation(paGetPaymentReq.getIdStation());
    requestV2.setPaymentNote(paGetPaymentReq.getPaymentNote());
    requestV2.setQrCode(paGetPaymentReq.getQrCode());
    requestV2.setTransferType(paGetPaymentReq.getTransferType());
    return requestV2;
  }

  public static PaGetPaymentRes paGetPaymentV2Response2V1(PaGetPaymentV2Response paGetPaymentV2Response) {
    PaGetPaymentRes response = new PaGetPaymentRes();
    response.setOutcome(paGetPaymentV2Response.getOutcome());
    response.setFault(paGetPaymentV2Response.getFault());
    if(paGetPaymentV2Response.getData() != null) {
      CtPaymentPA data = new CtPaymentPA();
      CtPaymentPAV2 dataV2 = paGetPaymentV2Response.getData();
      response.setData(data);
      data.setCreditorReferenceId(dataV2.getCreditorReferenceId());
      data.setPaymentAmount(dataV2.getPaymentAmount());
      data.setDueDate(dataV2.getDueDate());
      data.setRetentionDate(dataV2.getRetentionDate());
      data.setLastPayment(dataV2.isLastPayment());
      data.setDescription(dataV2.getDescription());
      data.setCompanyName(dataV2.getCompanyName());
      data.setOfficeName(dataV2.getOfficeName());
      data.setDebtor(dataV2.getDebtor());
      data.setMetadata(dataV2.getMetadata());
      if(dataV2.getTransferList() != null) {
        data.setTransferList(new CtTransferListPA());
        dataV2.getTransferList().getTransfers().forEach(transferV2 -> {
          CtTransferPA transfer = new CtTransferPA();
          transfer.setIdTransfer(transferV2.getIdTransfer());
          transfer.setFiscalCodePA(transferV2.getFiscalCodePA());
          transfer.setTransferAmount(transferV2.getTransferAmount());
          transfer.setTransferCategory(transferV2.getTransferCategory());
          transfer.setRemittanceInformation(transferV2.getRemittanceInformation());
          transfer.setIBAN(transferV2.getIBAN());
          data.getTransferList().getTransfers().add(transfer);
        });
      }
    }
    return response;
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
    payment.setDebtor(debtor);
    CtTransferListPAV2 transferList = new CtTransferListPAV2();
    installmentDTO.getTransfers().forEach(transferDTO -> {
      CtTransferPAV2 transfer = new CtTransferPAV2();
      transfer.setIdTransfer(transferDTO.getTransferIndex() != null ? transferDTO.getTransferIndex().intValue() : 0);
      transfer.setFiscalCodePA(transferDTO.getOrgFiscalCode());
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
