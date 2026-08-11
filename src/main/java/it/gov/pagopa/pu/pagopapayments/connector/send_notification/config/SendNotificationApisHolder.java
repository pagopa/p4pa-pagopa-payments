package it.gov.pagopa.pu.pagopapayments.connector.send_notification.config;

import it.gov.pagopa.pu.pagopapayments.config.rest.HttpClientErrorJsonBodyHandler;
import it.gov.pagopa.pu.sendnotification.generated.ApiClient;
import it.gov.pagopa.pu.sendnotification.generated.BaseApi;
import it.gov.pagopa.pu.sendnotification.client.generated.SendApi;
import it.gov.pagopa.pu.sendnotification.dto.generated.SendNotificationErrorDTO;
import jakarta.annotation.PreDestroy;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.json.JsonMapper;

@Service
public class SendNotificationApisHolder {

    private final SendApi sendApi;
    private final ThreadLocal<String> bearerTokenHolder = new ThreadLocal<>();

    public SendNotificationApisHolder(
        SendNotificationApiClientConfig clientConfig,
        RestTemplateBuilder restTemplateBuilder,
        JsonMapper jsonMapper
    ) {
        RestTemplate restTemplate = restTemplateBuilder.build();
        ApiClient apiClient = new ApiClient(restTemplate);
        apiClient.setBasePath(clientConfig.getBaseUrl());
        apiClient.setBearerToken(bearerTokenHolder::get);
        apiClient.setMaxAttemptsForRetry(Math.max(1, clientConfig.getMaxAttempts()));
        apiClient.setWaitTimeMillis(clientConfig.getWaitTimeMillis());
        restTemplate.setErrorHandler(new HttpClientErrorJsonBodyHandler<>(jsonMapper, "SEND-NOTIFICATION", clientConfig.isPrintBodyWhenError(),
          SendNotificationErrorDTO.class, SendNotificationErrorDTO::getCode, SendNotificationErrorDTO::getMessage)
        );

        this.sendApi = new SendApi(apiClient);
    }

    @PreDestroy
    public void unload(){
        bearerTokenHolder.remove();
    }

    /** It will return a {@link SendApi} instrumented with the provided accessToken. Use null if auth is not required */
    public SendApi getSendApi(String accessToken){
        return getApi(accessToken, sendApi);
    }

    private <T extends BaseApi> T getApi(String accessToken, T api) {
        bearerTokenHolder.set(accessToken);
        return api;
    }
}
