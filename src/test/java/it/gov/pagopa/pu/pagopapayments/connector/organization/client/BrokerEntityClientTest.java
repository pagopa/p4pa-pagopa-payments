package it.gov.pagopa.pu.pagopapayments.connector.organization.client;

import it.gov.pagopa.pu.organization.client.generated.BrokerApi;
import it.gov.pagopa.pu.organization.client.generated.BrokerEntityControllerApi;
import it.gov.pagopa.pu.organization.client.generated.BrokerSearchControllerApi;
import it.gov.pagopa.pu.organization.dto.generated.Broker;
import it.gov.pagopa.pu.organization.dto.generated.BrokerApiKeyType;
import it.gov.pagopa.pu.organization.dto.generated.BrokerApiKeys;
import it.gov.pagopa.pu.pagopapayments.connector.organization.config.OrganizationApisHolder;
import it.gov.pagopa.pu.pagopapayments.exception.common.RestInvokeNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import static org.mockito.Mockito.when;

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
  void init() {
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

    when(organizationApisHolder.getBrokerApi(accessToken))
      .thenReturn(brokerApiMock);
    when(brokerApiMock.getBrokerApiKeys(brokerId))
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

    when(organizationApisHolder.getBrokerApi(accessToken))
      .thenReturn(brokerApiMock);
    when(brokerApiMock.getBrokerApiKeys(brokerId))
      .thenThrow(new RestInvokeNotFoundException("APPNAME", HttpStatus.NOT_FOUND, "ERROR", "ERRORCODE", "ERRORMESSAGE"));

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

    when(organizationApisHolder.getBrokerEntityControllerApi(accessToken))
      .thenReturn(brokerEntityControllerApiMock);
    when(brokerEntityControllerApiMock.crudGetBroker(brokerId.toString()))
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

    when(organizationApisHolder.getBrokerEntityControllerApi(accessToken))
      .thenReturn(brokerEntityControllerApiMock);
    when(brokerEntityControllerApiMock.crudGetBroker(brokerId.toString()))
      .thenThrow(new RestInvokeNotFoundException("APPNAME", HttpStatus.NOT_FOUND, "ERROR", "ERRORCODE", "ERRORMESSAGE"));

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

    when(organizationApisHolder.getBrokerApi(accessToken))
      .thenReturn(brokerApiMock);
    when(brokerApiMock.getBrokerApiKey(brokerId, BrokerApiKeyType.GENERATE_NOTICE))
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

    when(organizationApisHolder.getBrokerApi(accessToken))
      .thenReturn(brokerApiMock);
    when(brokerApiMock.getBrokerApiKey(brokerId, BrokerApiKeyType.GENERATE_NOTICE))
      .thenThrow(new RestInvokeNotFoundException("APPNAME", HttpStatus.NOT_FOUND, "ERROR", "ERRORCODE", "ERRORMESSAGE"));

    // When
    String result = brokerClient.getBrokerApiKey(brokerId, BrokerApiKeyType.GENERATE_NOTICE, accessToken);

    // Then
    Assertions.assertNull(result);
  }

  @Test
  void whenGetBrokerByBrokerFiscalCodeThenInvokeWithAccessToken() {
    // Given
    String brokerFiscalCode = "brokerFiscalCode";
    String accessToken = "ACCESSTOKEN";
    Broker expectedResult = new Broker();

    when(organizationApisHolder.getBrokerSearchControllerApi(accessToken))
      .thenReturn(brokerSearchControllerApiMock);
    when(brokerSearchControllerApiMock.crudBrokersFindByBrokerFiscalCode(brokerFiscalCode))
      .thenReturn(expectedResult);

    // When
    Broker result = brokerClient.getBrokerByBrokerFiscalCode(brokerFiscalCode, accessToken);

    // Then
    Assertions.assertSame(expectedResult, result);
  }

  @Test
  void givenNoExistentBrokerFiscalCodeWhenGetBrokerByBrokerFiscalCodeThenNotFound() {
    // Given
    String brokerFiscalCode = "brokerFiscalCode";
    String accessToken = "ACCESSTOKEN";

    when(organizationApisHolder.getBrokerSearchControllerApi(accessToken))
      .thenReturn(brokerSearchControllerApiMock);
    when(brokerSearchControllerApiMock.crudBrokersFindByBrokerFiscalCode(brokerFiscalCode))
      .thenThrow(new RestInvokeNotFoundException("APPNAME", HttpStatus.NOT_FOUND, "ERROR", "ERRORCODE", "ERRORMESSAGE"));

    // When
    Broker result = brokerClient.getBrokerByBrokerFiscalCode(brokerFiscalCode, accessToken);

    // Then
    Assertions.assertNull(result);
  }
}
