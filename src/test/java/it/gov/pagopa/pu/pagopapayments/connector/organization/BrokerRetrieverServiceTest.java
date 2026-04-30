package it.gov.pagopa.pu.pagopapayments.connector.organization;

import it.gov.pagopa.pu.organization.dto.generated.Broker;
import it.gov.pagopa.pu.organization.dto.generated.BrokerApiKeyType;
import it.gov.pagopa.pu.organization.dto.generated.BrokerApiKeys;
import it.gov.pagopa.pu.pagopapayments.connector.organization.client.BrokerClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BrokerRetrieverServiceTest {

  @Mock
  private BrokerClient client;

  private BrokerService service;

  @BeforeEach
  void setUp() {
    service = new BrokerServiceImpl(client);
  }

  @Test
  void testGetBrokerById() {
    // Given
    Long brokerId = 1L;
    String accessToken = "accessToken";
    Broker expected = new Broker();

    when(client.getBrokerById(brokerId, accessToken)).thenReturn(expected);

    // When
    Broker result = service.getBrokerById(brokerId, accessToken);

    // Then
    assertSame(expected, result);
  }

  @Test
  void testGetApiKeyByBrokerId() {
    // Given
    Long brokerId = 1L;
    String accessToken = "accessToken";
    BrokerApiKeys expected = new BrokerApiKeys();

    when(client.getApiKeyByBrokerId(brokerId, accessToken)).thenReturn(expected);

    // When
    BrokerApiKeys result = service.getApiKeyByBrokerId(brokerId, accessToken);

    // Then
    assertSame(expected, result);
  }

  @Test
  void givenBrokerIdWhenGetBrokerApiKeyThenOk() {
    // Given
    Long brokerId = 1L;
    String accessToken = "accessToken";
    String expected = "apiKey";

    when(client.getBrokerApiKey(brokerId, BrokerApiKeyType.GENERATE_NOTICE, accessToken)).thenReturn(expected);

    // When
    String result = service.getBrokerApiKey(brokerId, BrokerApiKeyType.GENERATE_NOTICE, accessToken);

    // Then
    assertSame(expected, result);
  }

  @Test
  void givenBrokerFiscalCodeWhenGetBrokerByBrokerFiscalCodeThenOk() {
    // Given
    String brokerFiscalCode = "brokerFiscalCode";
    String accessToken = "accessToken";
    Broker expected = new Broker();

    when(client.getBrokerByBrokerFiscalCode(brokerFiscalCode, accessToken))
      .thenReturn(expected);

    // When
    Broker result = service.getBrokerByBrokerFiscalCode(brokerFiscalCode, accessToken);

    // Then
    assertSame(expected, result);
  }
}
