package it.gov.pagopa.pu.pagopapayments.connector;

import it.gov.pagopa.nodo.pacreateposition.controller.ApiClient;
import it.gov.pagopa.nodo.pacreateposition.controller.auth.ApiKeyAuth;
import it.gov.pagopa.nodo.pacreateposition.controller.auth.Authentication;
import it.gov.pagopa.nodo.pacreateposition.controller.generated.AcaApi;
import it.gov.pagopa.nodo.pacreateposition.dto.generated.DebtPositionResponse;
import it.gov.pagopa.nodo.pacreateposition.dto.generated.NewDebtPositionRequest;
import it.gov.pagopa.pu.pagopapayments.util.RestUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.function.Supplier;

@Service
@Slf4j
public class AcaClientImpl implements AcaClient {

  private final ThreadLocal<String> apiKeyHolder = new ThreadLocal<>();
  private final AcaApi acaApi;

  public AcaClientImpl(
    @Value("${app.aca.base-url}") String acaBaseUrl,
    RestTemplateBuilder restTemplateBuilder){
    ApiClientWrapper apiClient = new ApiClientWrapper(restTemplateBuilder.build());
    apiClient.setBasePath(acaBaseUrl);
    apiClient.setApiKey(apiKeyHolder::get);
    this.acaApi = new AcaApi(apiClient);
  }

  @Override
  public DebtPositionResponse paCreatePosition(NewDebtPositionRequest request, String apiKey, String segregationCodes){
    apiKeyHolder.set(apiKey);

    return RestUtil.handleRestException(
      () -> acaApi.newDebtPosition(request, segregationCodes),
      () -> "paCreatePosition [%s/%s]".formatted(request.getEntityFiscalCode(), request.getNav()) ,
      true
    );
  }

}

class ApiClientWrapper extends ApiClient{

  private Supplier<String> apiKeySupplier;

  public ApiClientWrapper(RestTemplate restTemplate) {
    super(restTemplate);
  }

  public void setApiKey(Supplier<String> apiKeySupplier) {
    this.apiKeySupplier = apiKeySupplier;
  }

  @Override
  protected void updateParamsForAuth(String[] authNames, MultiValueMap<String, String> queryParams, HttpHeaders headerParams, MultiValueMap<String, String> cookieParams) {
    if(this.apiKeySupplier != null) {
      for (String authName : authNames) {
        Authentication auth = getAuthentications().get(authName);
        if (auth == null) {
          throw new RestClientException("Authentication undefined: " + authName);
        }
        if(auth instanceof ApiKeyAuth) {
          ApiKeyAuth apiKeyAuthLocal = new ApiKeyAuth(((ApiKeyAuth)auth).getLocation(), ((ApiKeyAuth)auth).getParamName());
          apiKeyAuthLocal.setApiKey(apiKeySupplier.get());
          auth = apiKeyAuthLocal;
        }
        auth.applyToParams(queryParams, headerParams, cookieParams);
      }
    } else {
      super.updateParamsForAuth(authNames, queryParams, headerParams, cookieParams);
    }
  }

}
