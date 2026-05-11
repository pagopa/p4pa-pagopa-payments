package it.gov.pagopa.pu.pagopapayments.connector.organization;

import it.gov.pagopa.pu.organization.dto.generated.Station;
import it.gov.pagopa.pu.pagopapayments.connector.organization.client.StationClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class StationServiceTest {
  @Mock
  private StationClient client;

  private StationService service;

  @BeforeEach
  void setUp() {
    service = new StationServiceImpl(client);
  }

  @Test
  void whenGetStationByBrokerIdAndStationIdThenInvokeClient(){
    Long brokerId = 1L;
    String stationId = "STATIONID";
    String accessToken = "ACCESSTOKEN";
    Station expectedStation = new Station();

    Mockito.when(client.getStationByBrokerIdAndStationId(brokerId, stationId, accessToken))
      .thenReturn(expectedStation);

    Station result = service.getStationByBrokerIdAndStationId(brokerId, stationId, accessToken);

    assertEquals(expectedStation, result);
  }

  @Test
  void whenGetStationByBrokerIdAndBroadcastStationIdThenInvokeClient(){
    Long brokerId = 1L;
    String broadcastStationId = "BROADCASTSTATIONID";
    String accessToken = "accessToken";
    List<Station> expectedStationList = List.of(new Station());

    Mockito.when(client.getStationByBrokerIdAndBroadcastStationId(brokerId, broadcastStationId, accessToken))
      .thenReturn(expectedStationList);

    List<Station> result = service.getStationByBrokerIdAndBroadcastStationId(brokerId, broadcastStationId, accessToken);

    assertEquals(expectedStationList, result);
  }
}
