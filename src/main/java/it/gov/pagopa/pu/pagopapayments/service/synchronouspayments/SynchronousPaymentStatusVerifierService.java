package it.gov.pagopa.pu.pagopapayments.service.synchronouspayments;

import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.pagopapayments.enums.PagoPaNodeFaults;
import it.gov.pagopa.pu.pagopapayments.exception.SynchronousPaymentException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class SynchronousPaymentStatusVerifierService {

  public InstallmentDTO verifyPaymentStatus(Organization organization, List<InstallmentDTO> installmentDTOList, String noticeNumber, Boolean postalTransfer) {
    if (installmentDTOList.isEmpty()) {
      log.debug("getPayableDebtPositionByOrganizationAndNav [{}/{}]: no debt positions found", organization.getOrgFiscalCode(), noticeNumber);
      throw new SynchronousPaymentException(PagoPaNodeFaults.PAA_PAGAMENTO_SCONOSCIUTO, organization.getOrgFiscalCode());
    }

    /*
     * Rules (first one true wins, in this order):
     * If no data found -> KO PAA_PAGAMENTO_SCONOSCIUTO
     * If there is 1 UNPAID -> OK
     * If there are >1 UNPAID -> KO PAA_PAGAMENTO_DUPLICATO
     * If there is >= 1 EXPIRED -> KO PAA_PAGAMENTO_SCADUTO
     * If there is >= 1 PAID -> KO PAA_PAGAMENTO_SCADUTO
     * If there is >= 1 CANCELLED -> KO PAA_PAGAMENTO_ANNULLATO
     * Any other case -> KO PAA_PAGAMENTO_SCONOSCIUTO
     */

    List<InstallmentDTO> payableInstallmentDTOList = installmentDTOList.stream().filter(i -> {
      //if status is not UNPAID, the installment is not payable
      if (!Objects.equals(i.getStatus(), SynchronousPaymentService.PaymentStatus.UNPAID.name()))
        return false;
      //only for getPayment (for verifyPayment, postalTransfer is not set):
      //if at least 1 transfer of the same organization that created the debt position
      // does not have a suitable IBAN for payment type (postal/pagopa), the installment is not payable
      return postalTransfer==null || i.getTransfers().stream()
        .filter(t -> Objects.equals(t.getOrgFiscalCode(), organization.getOrgFiscalCode()))
        .map(t -> postalTransfer ? t.getPostalIban() : t.getIban())
        .noneMatch(StringUtils::isBlank);
    }).toList();
    if (payableInstallmentDTOList.size() > 1) {
      log.warn("getPayableDebtPositionByOrganizationAndNav [{}/{}]: multiple payable debt positions found", organization.getOrgFiscalCode(), noticeNumber);
      throw new SynchronousPaymentException(PagoPaNodeFaults.PAA_PAGAMENTO_DUPLICATO, organization.getOrgFiscalCode());
    } else if (payableInstallmentDTOList.size() == 1) {
      return payableInstallmentDTOList.getFirst();
    } else if (installmentDTOList.stream().anyMatch(i -> Objects.equals(i.getStatus(), SynchronousPaymentService.PaymentStatus.EXPIRED.name()))) {
      throw new SynchronousPaymentException(PagoPaNodeFaults.PAA_PAGAMENTO_SCADUTO, organization.getOrgFiscalCode());
    } else if (installmentDTOList.stream().anyMatch(i -> Objects.equals(i.getStatus(), SynchronousPaymentService.PaymentStatus.PAID.name()))) {
      throw new SynchronousPaymentException(PagoPaNodeFaults.PAA_PAGAMENTO_SCONOSCIUTO, organization.getOrgFiscalCode());
    } else if (installmentDTOList.stream().anyMatch(i -> Objects.equals(i.getStatus(), SynchronousPaymentService.PaymentStatus.CANCELLED.name()))) {
      throw new SynchronousPaymentException(PagoPaNodeFaults.PAA_PAGAMENTO_ANNULLATO, organization.getOrgFiscalCode());
    } else {
      throw new SynchronousPaymentException(PagoPaNodeFaults.PAA_PAGAMENTO_SCONOSCIUTO, organization.getOrgFiscalCode());
    }
  }

}
