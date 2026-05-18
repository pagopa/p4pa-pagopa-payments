package it.gov.pagopa.pu.pagopapayments.connector.organization;

import it.gov.pagopa.pu.organization.dto.generated.Station;
import it.gov.pagopa.pu.pagopapayments.connector.organization.client.StationClient;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StationServiceImpl implements StationService {

  private final StationClient stationClient;

  public StationServiceImpl(StationClient stationClient) {
    this.stationClient = stationClient;
  }

  @Override
  public Station getStationByBrokerIdAndStationId(Long brokerId, String stationId, String accessToken) {
    return stationClient.getStationByBrokerIdAndStationId(brokerId, stationId, accessToken);
  }

  @Override
  public List<Station> getStationByBrokerIdAndBroadcastStationId(Long brokerId, String broadcastStationId, String accessToken) {
    return stationClient.getStationByBrokerIdAndBroadcastStationId(brokerId, broadcastStationId, accessToken);
  }
}
