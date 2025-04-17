package it.gov.pagopa.pu.pagopapayments.mapper;

import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.PersonDTO;
import it.gov.pagopa.pu.pagopapayments.util.TestUtils;
import it.gov.pagopa.pu.printpaymentnotice.connector.printpaymentnotice.generated.dto.NoticeDTO;
import it.gov.pagopa.pu.printpaymentnotice.connector.printpaymentnotice.generated.dto.NoticeRequestDataDTO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.jemos.podam.api.PodamFactory;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class NoticeRequestMapperTest {

  private final PodamFactory podamFactory;

  public NoticeRequestMapperTest() {
    this.podamFactory = TestUtils.getPodamFactory();
  }

  @Test
  void givenValidInstallmentWhenToNoticeDTOThenOk() {
    //given
    InstallmentDTO installmentRequest = podamFactory.manufacturePojo(InstallmentDTO.class);

    //when
    NoticeDTO response = NoticeRequestMapper.toNoticeDTO(installmentRequest);

    //verify
    assertNotNull(response);
    Assertions.assertEquals(installmentRequest.getNav(), response.getCode());
    Assertions.assertEquals(installmentRequest.getDueDate().toString(), response.getDueDate());
    Assertions.assertEquals(installmentRequest.getAmountCents(), response.getPaymentAmount());
    Assertions.assertEquals(installmentRequest.getRemittanceInformation(), response.getSubject());
    TestUtils.checkNotNullFields(response, "installments", "discounted", "reduced");
  }

  @Test
  void givenValidInstallmentWithNullDueDateWhenToNoticeDTOThenOk() {
    //given
    InstallmentDTO installmentRequest = podamFactory.manufacturePojo(InstallmentDTO.class);
    installmentRequest.setDueDate(null);

    //when
    NoticeDTO response = NoticeRequestMapper.toNoticeDTO(installmentRequest);

    //verify
    assertNotNull(response);
    Assertions.assertEquals(installmentRequest.getNav(), response.getCode());
    Assertions.assertNull(response.getDueDate());
    Assertions.assertEquals(installmentRequest.getAmountCents(), response.getPaymentAmount());
    Assertions.assertEquals(installmentRequest.getRemittanceInformation(), response.getSubject());
    TestUtils.checkNotNullFields(response, "dueDate", "installments", "discounted", "reduced");
  }

  @Test
  void givenValidInputWhenToNoticeRequestDataDTOThenOk() {
    //given
    InstallmentDTO installmentRequest = podamFactory.manufacturePojo(InstallmentDTO.class);
    PersonDTO personRequest = podamFactory.manufacturePojo(PersonDTO.class);
    String orgFiscalCode = "999999982";

    //when
    NoticeRequestDataDTO response = NoticeRequestMapper.toNoticeRequestDataDTO(orgFiscalCode, installmentRequest, personRequest);

    //verify
    assertNotNull(response);
    Assertions.assertEquals(NoticeRequestMapper.toNoticeDTO(installmentRequest), response.getNotice());
    Assertions.assertEquals(DebtorMapper.toDebtorDTO(personRequest), response.getDebtor());
    Assertions.assertEquals(orgFiscalCode, response.getCreditorInstitution().getTaxCode());
    TestUtils.checkNotNullFields(response);
  }
}
