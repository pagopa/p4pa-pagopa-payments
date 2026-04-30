package it.gov.pagopa.pu.pagopapayments.mapper;

import it.gov.pagopa.pu.debtpositions.dto.generated.PersonDTO;
import it.gov.pagopa.pu.pagopapayments.util.TestUtils;
import it.gov.pagopa.pu.printpaymentnotice.connector.printpaymentnotice.generated.dto.DebtorDTO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.jemos.podam.api.PodamFactory;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class DebtorMapperTest {

  private final PodamFactory podamFactory;

  public DebtorMapperTest() {
    this.podamFactory = TestUtils.getPodamFactory();
  }

  @Test
  void givenValidPersonWhenToDebtorDTOThenOk() {
    //given
    PersonDTO personRequest = podamFactory.manufacturePojo(PersonDTO.class);

    //when
    DebtorDTO response = DebtorMapper.toDebtorDTO(personRequest);

    //verify
    assertNotNull(response);
    Assertions.assertEquals(personRequest.getAddress(), response.getAddress());
    Assertions.assertEquals(personRequest.getCivic(), response.getBuildingNumber());
    Assertions.assertEquals(personRequest.getLocation(), response.getCity());
    Assertions.assertEquals(personRequest.getFullName(), response.getFullName());
    Assertions.assertEquals(personRequest.getPostalCode(), response.getPostalCode());
    Assertions.assertEquals(personRequest.getProvince(), response.getProvince());
    TestUtils.checkNotNullFields(response, "taxCode");
  }

  @Test
  void givenValidPersonWithNullAddressFieldsWhenToDebtorDTOThenOk() {
    //given
    PersonDTO personRequest = podamFactory.manufacturePojo(PersonDTO.class);
    personRequest.setAddress(null);
    personRequest.setLocation(null);
    personRequest.setCivic(null);
    personRequest.setProvince(null);
    personRequest.setPostalCode(null);

    //when
    DebtorDTO response = DebtorMapper.toDebtorDTO(personRequest);

    //verify
    assertNotNull(response);
    Assertions.assertEquals(" ", response.getAddress());
    Assertions.assertEquals(" ", response.getBuildingNumber());
    Assertions.assertEquals(" ", response.getCity());
    Assertions.assertEquals(personRequest.getFullName(), response.getFullName());
    Assertions.assertEquals(" ", response.getPostalCode());
    Assertions.assertEquals(" ", response.getProvince());
    TestUtils.checkNotNullFields(response, "taxCode");
  }

}
