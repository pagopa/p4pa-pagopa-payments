package it.gov.pagopa.pu.pagopapayments.mapper;

import it.gov.pagopa.pu.debtpositions.dto.generated.PersonDTO;
import it.gov.pagopa.pu.printpaymentnotice.connector.printpaymentnotice.generated.dto.DebtorDTO;
import org.springframework.stereotype.Component;

import static it.gov.pagopa.pu.pagopapayments.util.Utilities.isFieldValid;

@Component
public class DebtorMapper {
  private DebtorMapper() {
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
}
