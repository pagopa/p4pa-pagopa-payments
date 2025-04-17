package it.gov.pagopa.pu.pagopapayments.mapper;

import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.PersonDTO;
import it.gov.pagopa.pu.printpaymentnotice.connector.printpaymentnotice.generated.dto.CreditorInstitutionDTO;
import it.gov.pagopa.pu.printpaymentnotice.connector.printpaymentnotice.generated.dto.DebtorDTO;
import it.gov.pagopa.pu.printpaymentnotice.connector.printpaymentnotice.generated.dto.NoticeDTO;
import it.gov.pagopa.pu.printpaymentnotice.connector.printpaymentnotice.generated.dto.NoticeRequestDataDTO;
import org.springframework.stereotype.Component;

import static it.gov.pagopa.pu.pagopapayments.util.Utilities.isFieldValid;

@Component
public class NoticeRequestMapper {
  private NoticeRequestMapper() {
  }

  public static DebtorDTO toDebtorDTO(PersonDTO person) {
    if (!isFieldValid(person.getAddress())) {
      throw new IllegalArgumentException("Debtor address cannot be null or empty");
    }
    if (!isFieldValid(person.getCivic())) {
      throw new IllegalArgumentException("Debtor building Number cannot be null or empty");
    }
    if (!isFieldValid(person.getLocation())) {
      throw new IllegalArgumentException("Debtor city cannot be null or empty");
    }
    if (!isFieldValid(person.getPostalCode())) {
      throw new IllegalArgumentException("Debtor postal Code cannot be null or empty");
    }
    if (!isFieldValid(person.getProvince())) {
      throw new IllegalArgumentException("Debtor province cannot be null or empty");
    }

    DebtorDTO debtor = new DebtorDTO();
    debtor.setAddress(person.getAddress());
    debtor.setBuildingNumber(person.getCivic());
    debtor.setCity(person.getLocation());
    debtor.setFullName(person.getFullName());
    debtor.setPostalCode(person.getPostalCode());
    debtor.setProvince(person.getProvince());

    return debtor;
  }

  public static NoticeDTO toNoticeDTO(InstallmentDTO installment) {
    NoticeDTO notice = new NoticeDTO();
    notice.setCode(installment.getNav());
    notice.setDueDate(installment.getDueDate() != null ? installment.getDueDate().toString() : null);
    notice.setPaymentAmount(installment.getAmountCents());
    notice.setSubject(installment.getRemittanceInformation());
    return notice;
  }

  public static NoticeRequestDataDTO toNoticeRequestDataDTO(String taxCode, InstallmentDTO installment, PersonDTO person) {
    NoticeRequestDataDTO noticeRequestData = new NoticeRequestDataDTO();

    CreditorInstitutionDTO ci = new CreditorInstitutionDTO();
    ci.setTaxCode(taxCode);

    noticeRequestData.setCreditorInstitution(ci);
    noticeRequestData.setDebtor(toDebtorDTO(person));
    noticeRequestData.setNotice(toNoticeDTO(installment));

    return noticeRequestData;
  }
}
