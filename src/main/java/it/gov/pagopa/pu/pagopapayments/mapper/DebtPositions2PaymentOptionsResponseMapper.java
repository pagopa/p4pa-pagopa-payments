package it.gov.pagopa.pu.pagopapayments.mapper;

import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import it.gov.pagopa.pu.orgfornode.dto.generated.PaymentOption;
import it.gov.pagopa.pu.orgfornode.dto.generated.*;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.pagopapayments.util.ConversionUtils;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Component
public class DebtPositions2PaymentOptionsResponseMapper {

  public PaymentOptionsResponse mapToResponse(List<DebtPositionDTO> debtPositions, Organization organization) {
    PaymentOptionsResponse response = new PaymentOptionsResponse();

    if (debtPositions == null || debtPositions.isEmpty()) {
      return response;
    }

    response.setOrganizationFiscalCode(organization.getOrgFiscalCode());
    response.setCompanyName(organization.getOrgName());
    response.setOfficeName(null);
    response.setStandin(false);

    List<PaymentOption> paymentOptions = debtPositions.stream()
      .flatMap(dp -> dp.getPaymentOptions().stream()
        .map(poDTO -> mapPO(poDTO, dp)))
      .toList();

    response.setPaymentOptions(paymentOptions);

    return response;
  }

  private PaymentOption mapPO(PaymentOptionDTO poDTO, DebtPositionDTO dpDTO) {
    PaymentOption po = new PaymentOption();

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

    List<Installment> installments = poDTO.getInstallments()
      .stream()
      .map(inst -> mapInstallment(inst, dpDTO))
      .toList();

    po.setInstallments(installments);

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

  private Installment mapInstallment(InstallmentDTO installmentDTO, DebtPositionDTO dpDTO) {
    Installment installment = new Installment();

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

  private EnumPo mapPOStatus(PaymentOptionStatus status) {
    if (status == null) return null;

    return switch (status) {
      case UNPAID -> EnumPo.PO_UNPAID;
      case REPORTED, PAID -> EnumPo.PO_PAID;
      case PARTIALLY_PAID -> EnumPo.PO_PARTIALLY_PAID;
      case EXPIRED -> EnumPo.PO_EXPIRED_NOT_PAYABLE;
      case INVALID, CANCELLED, TO_SYNC, UNPAYABLE, DRAFT -> EnumPo.PO_INVALID;
    };
  }

  private EnumInstallment mapInstallmentStatus(InstallmentStatus status) {
    if (status == null) return null;

    return switch (status) {
      case UNPAID -> EnumInstallment.POI_UNPAID;
      case REPORTED, PAID -> EnumInstallment.POI_PAID;
      case EXPIRED -> EnumInstallment.POI_EXPIRED_NOT_PAYABLE;
      case INVALID, CANCELLED, TO_SYNC, UNPAYABLE, DRAFT ->
        EnumInstallment.POI_INVALID;
    };
  }
}
