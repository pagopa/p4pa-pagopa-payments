package it.gov.pagopa.pu.pagopapayments.connector;

import it.gov.pagopa.nodo.pacreateposition.controller.ApiClient;
import it.gov.pagopa.nodo.pacreateposition.controller.generated.AcaApi;
import it.gov.pagopa.nodo.pacreateposition.dto.generated.DebtPositionResponse;
import it.gov.pagopa.nodo.pacreateposition.dto.generated.NewDebtPositionRequest;
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

  private final String acaBaseUrl;

  private final RestTemplate restTemplate;

  private ThreadLocal<ApiClient> apiClientThreadLocal;
  private AcaApi acaApi;

  public AcaClientImpl(
    @Value("${app.aca.base-url}") String acaBaseUrl,
    RestTemplateBuilder restTemplateBuilder){
    this.acaBaseUrl = acaBaseUrl;
    this.restTemplate = restTemplateBuilder.build();
  }

  @PostConstruct
  void init(){
    this.apiClientThreadLocal = ThreadLocal.withInitial( () -> new ApiClient(restTemplate).setBasePath(acaBaseUrl));
    this.acaApi = new AcaApi();
  }

  @Override
  public DebtPositionResponse paCreatePosition(NewDebtPositionRequest request, String apiKey, String segregationCodes){
    ApiClient apiClient = apiClientThreadLocal.get();
    apiClient.setApiKey(apiKey);
    acaApi.setApiClient(apiClient);

    return RestUtil.handleRestException(
      () -> acaApi.newDebtPosition(request, segregationCodes),
      () -> "paCreatePosition [%s/%s]".formatted(request.getEntityFiscalCode(), request.getNav()) ,
      true
    );
  }

}
