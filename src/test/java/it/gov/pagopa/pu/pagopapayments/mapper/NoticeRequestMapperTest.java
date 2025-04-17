package it.gov.pagopa.pu.pagopapayments.mapper;

import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.PersonDTO;
import it.gov.pagopa.pu.pagopapayments.util.TestUtils;
import it.gov.pagopa.pu.printpaymentnotice.connector.printpaymentnotice.generated.dto.DebtorDTO;
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
  void givenValidPersonWhenToDebtorDTOThenOk() {
    //given
    PersonDTO personRequest = podamFactory.manufacturePojo(PersonDTO.class);

    //when
    DebtorDTO response = NoticeRequestMapper.toDebtorDTO(personRequest);

    //verify
    assertNotNull(response);
    Assertions.assertEquals(personRequest.getAddress(), response.getAddress());
    Assertions.assertEquals(personRequest.getCivic(), response.getBuildingNumber());
    Assertions.assertEquals(personRequest.getLocation(), response.getCity());
    Assertions.assertEquals(personRequest.getFullName(), response.getFullName());
    Assertions.assertEquals(personRequest.getPostalCode(), response.getPostalCode());
    Assertions.assertEquals(personRequest.getProvince(), response.getProvince());
  }

  @Test
  void givenInvalidAddressWhenToDebtorDTOThenError() {
    //given
    PersonDTO personRequest = podamFactory.manufacturePojo(PersonDTO.class);
    personRequest.setAddress(null);

    //when & verify
    Assertions.assertThrows(IllegalArgumentException.class, () -> NoticeRequestMapper.toDebtorDTO(personRequest));
  }

  @Test
  void givenInvalidCivicWhenToDebtorDTOThenError() {
    //given
    PersonDTO personRequest = podamFactory.manufacturePojo(PersonDTO.class);
    personRequest.setCivic(null);

    //when & verify
    Assertions.assertThrows(IllegalArgumentException.class, () -> NoticeRequestMapper.toDebtorDTO(personRequest));
  }

  @Test
  void givenInvalidLocationWhenToDebtorDTOThenError() {
    //given
    PersonDTO personRequest = podamFactory.manufacturePojo(PersonDTO.class);
    personRequest.setLocation(null);

    //when & verify
    Assertions.assertThrows(IllegalArgumentException.class, () -> NoticeRequestMapper.toDebtorDTO(personRequest));
  }

  @Test
  void givenInvalidPostalCodeWhenToDebtorDTOThenError() {
    //given
    PersonDTO personRequest = podamFactory.manufacturePojo(PersonDTO.class);
    personRequest.setPostalCode(null);

    //when & verify
    Assertions.assertThrows(IllegalArgumentException.class, () -> NoticeRequestMapper.toDebtorDTO(personRequest));
  }

  @Test
  void givenInvalidProvinceWhenToDebtorDTOThenError() {
    //given
    PersonDTO personRequest = podamFactory.manufacturePojo(PersonDTO.class);
    personRequest.setProvince(null);

    //when & verify
    Assertions.assertThrows(IllegalArgumentException.class, () -> NoticeRequestMapper.toDebtorDTO(personRequest));
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
  }

  @Test
  void givenValidInputWhenToNoticeRequestDataDTOThenOk() {
    //given
    InstallmentDTO installmentRequest = podamFactory.manufacturePojo(InstallmentDTO.class);
    PersonDTO personRequest = podamFactory.manufacturePojo(PersonDTO.class);
    String taxCode = "999999982";

    //when
    NoticeRequestDataDTO response = NoticeRequestMapper.toNoticeRequestDataDTO(taxCode, installmentRequest, personRequest);

    //verify
    assertNotNull(response);
    Assertions.assertEquals(NoticeRequestMapper.toNoticeDTO(installmentRequest), response.getNotice());
    Assertions.assertEquals(NoticeRequestMapper.toDebtorDTO(personRequest), response.getDebtor());
    Assertions.assertEquals(taxCode, response.getCreditorInstitution().getTaxCode());
  }
}
