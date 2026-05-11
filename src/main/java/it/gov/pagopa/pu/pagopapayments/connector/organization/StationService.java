package it.gov.pagopa.pu.pagopapayments.connector.organization;

import it.gov.pagopa.pu.organization.dto.generated.Station;

import java.util.List;

public interface StationService {
  Station getStationByBrokerIdAndStationId(Long brokerId, String stationId, String accessToken);
  List<Station> getStationByBrokerIdAndBroadcastStationId(Long brokerId, String broadcastStationId, String accessToken);
}
