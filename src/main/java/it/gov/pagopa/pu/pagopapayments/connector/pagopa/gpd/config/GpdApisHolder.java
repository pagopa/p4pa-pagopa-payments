package it.gov.pagopa.pu.pagopapayments.connector.pagopa.gpd.config;

import it.gov.pagopa.pu.pagopapayments.connector.pagopa.aca_gpd.BaseApisHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class GpdApisHolder extends BaseApisHolder {

  public GpdApisHolder(
    GpdApiClientConfig clientConfig,
    RestTemplateBuilder restTemplateBuilder
  ) {
    super(
      restTemplateBuilder,
      clientConfig.getBaseUrl(),
      clientConfig.getMaxAttempts(),
      clientConfig.getWaitTimeMillis(),
      clientConfig.isPrintBodyWhenError(),
      "GPD"
    );
  }
}
