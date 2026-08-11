package it.gov.pagopa.pu.pagopapayments.connector.organization.client;

import it.gov.pagopa.pu.organization.client.generated.StationSearchControllerApi;
import it.gov.pagopa.pu.organization.dto.generated.CollectionModelStation;
import it.gov.pagopa.pu.organization.dto.generated.PagedModelStationEmbedded;
import it.gov.pagopa.pu.organization.dto.generated.Station;
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

import java.util.List;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StationClientTest {
  @Mock
  private OrganizationApisHolder organizationApisHolder;
  @Mock
  private StationSearchControllerApi stationSearchControllerApiMock;

  private StationClient stationClient;

  @BeforeEach
  void init() {
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

    when(organizationApisHolder.getStationSearchControllerApi(accessToken))
      .thenReturn(stationSearchControllerApiMock);
    when(stationSearchControllerApiMock.crudStationsFindByBrokerIdAndStationId(brokerId, stationId))
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

    when(organizationApisHolder.getStationSearchControllerApi(accessToken))
      .thenReturn(stationSearchControllerApiMock);
    when(stationSearchControllerApiMock.crudStationsFindByBrokerIdAndStationId(brokerId, stationId))
      .thenThrow(new RestInvokeNotFoundException("APPNAME", HttpStatus.NOT_FOUND, "ERROR", "ERRORCODE", "ERRORMESSAGE"));

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

    List<Station> expectedStationList = List.of(new Station());
    PagedModelStationEmbedded pagedModelStationEmbedded = new PagedModelStationEmbedded();
    pagedModelStationEmbedded.setStations(expectedStationList);
    CollectionModelStation collectionModelStation = new CollectionModelStation();
    collectionModelStation.setEmbedded(pagedModelStationEmbedded);

    when(organizationApisHolder.getStationSearchControllerApi(accessToken))
      .thenReturn(stationSearchControllerApiMock);
    when(stationSearchControllerApiMock.crudStationsFindByBrokerIdAndBroadcastStationId(brokerId, broadcastStationId))
      .thenReturn(collectionModelStation);

    // When
    List<Station> result = stationClient.getStationByBrokerIdAndBroadcastStationId(brokerId, broadcastStationId, accessToken);

    // Then
    Assertions.assertEquals(expectedStationList, result);
  }
}
