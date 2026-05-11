package it.gov.pagopa.pu.pagopapayments.connector.organization.client;

import it.gov.pagopa.pu.organization.controller.generated.StationSearchControllerApi;
import it.gov.pagopa.pu.organization.dto.generated.Station;
import it.gov.pagopa.pu.pagopapayments.connector.organization.config.OrganizationApisHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.HttpClientErrorException;

@ExtendWith(MockitoExtension.class)
class StationClientTest {
  @Mock
  private OrganizationApisHolder organizationApisHolder;
  @Mock
  private StationSearchControllerApi stationSearchControllerApiMock;

  private StationClient stationClient;

  @BeforeEach
  void setUp() {
    stationClient = new StationClient(organizationApisHolder);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      organizationApisHolder,
      stationSearchControllerApiMock
    );
  }

  @Test
  void whenGetStationByBrokerIdAndStationIdThenInvokeWithAccessToken() {
    // Given
    Long brokerId = 1L;
    String stationId = "STATIONID";
    String accessToken = "ACCESSTOKEN";
    Station expectedResult = new Station();

    Mockito.when(organizationApisHolder.getStationSearchControllerApi(accessToken))
      .thenReturn(stationSearchControllerApiMock);
    Mockito.when(stationSearchControllerApiMock.crudStationsFindByBrokerIdAndStationId(brokerId, stationId))
      .thenReturn(expectedResult);

    // When
    Station result = stationClient.getStationByBrokerIdAndStationId(brokerId, stationId, accessToken);

    // Then
    Assertions.assertSame(expectedResult, result);
  }

  @Test
  void givenNotFoundExceptionWhenGetStationByBrokerIdAndStationIdThenReturnNull() {
    // Given
    Long brokerId = 1L;
    String stationId = "STATIONID";
    String accessToken = "ACCESSTOKEN";

    Mockito.when(organizationApisHolder.getStationSearchControllerApi(accessToken))
      .thenReturn(stationSearchControllerApiMock);
    Mockito.when(stationSearchControllerApiMock.crudStationsFindByBrokerIdAndStationId(brokerId, stationId))
      .thenThrow(Mockito.mock(HttpClientErrorException.NotFound.class));

    // When
    Station result = stationClient.getStationByBrokerIdAndStationId(brokerId, stationId, accessToken);

    // Then
    Assertions.assertNull(result);
  }

  @Test
  void whenGetStationByBrokerIdAndBroadcastStationIdThenInvokeWithAccessToken() {
    // Given
    Long brokerId = 1L;
    String broadcastStationId = "BROADCASTSTATIONID";
    String accessToken = "ACCESSTOKEN";
    Station expectedResult = new Station();

    Mockito.when(organizationApisHolder.getStationSearchControllerApi(accessToken))
      .thenReturn(stationSearchControllerApiMock);
    Mockito.when(stationSearchControllerApiMock.crudStationsFindByBrokerIdAndBroadcastStationId(brokerId, broadcastStationId))
      .thenReturn(expectedResult);

    // When
    Station result = stationClient.getStationByBrokerIdAndBroadcastStationId(brokerId, broadcastStationId, accessToken);

    // Then
    Assertions.assertSame(expectedResult, result);
  }

  @Test
  void givenNotFoundExceptionWhenGetStationByBrokerIdAndBroadcastStationIdThenReturnNull() {
    // Given
    Long brokerId = 1L;
    String broadcastStationId = "BROADCASTSTATIONID";
    String accessToken = "ACCESSTOKEN";

    Mockito.when(organizationApisHolder.getStationSearchControllerApi(accessToken))
      .thenReturn(stationSearchControllerApiMock);
    Mockito.when(stationSearchControllerApiMock.crudStationsFindByBrokerIdAndBroadcastStationId(brokerId, broadcastStationId))
      .thenThrow(Mockito.mock(HttpClientErrorException.NotFound.class));

    // When
    Station result = stationClient.getStationByBrokerIdAndBroadcastStationId(brokerId, broadcastStationId, accessToken);

    // Then
    Assertions.assertNull(result);
  }
}
