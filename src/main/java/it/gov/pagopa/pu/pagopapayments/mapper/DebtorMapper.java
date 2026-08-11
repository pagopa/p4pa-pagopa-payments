package it.gov.pagopa.pu.pagopapayments.mapper;

import io.micrometer.common.util.StringUtils;
import it.gov.pagopa.pu.debtpositions.dto.generated.PersonDTO;
import it.gov.pagopa.nodo.printpaymentnotice.dto.generated.DebtorDTO;
import org.springframework.stereotype.Component;

@Component
public class DebtorMapper {
  private DebtorMapper() {
  }

  public static DebtorDTO toDebtorDTO(PersonDTO person) {
    DebtorDTO debtor = new DebtorDTO();
    debtor.setAddress(StringUtils.isBlank(person.getAddress()) ? " " : person.getAddress());
    debtor.setBuildingNumber(StringUtils.isBlank(person.getCivic()) ? " " : person.getCivic());
    debtor.setCity(StringUtils.isBlank(person.getLocation()) ? " " : person.getLocation());
    debtor.setFullName(person.getFullName());
    debtor.setPostalCode(StringUtils.isBlank(person.getPostalCode()) ? " " : person.getPostalCode());
    debtor.setProvince(StringUtils.isBlank(person.getProvince()) ? " " : person.getProvince());

    return debtor;
  }
}
