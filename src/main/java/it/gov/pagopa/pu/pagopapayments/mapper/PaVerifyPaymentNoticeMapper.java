package it.gov.pagopa.pu.pagopapayments.mapper;

import it.gov.pagopa.pagopa_api.pa.pafornode.*;
import it.gov.pagopa.pagopa_api.xsd.common_types.v1_0.StOutcome;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.TransferDTO;
import it.gov.pagopa.pu.pagopapayments.dto.RetrievePaymentDTO;
import it.gov.pagopa.pu.pagopapayments.util.Constants;
import it.gov.pagopa.pu.pagopapayments.util.ConversionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

public class PaVerifyPaymentNoticeMapper {

  private PaVerifyPaymentNoticeMapper() {
  }

  public static RetrievePaymentDTO paVerifyPaymentNoticeReq2RetrievePaymentDTO(PaVerifyPaymentNoticeReq request) {
    return RetrievePaymentDTO.builder()
      .idPA(request.getIdPA())
      .idBrokerPA(request.getIdBrokerPA())
      .idStation(request.getIdStation())
      .fiscalCode(request.getQrCode().getFiscalCode())
      .noticeNumber(request.getQrCode().getNoticeNumber())
      .build();
  }

  public static PaVerifyPaymentNoticeRes installmentDto2PaVerifyPaymentNoticeRes(InstallmentDTO installment, Organization organization) {
    PaVerifyPaymentNoticeRes response = new PaVerifyPaymentNoticeRes();
    response.setFiscalCodePA(organization.getOrgFiscalCode());
    response.setCompanyName(organization.getOrgName());
    response.setOfficeName(null);
    response.setPaymentDescription(StringUtils.firstNonBlank(installment.getHumanFriendlyRemittanceInformation(), installment.getRemittanceInformation()));
    CtPaymentOptionDescriptionPA paymentOption = new CtPaymentOptionDescriptionPA();
    paymentOption.setOptions(StAmountOption.EQ);
    paymentOption.setAmount(ConversionUtils.centsAmountToBigDecimalEuroAmount(installment.getAmountCents()));
    paymentOption.setDueDate(ConversionUtils.toXMLGregorianCalendar(
      ObjectUtils.firstNonNull(installment.getDueDate(), Constants.MAX_EXPIRATION_DATE)));
    boolean postalPayment = installment.getTransfers().stream().map(TransferDTO::getPostalIban).noneMatch(StringUtils::isBlank);
    paymentOption.setAllCCP(postalPayment);
    CtPaymentOptionsDescriptionListPA paymentOptions = new CtPaymentOptionsDescriptionListPA();
    paymentOptions.setPaymentOptionDescription(paymentOption);
    response.setPaymentList(paymentOptions);
    response.setOutcome(StOutcome.OK);
    return response;
  }
}
