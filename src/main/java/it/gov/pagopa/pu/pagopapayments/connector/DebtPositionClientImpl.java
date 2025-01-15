package it.gov.pagopa.pu.pagopapayments.connector;

import it.gov.pagopa.pu.debtpositions.controller.ApiClient;
import it.gov.pagopa.pu.debtpositions.controller.generated.DebtPositionEntityControllerApi;
import it.gov.pagopa.pu.debtpositions.controller.generated.DebtPositionTypeOrgEntityControllerApi;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.pagopapayments.util.RestUtil;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@Slf4j
public class DebtPositionClientImpl implements DebtPositionClient {

  private final DebtPositionEntityControllerApi debtPositionEntityControllerApi;
  private final DebtPositionTypeOrgEntityControllerApi debtPositionTypeOrgEntityControllerApi;
  private final ThreadLocal<String> bearerTokenHolder = new ThreadLocal<>();

  public DebtPositionClientImpl(@Value("${rest.debt-position.base-url}") String debtPositionBaseUrl,
                                RestTemplateBuilder restTemplateBuilder){
    RestTemplate restTemplate = restTemplateBuilder.build();
    ApiClient apiClient = new ApiClient(restTemplate)
      .setBasePath(debtPositionBaseUrl);
    apiClient.setBearerToken(bearerTokenHolder::get);
    this.debtPositionTypeOrgEntityControllerApi = new DebtPositionTypeOrgEntityControllerApi(apiClient);
    this.debtPositionEntityControllerApi = new DebtPositionEntityControllerApi(apiClient);
  }

  @PreDestroy
  public void unload(){
    bearerTokenHolder.remove();
  }

  @Override
  @Cacheable("debtPositionTypeOrg")
  public DebtPositionTypeOrg getDebtPositionTypeOrgById(Long debtPositionTypeOrgId, String accessToken) {
    bearerTokenHolder.set(accessToken);
    return RestUtil.handleRestException(
      () -> debtPositionTypeOrgEntityControllerApi.crudGetDebtpositiontypeorg(String.valueOf(debtPositionTypeOrgId)),
      "getDebtPositionTypeOrgById[%s]".formatted(debtPositionTypeOrgId)
    );
  }

  @Override
  public List<InstallmentDTO> getDebtPositionsByOrganizationIdAndNav(Long organizationId, String nav, String accessToken) {
    return List.of(); //TODO
  }
}
