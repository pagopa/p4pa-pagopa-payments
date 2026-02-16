package it.gov.pagopa.pu.pagopapayments.mapper;

import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.orgfornode.dto.generated.*;
import it.gov.pagopa.pu.pagopapayments.util.ConversionUtils;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import static it.gov.pagopa.pu.pagopapayments.util.DebtPositionUtils.PAID_INSTALLMENT_STATUSES;

@Component
public class DebtPositions2PaymentOptionsNodeResponseMapper {

  public PaymentOptionsResponseForNode mapToResponse(DebtPositionDTO dp, Organization organization) {
    PaymentOptionsResponseForNode response = new PaymentOptionsResponseForNode();
    response.setOrganizationFiscalCode(organization.getOrgFiscalCode());
    response.setCompanyName(organization.getOrgName());
    response.setOfficeName(null);
    response.setStandin(false);

    if (dp == null) {
      response.setPaymentOptions(List.of());
      return response;
    }

    List<PaymentOptionForNode> paymentOptions = dp.getPaymentOptions().stream()
      .filter(poDTO -> poDTO.getStatus() == PaymentOptionStatus.UNPAID
        || poDTO.getStatus() == PaymentOptionStatus.PARTIALLY_PAID)
      .map(poDTO -> mapPO(poDTO, dp))
      .toList();

    response.setPaymentOptions(paymentOptions);

    return response;
  }

  private PaymentOptionForNode mapPO(PaymentOptionDTO poDTO, DebtPositionDTO dpDTO) {
    List<InstallmentDTO> src = poDTO.getInstallments();

    List<InstallmentDTO> notPaid = src.stream()
      .filter(inst -> inst.getStatus() == null || !PAID_INSTALLMENT_STATUSES.contains(inst.getStatus()))
      .toList();

    if (notPaid.isEmpty()) {
      return null;
    }

    PaymentOptionForNode po = new PaymentOptionForNode();
    po.setDescription(poDTO.getDescription());
    po.setNumberOfInstallments(poDTO.getInstallments().size());
    po.setDueDate(calculateMaxDueDate(poDTO.getInstallments()));
    po.setValidFrom(
      dpDTO.getValidityDate() != null
        ? dpDTO.getValidityDate().atStartOfDay().toString()
        : null
    );
    po.setAmount(poDTO.getTotalAmountCents());
    po.setStatus(mapPOStatus(poDTO.getStatus()));
    po.setStatusReason(null);
    po.setAllCCP(false);

    po.setInstallments(notPaid.stream().map(inst -> mapInstallment(inst, dpDTO)).toList());

    return po;
  }

  private String calculateMaxDueDate(List<InstallmentDTO> installments) {
    return installments.stream()
      .map(InstallmentDTO::getDueDate)
      .filter(Objects::nonNull)
      .max(LocalDate::compareTo)
      .map(d -> ConversionUtils.atEndOfDay(d).toString())
      .orElse(null);
  }

  private InstallmentForNode mapInstallment(InstallmentDTO installmentDTO, DebtPositionDTO dpDTO) {
    InstallmentForNode installment = new InstallmentForNode();

    installment.setNav(installmentDTO.getNav());
    installment.setIuv(installmentDTO.getIuv());
    installment.setAmount(installmentDTO.getAmountCents());
    installment.setDescription(installmentDTO.getRemittanceInformation());
    installment.setDueDate(
      installmentDTO.getDueDate() != null
        ? ConversionUtils.atEndOfDay(installmentDTO.getDueDate()).toString()
        : null
    );
    installment.setValidFrom(
      dpDTO.getValidityDate() != null
        ? dpDTO.getValidityDate().atStartOfDay().toString()
        : null
    );
    installment.setStatus(mapInstallmentStatus(installmentDTO.getStatus()));
    installment.setStatusReason(null);

    return installment;
  }

  private EnumPoForNode mapPOStatus(PaymentOptionStatus status) {
    if (status == null) return null;

    return switch (status) {
      case UNPAID -> EnumPoForNode.PO_UNPAID;
      case REPORTED, PAID -> EnumPoForNode.PO_PAID;
      case PARTIALLY_PAID -> EnumPoForNode.PO_PARTIALLY_PAID;
      case EXPIRED -> EnumPoForNode.PO_EXPIRED_NOT_PAYABLE;
      case INVALID, CANCELLED, TO_SYNC, UNPAYABLE, DRAFT -> EnumPoForNode.PO_INVALID;
    };
  }

  private EnumInstallmentForNode mapInstallmentStatus(InstallmentStatus status) {
    if (status == null) return null;

    return switch (status) {
      case UNPAID -> EnumInstallmentForNode.POI_UNPAID;
      case REPORTED, PAID -> EnumInstallmentForNode.POI_PAID;
      case EXPIRED -> EnumInstallmentForNode.POI_EXPIRED_NOT_PAYABLE;
      case INVALID, CANCELLED, TO_SYNC, UNPAYABLE, DRAFT -> EnumInstallmentForNode.POI_INVALID;
    };
  }
}
