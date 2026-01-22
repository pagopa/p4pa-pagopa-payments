package it.gov.pagopa.pu.pagopapayments.config;

import it.gov.pagopa.pu.pagopapayments.connector.soap.NodeForPaClient;
import it.gov.pagopa.pu.pagopapayments.connector.soap.NodeForPaClientImpl;
import it.gov.pagopa.pu.pagopapayments.connector.soap.mapper.NodoChiediFlussoRendicontazioneMapper;
import it.gov.pagopa.pu.pagopapayments.mapper.PaymentsReportingMapper;
import it.gov.pagopa.pu.pagopapayments.registry.RegistryLogger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

@Configuration
@Slf4j
public class SoapWebServiceClientConfig {

  @Value("${soap.pagopa-payments.node-for-pa-url}")
  private String nodeForPaUrl;

  @Bean
  public NodeForPaClient getNodeForPaClient(NodoChiediFlussoRendicontazioneMapper mapper, RegistryLogger registryLogger, PaymentsReportingMapper paymentsReportingMapper) {
    Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
    // this package must match the package in the <jaxbJavaGenXXXX> task specified in build.gradle.kts
    // (i.e. in the XXX.xjb file corresponding to the WSDL)
    marshaller.setContextPath("gov.telematici.pagamenti.ws");

    return new NodeForPaClientImpl(nodeForPaUrl, marshaller, mapper, registryLogger, paymentsReportingMapper);
  }

}
