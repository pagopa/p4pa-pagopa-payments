package it.gov.pagopa.pu.pagopapayments.mapper;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.pu.pagopapayments.exception.ValidatorException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class BalanceMapper {

  private final static String CAPITOLO = "capitolo";
  private final static String IMPORTO = "importo";

  public String mapBalanceFromPuSil(String balance) {
    try{
      ObjectMapper mapper = new ObjectMapper();
      List<Map<String, Object>> balanceList = mapper.readValue(balance, new TypeReference<>() {});
      if (balanceList != null && !balanceList.isEmpty()) {
        if (StringUtils.isNotBlank((String) balanceList.get(0).get(CAPITOLO)) &&
          balanceList.get(0).containsKey(IMPORTO)) {

          StringBuilder sb = new StringBuilder("<bilancio>");
          BigDecimal importFromBalance = BigDecimal.ZERO;

          for (Map<String, Object> bb : balanceList) {
            if (StringUtils.isNotBlank((String) bb.get(CAPITOLO))) {
              sb.append("<capitolo>");
              sb.append("<codCapitolo>").append(bb.get(CAPITOLO)).append("</codCapitolo>");

              if (StringUtils.isNotBlank((String) bb.get("ufficio"))) {
                sb.append("<codUfficio>").append(bb.get("ufficio")).append("</codUfficio>");
              }

              sb.append("<accertamento>");
              if (StringUtils.isNotBlank((String) bb.get("accertamento"))) {
                sb.append("<codAccertamento>").append(bb.get("accertamento")).append("</codAccertamento>");
              }

              sb.append("<importo>").append(bb.get(IMPORTO)).append("</importo>");
              importFromBalance = importFromBalance.add(new BigDecimal(bb.get(IMPORTO).toString()));
              sb.append("</accertamento></capitolo>");
            }
          }
          sb.append("</bilancio>");
          return sb.toString();
        } else {
          log.error("Field chapter or import not present.");
        }
      }
    } catch (JsonProcessingException e) {
      throw new ValidatorException("Error while map balance from PuSil.", e);
    }
    return null;
  }
}
