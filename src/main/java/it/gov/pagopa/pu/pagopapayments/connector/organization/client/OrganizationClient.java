package it.gov.pagopa.pu.pagopapayments.connector.organization.client;

import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.organization.dto.generated.OrganizationApiKeyType;
import it.gov.pagopa.pu.organization.dto.generated.OrganizationStationDTO;
import it.gov.pagopa.pu.pagopapayments.connector.organization.config.OrganizationApisHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

@Service
@Slf4j
public class OrganizationClient {

  private final OrganizationApisHolder apisHolder;

  public OrganizationClient(OrganizationApisHolder apisHolder){
    this.apisHolder = apisHolder;
  }

  public Organization getOrganizationById(Long organizationId, String accessToken) {
    try{
      return apisHolder.getOrganizationEntityControllerApi(accessToken)
        .crudGetOrganization(String.valueOf(organizationId));
    } catch (HttpClientErrorException.NotFound e){
      log.info("Cannot find organization having id {}", organizationId);
      return null;
    }
  }

  public Organization getOrganizationByFiscalCode(String organizationFiscalCode, String accessToken) {
    try{
      return apisHolder.getOrganizationSearchControllerApi(accessToken)
        .crudOrganizationsFindByOrgFiscalCode(organizationFiscalCode);
    } catch (HttpClientErrorException.NotFound e){
      log.info("Cannot find organization having fiscalCode {}", organizationFiscalCode);
      return null;
    }
  }

  public String getOrganizationApiKey(Long organizationId,
    OrganizationApiKeyType organizationApiKeyType, String accessToken) {
    try{
      return apisHolder.getOrganizationApi(accessToken)
        .getOrganizationApiKey(organizationId, organizationApiKeyType);
    } catch (HttpClientErrorException.NotFound e){
      log.info("Cannot find organization having organizationId {}", organizationId);
      return null;
    }
  }

  public OrganizationStationDTO getOrganizationStationDTO(Long organizationId, String stationId, String accessToken) {
    try{
      return apisHolder.getOrganizationApi(accessToken)
        .getOrganizationStation(organizationId, stationId);
    } catch (HttpClientErrorException.NotFound e){
      log.info("Cannot find organization_station having organizationId {} and stationId {}", organizationId, stationId);
      return null;
    }
  }
}
