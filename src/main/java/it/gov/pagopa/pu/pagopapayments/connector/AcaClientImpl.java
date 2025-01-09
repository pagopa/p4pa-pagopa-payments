package it.gov.pagopa.pu.pagopapayments.connector;

import it.gov.pagopa.nodo.pacreateposition.controller.ApiClient;
import it.gov.pagopa.nodo.pacreateposition.controller.generated.AcaApi;
import it.gov.pagopa.nodo.pacreateposition.dto.generated.DebtPositionResponse;
import it.gov.pagopa.nodo.pacreateposition.dto.generated.NewDebtPositionRequest;
import it.gov.pagopa.pu.pagopapayments.util.RestUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class AcaClientImpl implements AcaClient {

  private final RestTemplateBuilder restTemplateBuilder;
  private final String acaBaseUrl;
  private final Map<String, AcaApi> acaApiMap = new ConcurrentHashMap<>();

  public AcaClientImpl(
    @Value("${rest.node.aca.base-url}") String acaBaseUrl,
    RestTemplateBuilder restTemplateBuilder){
    this.restTemplateBuilder = restTemplateBuilder;
    this.acaBaseUrl = acaBaseUrl;
  }

  private AcaApi getAcaApiClientByApiKey(String apiKey) {
    return acaApiMap.computeIfAbsent(apiKey, key -> {
      ApiClient apiClient = new ApiClient(restTemplateBuilder.build());
      apiClient.setBasePath(acaBaseUrl);
      apiClient.setApiKey(key);
      return new AcaApi(apiClient);
    });
  }

  @Override
  public DebtPositionResponse paCreatePosition(NewDebtPositionRequest request, String apiKey, String segregationCodes) {
    //get the correct AcaApi linked to this apiKey
    AcaApi acaApi = getAcaApiClientByApiKey(apiKey);

    return RestUtil.handleRestException(
      () -> acaApi.newDebtPosition(request, segregationCodes),
      "paCreatePosition [%s/%s]".formatted(request.getEntityFiscalCode(), request.getNav()),
      true
    );
  }

}
