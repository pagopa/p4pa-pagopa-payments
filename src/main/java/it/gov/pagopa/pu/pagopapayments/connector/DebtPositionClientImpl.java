package it.gov.pagopa.pu.pagopapayments.connector;

import it.gov.pagopa.pu.p4pa_debt_positions.controller.ApiClient;
import it.gov.pagopa.pu.p4pa_debt_positions.controller.generated.DebtPositionTypeOrgEntityControllerApi;
import it.gov.pagopa.pu.p4pa_debt_positions.dto.generated.DebtPositionTypeOrg;
import it.gov.pagopa.pu.pagopapayments.util.RestUtil;
import it.gov.pagopa.pu.pagopapayments.util.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class DebtPositionClientImpl implements DebtPositionClient {

  private final DebtPositionTypeOrgEntityControllerApi debtPositionTypeOrgEntityControllerApi;
  private final ThreadLocal<String> bearerTokenHolder = new ThreadLocal<>();

  public DebtPositionClientImpl(@Value("${app.debt-position.base-url}") String debtPositionBaseUrl,
                                RestTemplateBuilder restTemplateBuilder){
    RestTemplate restTemplate = restTemplateBuilder.build();
    ApiClient apiClient = new ApiClient(restTemplate)
      .setBasePath(debtPositionBaseUrl);
    apiClient.setBearerToken(bearerTokenHolder::get);
    this.debtPositionTypeOrgEntityControllerApi = new DebtPositionTypeOrgEntityControllerApi(apiClient);
  }


  @Override
  @Cacheable("debtPositionTypeOrg")
  public DebtPositionTypeOrg getDebtPositionTypeOrgById(Long debtPositionTypeOrgId, String accessToken) {
    bearerTokenHolder.set(accessToken);
    return RestUtil.handleRestExceptionWithResponseEntity(
      () -> debtPositionTypeOrgEntityControllerApi.getItemResourceDebtpositiontypeorgGetWithHttpInfo(String.valueOf(debtPositionTypeOrgId)),
      () -> "getDebtPositionTypeOrgById[%s]".formatted(debtPositionTypeOrgId)
    );
  }

}
