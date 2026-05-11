package it.gov.pagopa.pu.pagopapayments.connector.organization;

import it.gov.pagopa.pu.organization.dto.generated.Station;

public interface StationService {
  Station getStationByBrokerIdAndStationId(Long brokerId, String stationId, String accessToken);
  Station getStationByBrokerIdAndBroadcastStationId(Long brokerId, String broadcastStationId, String accessToken);
}
