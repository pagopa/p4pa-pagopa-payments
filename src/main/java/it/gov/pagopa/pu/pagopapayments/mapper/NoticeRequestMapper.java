package it.gov.pagopa.pu.pagopapayments.mapper;

import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.PersonDTO;
import it.gov.pagopa.pu.printpaymentnotice.connector.printpaymentnotice.generated.dto.CreditorInstitutionDTO;
import it.gov.pagopa.pu.printpaymentnotice.connector.printpaymentnotice.generated.dto.NoticeDTO;
import it.gov.pagopa.pu.printpaymentnotice.connector.printpaymentnotice.generated.dto.NoticeRequestDataDTO;
import org.springframework.stereotype.Component;

import static it.gov.pagopa.pu.pagopapayments.mapper.DebtorMapper.toDebtorDTO;

@Component
public class NoticeRequestMapper {
  private NoticeRequestMapper() {
  }

  public static NoticeDTO toNoticeDTO(InstallmentDTO installment) {
    NoticeDTO notice = new NoticeDTO();
    notice.setCode(installment.getNav());
    notice.setDueDate(installment.getDueDate() != null ? installment.getDueDate().toString() : null);
    notice.setPaymentAmount(installment.getAmountCents());
    notice.setSubject(installment.getRemittanceInformation());
    return notice;
  }

  public static NoticeRequestDataDTO toNoticeRequestDataDTO(String orgFiscalCode, InstallmentDTO installment, PersonDTO person) {
    NoticeRequestDataDTO noticeRequestData = new NoticeRequestDataDTO();

    CreditorInstitutionDTO ci = new CreditorInstitutionDTO();
    ci.setTaxCode(orgFiscalCode);

    noticeRequestData.setCreditorInstitution(ci);
    noticeRequestData.setDebtor(toDebtorDTO(person));
    noticeRequestData.setNotice(toNoticeDTO(installment));

    return noticeRequestData;
  }
}
