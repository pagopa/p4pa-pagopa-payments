package it.gov.pagopa.pu.pagopapayments.mapper;

import it.gov.pagopa.pagopa_api.pa.pafornode.*;
import it.gov.pagopa.pagopa_api.xsd.common_types.v1_0.StOutcome;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.TransferDTO;
import it.gov.pagopa.pu.organization.dto.generated.Broker;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.pagopapayments.dto.RetrievePaymentDTO;
import it.gov.pagopa.pu.pagopapayments.util.ConversionUtils;
import it.gov.pagopa.pu.pagopapayments.util.Utilities;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Optional;

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

  public static PaVerifyPaymentNoticeRes installmentDto2PaVerifyPaymentNoticeRes(InstallmentDTO installment, Organization organization, Broker broker) {
    PaVerifyPaymentNoticeRes response = new PaVerifyPaymentNoticeRes();

    List<TransferDTO> transfers = Optional.ofNullable(installment.getTransfers()).orElse(List.of());

    Pair<String, String> orgInfo = resolveOrganizationInfo(organization, broker, transfers);
    response.setFiscalCodePA(orgInfo.getLeft());
    response.setCompanyName(orgInfo.getRight());
    response.setOfficeName(null);

    CtPaymentOptionDescriptionPA paymentOption = new CtPaymentOptionDescriptionPA();
    response.setPaymentDescription(Utilities.truncateRemittanceInformation(installment.getRemittanceInformation()));
    paymentOption.setOptions(StAmountOption.EQ);
    paymentOption.setAmount(ConversionUtils.centsAmountToBigDecimalEuroAmount(installment.getAmountCents()));
    paymentOption.setDueDate(ConversionUtils.toXMLGregorianCalendar(ConversionUtils.localDate2RomeMaxTime(installment.getDueDate())));

    boolean postalPayment = transfers.stream()
      .filter(t -> t.getAmountCents() > 0)
      .map(TransferDTO::getPostalIban)
      .noneMatch(StringUtils::isBlank);

    paymentOption.setAllCCP(postalPayment);

    CtPaymentOptionsDescriptionListPA paymentOptions = new CtPaymentOptionsDescriptionListPA();
    paymentOptions.setPaymentOptionDescription(paymentOption);
    response.setPaymentList(paymentOptions);
    response.setOutcome(StOutcome.OK);

    return response;
  }

  public static Pair<String, String> resolveOrganizationInfo(Organization organization, Broker broker, List<TransferDTO> transfers) {
    String fiscalCodePA = organization.getOrgFiscalCode();
    String companyName = organization.getOrgName();

    if (broker != null && Boolean.TRUE.equals(broker.getFlagDelegate())) {
      TransferDTO owner = transfers.stream()
        .filter(t -> Boolean.TRUE.equals(t.getFlagOwner()))
        .findFirst()
        .orElse(null);

      if (owner != null) {
        if (StringUtils.isNotBlank(owner.getOrgFiscalCode())) {
          fiscalCodePA = owner.getOrgFiscalCode();
        }
        if (StringUtils.isNotBlank(owner.getOrgName())) {
          companyName = owner.getOrgName();
        }
      }
    }

    return Pair.of(fiscalCodePA, companyName);
  }
}
