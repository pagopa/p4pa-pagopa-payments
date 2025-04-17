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
  }

  @Test
  void givenInvalidAddressWhenToDebtorDTOThenError() {
    //given
    PersonDTO personRequest = podamFactory.manufacturePojo(PersonDTO.class);
    personRequest.setAddress(null);

    //when & verify
    Assertions.assertThrows(IllegalArgumentException.class, () -> DebtorMapper.toDebtorDTO(personRequest));
  }

  @Test
  void givenInvalidCivicWhenToDebtorDTOThenError() {
    //given
    PersonDTO personRequest = podamFactory.manufacturePojo(PersonDTO.class);
    personRequest.setCivic(null);

    //when & verify
    Assertions.assertThrows(IllegalArgumentException.class, () -> DebtorMapper.toDebtorDTO(personRequest));
  }

  @Test
  void givenInvalidLocationWhenToDebtorDTOThenError() {
    //given
    PersonDTO personRequest = podamFactory.manufacturePojo(PersonDTO.class);
    personRequest.setLocation(null);

    //when & verify
    Assertions.assertThrows(IllegalArgumentException.class, () -> DebtorMapper.toDebtorDTO(personRequest));
  }

  @Test
  void givenInvalidPostalCodeWhenToDebtorDTOThenError() {
    //given
    PersonDTO personRequest = podamFactory.manufacturePojo(PersonDTO.class);
    personRequest.setPostalCode(null);

    //when & verify
    Assertions.assertThrows(IllegalArgumentException.class, () -> DebtorMapper.toDebtorDTO(personRequest));
  }

  @Test
  void givenInvalidProvinceWhenToDebtorDTOThenError() {
    //given
    PersonDTO personRequest = podamFactory.manufacturePojo(PersonDTO.class);
    personRequest.setProvince(null);

    //when & verify
    Assertions.assertThrows(IllegalArgumentException.class, () -> DebtorMapper.toDebtorDTO(personRequest));
  }
}
