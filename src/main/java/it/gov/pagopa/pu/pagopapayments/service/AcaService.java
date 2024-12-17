package it.gov.pagopa.pu.pagopapayments.service;

import it.gov.pagopa.nodo.paCreatePosition.controller.ApiClient;
import it.gov.pagopa.nodo.paCreatePosition.controller.generated.AcaApi;
import it.gov.pagopa.nodo.paCreatePosition.dto.generated.DebtPositionResponse;
import it.gov.pagopa.nodo.paCreatePosition.dto.generated.NewDebtPositionRequest;
import it.gov.pagopa.pu.p4pa_organization.controller.generated.BrokerEntityControllerApi;
import it.gov.pagopa.pu.p4pa_organization.controller.generated.OrganizationEntityControllerApi;
import it.gov.pagopa.pu.p4pa_organization.dto.generated.EntityModelBroker;
import it.gov.pagopa.pu.p4pa_organization.dto.generated.EntityModelOrganization;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class AcaService {

  @Value("${app.aca.base-url}")
  private String acaBaseUrl;

  private final BrokerEntityControllerApi brokerEntityApi;

  private final OrganizationEntityControllerApi organizationEntityApi;

  private final RestTemplate restTemplate;

  private ThreadLocal<ApiClient> apiClientThreadLocal;
  private AcaApi acaApi;

  @PostConstruct
  void init(){
    this.apiClientThreadLocal = ThreadLocal.withInitial( () -> new ApiClient(restTemplate).setBasePath(acaBaseUrl));
    this.acaApi = new AcaApi();
  }

  public void create(){
    Long organizationId = 1L;
    NewDebtPositionRequest request = new NewDebtPositionRequest()
      .nav("301000000000012345")
      .amount(1234);
    invokeAca(organizationId, request);
  }

  public void update(){

  }

  public void delete(){

  }

  //TODO implement some caching mechanism for API KEY
  private String getApiKeyForOrganization(Long organizationId){
    EntityModelOrganization organization = organizationEntityApi.getItemResourceOrganizationGet(""+organizationId);
    EntityModelBroker broker = brokerEntityApi.getItemResourceBrokerGet(""+organization.getBrokerId());
    byte[] encryptedAcaKey = broker.getAcaKey();
    String decryptedAcaKey = "decryptedAcaKey"; //TODO implement decrypt API on broker API
    return decryptedAcaKey;
  }

  private DebtPositionResponse invokeAca(Long organizationId, NewDebtPositionRequest request){
    String apiKey = getApiKeyForOrganization(organizationId);
    ApiClient apiClient = apiClientThreadLocal.get();
    apiClient.setApiKey(apiKey);
    acaApi.setApiClient(apiClient);

    long httpCallStart = System.currentTimeMillis();
    try{
      String segregationCodes = StringUtils.substring(request.getNav(),1,3);
      DebtPositionResponse response = acaApi.newDebtPosition(request, segregationCodes);
      return response;
    } catch (HttpServerErrorException he) {
      int statusCode = he.getStatusCode().value();
      String body = he.getResponseBodyAsString();
      log.error("HttpServerErrorException on invokeAca - returned code[{}] body[{}]", statusCode, body);
      throw he;
    } catch (RestClientException e) {
      log.error("error on invokeAca[{}]", request, e);
      throw e;
    } finally {
      long elapsed = Math.max(0, System.currentTimeMillis() - httpCallStart);
      log.info("elapsed time(ms) for invokeAca: {}", elapsed);
    }
  }
}
