package it.gov.pagopa.pu.pagopapayments.mapper;

import static org.junit.jupiter.api.Assertions.*;

import it.gov.pagopa.pu.pagopapayments.exception.ValidatorException;
import org.junit.jupiter.api.Test;

class BalanceMapperTest {

  private final BalanceMapper balanceMapper = new BalanceMapper();


  @Test
  void givenBalanceWhenMapBalanceFromPuSilThenReturnStringXml() {
    String jsonBalance = "[{" +
      "\"capitolo\": \"Dionysus\"," +
      "\"ufficio\": \"Prometheus\"," +
      "\"accertamento\": \"Meleager\"," +
      "\"importo\": 9.53" +
      "}]";

    String expectedXml = "<bilancio><capitolo><codCapitolo>Dionysus</codCapitolo>" +
      "<codUfficio>Prometheus</codUfficio>" +
      "<accertamento><codAccertamento>Meleager</codAccertamento>" +
      "<importo>9.53</importo></accertamento></capitolo></bilancio>";

    String result = balanceMapper.mapBalanceFromPuSil(jsonBalance);
    assertEquals(expectedXml, result);
  }



  @Test
  void givenBalanceWhenMapBalanceFromPuSilThenReturnNull() {
    String jsonBalance = "[{" +
      "\"ufficio\": \"Prometheus\"," +
      "\"accertamento\": \"Meleager\"," +
      "\"importo\": 9.53" +
      "}]";

    String result = balanceMapper.mapBalanceFromPuSil(jsonBalance);
    assertNull(result);
  }


  @Test
  void givenBalanceWhenMapBalanceFromPuSilThenThrowException() {
    String invalidJson = "not a json";

    ValidatorException exception = assertThrows(ValidatorException.class, () -> {
      balanceMapper.mapBalanceFromPuSil(invalidJson);
    });

    assertTrue(exception.getMessage().contains("Error while map balance from PuSil"));
  }


  @Test
  void givenEmptyListWhenMapBalanceFromPuSilThenReturnNull() {
    String emptyListJson = "[]";

    String result = balanceMapper.mapBalanceFromPuSil(emptyListJson);
    assertNull(result);
  }

}
