package it.gov.pagopa.pu.pagopapayments.connector;

import it.gov.pagopa.nodo.paCreatePosition.controller.ApiClient;
import it.gov.pagopa.nodo.paCreatePosition.controller.generated.AcaApi;
import it.gov.pagopa.nodo.paCreatePosition.dto.generated.DebtPositionResponse;
import it.gov.pagopa.nodo.paCreatePosition.dto.generated.NewDebtPositionRequest;
import it.gov.pagopa.pu.p4pa_organization.dto.generated.EntityModelOrganization;
import it.gov.pagopa.pu.pagopapayments.util.RestUtil;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class AcaClientImpl implements AcaClient {

  @Value("${app.aca.base-url}")
  private String acaBaseUrl;

  private final RestTemplate restTemplate;

  private final OrganizationClient organizationClient;

  private ThreadLocal<ApiClient> apiClientThreadLocal;
  private AcaApi acaApi;

  public AcaClientImpl(
    RestTemplateBuilder restTemplateBuilder,
    OrganizationClient organizationClient){
    this.restTemplate = restTemplateBuilder.build();
    this.organizationClient = organizationClient;
  }

  @PostConstruct
  void init(){
    this.apiClientThreadLocal = ThreadLocal.withInitial( () -> new ApiClient(restTemplate).setBasePath(acaBaseUrl));
    this.acaApi = new AcaApi();
  }

  @Override
  public DebtPositionResponse paCreatePosition(Long organizationId, NewDebtPositionRequest request){
    EntityModelOrganization organization = organizationClient.getOrganizationById(organizationId);
    //TODO evaluate if implement here some caching mechanism for retrieving API KEY (as they are some sort of static data)
    String apiKey = organizationClient.getAcaApiKeyByBrokerId(organization.getBrokerId());
    ApiClient apiClient = apiClientThreadLocal.get();
    apiClient.setApiKey(apiKey);
    acaApi.setApiClient(apiClient);

    String segregationCodes = organization.getApplicationCode();
    return RestUtil.handleRestException(
      () -> acaApi.newDebtPosition(request, segregationCodes),
      () -> "paCreatePosition [%s/%s]".formatted(request.getEntityFiscalCode(), request.getNav()) ,
      true
    );
  }

}
