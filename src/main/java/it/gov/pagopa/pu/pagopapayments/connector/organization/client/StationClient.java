package it.gov.pagopa.pu.pagopapayments.connector.organization.client;

import it.gov.pagopa.pu.organization.dto.generated.CollectionModelStation;
import it.gov.pagopa.pu.organization.dto.generated.PagedModelStationEmbedded;
import it.gov.pagopa.pu.organization.dto.generated.Station;
import it.gov.pagopa.pu.pagopapayments.connector.organization.config.OrganizationApisHolder;
import it.gov.pagopa.pu.pagopapayments.exception.common.RestInvokeNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class StationClient {

  private final OrganizationApisHolder apisHolder;

  public StationClient(OrganizationApisHolder apisHolder){
    this.apisHolder = apisHolder;
  }

  public Station getStationByBrokerIdAndStationId(Long brokerId, String stationId, String accessToken) {
    try {
      return apisHolder.getStationSearchControllerApi(accessToken)
        .crudStationsFindByBrokerIdAndStationId(brokerId, stationId);
    } catch (RestInvokeNotFoundException e){
      log.info("Cannot find Station having brokerId {} and stationId {}", brokerId, stationId);
      return null;
    }
  }

  public List<Station> getStationByBrokerIdAndBroadcastStationId(Long brokerId, String broadcastStationId, String accessToken) {
    CollectionModelStation collectionModelStation = apisHolder.getStationSearchControllerApi(accessToken)
      .crudStationsFindByBrokerIdAndBroadcastStationId(brokerId, broadcastStationId);

    return Optional.ofNullable(collectionModelStation)
      .map(CollectionModelStation::getEmbedded)
      .map(PagedModelStationEmbedded::getStations)
      .orElse(Collections.emptyList());
  }
}
