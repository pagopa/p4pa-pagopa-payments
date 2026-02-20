package it.gov.pagopa.pu.pagopapayments.connector.organization.client;

import it.gov.pagopa.pu.organization.controller.generated.BrokerApi;
import it.gov.pagopa.pu.organization.controller.generated.BrokerEntityControllerApi;
import it.gov.pagopa.pu.organization.controller.generated.BrokerSearchControllerApi;
import it.gov.pagopa.pu.organization.dto.generated.Broker;
import it.gov.pagopa.pu.organization.dto.generated.BrokerApiKeyType;
import it.gov.pagopa.pu.organization.dto.generated.BrokerApiKeys;
import it.gov.pagopa.pu.pagopapayments.connector.organization.config.OrganizationApisHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

@ExtendWith(MockitoExtension.class)
class BrokerEntityClientTest {
  @Mock
  private OrganizationApisHolder organizationApisHolder;
  @Mock
  private BrokerEntityControllerApi brokerEntityControllerApiMock;
  @Mock
  private BrokerApi brokerApiMock;
  @Mock
  private BrokerSearchControllerApi brokerSearchControllerApiMock;

  private BrokerClient brokerClient;

  @BeforeEach
  void setUp() {
    brokerClient = new BrokerClient(organizationApisHolder);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      organizationApisHolder,
      brokerEntityControllerApiMock,
      brokerApiMock,
      brokerSearchControllerApiMock
    );
  }

  @Test
  void whenGetApiKeyByBrokerIdThenInvokeWithAccessToken() {
    // Given
    Long brokerId = 0L;
    String accessToken = "ACCESSTOKEN";
    BrokerApiKeys expectedResult = new BrokerApiKeys();

    Mockito.when(organizationApisHolder.getBrokerApi(accessToken))
      .thenReturn(brokerApiMock);
    Mockito.when(brokerApiMock.getBrokerApiKeys(brokerId))
      .thenReturn(expectedResult);

    // When
    BrokerApiKeys result = brokerClient.getApiKeyByBrokerId(brokerId, accessToken);

    // Then
    Assertions.assertSame(expectedResult, result);
  }

  @Test
  void givenNoExistentBrokerIdWhenApiKeyByBrokerIdIdThenNull() {
    // Given
    Long brokerId = 0L;
    String accessToken = "ACCESSTOKEN";

    Mockito.when(organizationApisHolder.getBrokerApi(accessToken))
      .thenReturn(brokerApiMock);
    Mockito.when(brokerApiMock.getBrokerApiKeys(brokerId))
      .thenThrow(HttpClientErrorException.create(HttpStatus.NOT_FOUND, "NotFound", null, null, null));

    // When
    BrokerApiKeys result = brokerClient.getApiKeyByBrokerId(brokerId, accessToken);

    // Then
    Assertions.assertNull(result);
  }

  @Test
  void whenGetBrokerByIdThenInvokeWithAccessToken() {
    // Given
    Long brokerId = 0L;
    String accessToken = "ACCESSTOKEN";
    Broker expectedResult = new Broker();

    Mockito.when(organizationApisHolder.getBrokerEntityControllerApi(accessToken))
      .thenReturn(brokerEntityControllerApiMock);
    Mockito.when(brokerEntityControllerApiMock.crudGetBroker(brokerId.toString()))
      .thenReturn(expectedResult);

    // When
    Broker result = brokerClient.getBrokerById(brokerId, accessToken);

    // Then
    Assertions.assertSame(expectedResult, result);
  }

  @Test
  void givenNoExistentBrokerIdWhenGetBrokerByIdThenNull() {
    // Given
    Long brokerId = 0L;
    String accessToken = "ACCESSTOKEN";

    Mockito.when(organizationApisHolder.getBrokerEntityControllerApi(accessToken))
      .thenReturn(brokerEntityControllerApiMock);
    Mockito.when(brokerEntityControllerApiMock.crudGetBroker(brokerId.toString()))
      .thenThrow(HttpClientErrorException.create(HttpStatus.NOT_FOUND, "NotFound", null, null, null));

    // When
    Broker result = brokerClient.getBrokerById(brokerId, accessToken);

    // Then
    Assertions.assertNull(result);
  }

  @Test
  void whenGetBrokerApiKeyThenInvokeWithAccessToken() {
    // Given
    Long brokerId = 1L;
    String accessToken = "ACCESSTOKEN";
    String expectedResult = "apiKey";

    Mockito.when(organizationApisHolder.getBrokerApi(accessToken))
      .thenReturn(brokerApiMock);
    Mockito.when(brokerApiMock.getBrokerApiKey(brokerId, BrokerApiKeyType.GENERATE_NOTICE))
      .thenReturn(expectedResult);

    // When
    String result = brokerClient.getBrokerApiKey(brokerId, BrokerApiKeyType.GENERATE_NOTICE, accessToken);

    // Then
    Assertions.assertSame(expectedResult, result);
  }

  @Test
  void givenNoExistentBrokerIdWhenGetBrokerApiKeyThenNotFound() {
    // Given
    Long brokerId = 0L;
    String accessToken = "ACCESSTOKEN";

    Mockito.when(organizationApisHolder.getBrokerApi(accessToken))
      .thenReturn(brokerApiMock);
    Mockito.when(brokerApiMock.getBrokerApiKey(brokerId, BrokerApiKeyType.GENERATE_NOTICE))
      .thenThrow(HttpClientErrorException.create(HttpStatus.NOT_FOUND, "NotFound", null, null, null));

    // When
    String result = brokerClient.getBrokerApiKey(brokerId, BrokerApiKeyType.GENERATE_NOTICE, accessToken);

    // Then
    Assertions.assertNull(result);
  }

  @Test
  void whenGetBrokerByStationIdThenInvokeWithAccessToken() {
    // Given
    String stationId = "32685440409_01";
    String accessToken = "ACCESSTOKEN";
    Broker expectedResult = new Broker();

    Mockito.when(organizationApisHolder.getBrokerSearchControllerApi(accessToken))
      .thenReturn(brokerSearchControllerApiMock);
    Mockito.when(brokerSearchControllerApiMock.crudBrokersFindByStationId(stationId))
      .thenReturn(expectedResult);

    // When
    Broker result = brokerClient.getBrokerByStationId(stationId, accessToken);

    // Then
    Assertions.assertSame(expectedResult, result);
  }

  @Test
  void givenNoExistentStationIdWhenGetBrokerByStationIdThenNotFound() {
    // Given
    String stationId = "32685441234_01";
    String accessToken = "ACCESSTOKEN";

    Mockito.when(organizationApisHolder.getBrokerSearchControllerApi(accessToken))
      .thenReturn(brokerSearchControllerApiMock);
    Mockito.when(brokerSearchControllerApiMock.crudBrokersFindByStationId(stationId))
      .thenThrow(HttpClientErrorException.create(HttpStatus.NOT_FOUND, "NotFound", null, null, null));

    // When
    Broker result = brokerClient.getBrokerByStationId(stationId, accessToken);

    // Then
    Assertions.assertNull(result);
  }
}
