package it.gov.pagopa.pu.pagopapayments.service;

import it.gov.pagopa.pu.organization.dto.generated.Broker;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.organization.dto.generated.OrganizationStatus;
import it.gov.pagopa.pu.pagopapayments.connector.organization.BrokerService;
import it.gov.pagopa.pu.pagopapayments.connector.organization.OrganizationService;
import it.gov.pagopa.pu.pagopapayments.dto.PaForNodeDTO;
import it.gov.pagopa.pu.pagopapayments.dto.PaSendRtDTO;
import it.gov.pagopa.pu.pagopapayments.enums.PagoPaNodeFaults;
import it.gov.pagopa.pu.pagopapayments.exception.PagoPaNodeFaultException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class PaForNodeRequestValidatorService {

  private final BrokerService brokerService;
  private final OrganizationService organizationService;

  public PaForNodeRequestValidatorService(BrokerService brokerService, OrganizationService organizationService) {
    this.brokerService = brokerService;
    this.organizationService = organizationService;
  }

  public Pair<Broker, Organization> paForNodeRequestValidate(PaForNodeDTO request, String accessToken){
    Pair<Broker, Organization> brokerOrgPair = retrieveBrokerAndOrganization(request.getIdBrokerPA(), request.getIdPA(), accessToken);

    Organization organization = brokerOrgPair.getRight();
    if(organization == null) {
      throw new PagoPaNodeFaultException(PagoPaNodeFaults.PAA_ID_DOMINIO_ERRATO, request.getIdBrokerPA());
    }

    Broker broker = brokerOrgPair.getLeft();
    validateOrganizationBrokerAndStation(organization, broker, request);

    return Pair.of(brokerOrgPair.getLeft(), organization);
  }

  public Organization paSendRtRequestValidate(PaSendRtDTO request, String accessToken) {
    Pair<Broker, Organization> brokerOrgPair = retrieveBrokerAndOrganization(request.getIdBrokerPA(), request.getIdPA(), accessToken);

    Organization organization = brokerOrgPair.getRight();
    if(organization == null) {
      // Check if there is at least one organization managed in PU within the transfer list
      boolean hasValidTransferOrg = request.getTransferList().stream()
        .map(transfer -> organizationService.getOrganizationByFiscalCode(transfer.getFiscalCodePA(), accessToken))
        .anyMatch(Objects::nonNull);

      if(!hasValidTransferOrg) {
        throw new PagoPaNodeFaultException(PagoPaNodeFaults.PAA_ID_DOMINIO_ERRATO, request.getIdBrokerPA());
      } else {
        // If at least one transfer is managed, use the technical organization
        organization = organizationService.getOrganizationById(-1L, accessToken);
      }
    }

    validateOrganizationBrokerAndStation(organization, brokerOrgPair.getLeft(), request);

    return organization;
  }

  private Pair<Broker, Organization> retrieveBrokerAndOrganization(String brokerFiscalCode, String orgFiscalCode, String accessToken) {    Broker broker = brokerService.getBrokerByBrokerFiscalCode(brokerFiscalCode, accessToken);
    if (broker == null) {
      throw new PagoPaNodeFaultException(PagoPaNodeFaults.PAA_ID_INTERMEDIARIO_ERRATO, brokerFiscalCode);
    }

    if (Boolean.TRUE.equals(broker.getFlagDelegate())) {
      Organization org = organizationService.getOrganizationById(broker.getOrganizationId(), accessToken);
      if (org == null) {
        throw new PagoPaNodeFaultException(PagoPaNodeFaults.PAA_SYSTEM_ERROR, orgFiscalCode);
      }

      return Pair.of(broker, org);
    }

    Organization organization = organizationService.getOrganizationByFiscalCode(orgFiscalCode, accessToken);

    return Pair.of(broker, organization);
  }

  private void validateOrganizationBrokerAndStation(Organization organization, Broker broker, PaForNodeDTO request) {
    if (!Objects.equals(organization.getStatus(), OrganizationStatus.ACTIVE)) {
      log.warn("paymentRequestValidate [{}/{}]: organization is not active", request.getFiscalCode(), request.getNoticeNumber());
      throw new PagoPaNodeFaultException(PagoPaNodeFaults.PAA_ID_DOMINIO_ERRATO, organization.getOrgFiscalCode());
    }

    if (!Objects.equals(request.getIdBrokerPA(), broker.getBrokerFiscalCode())) {
      log.warn("paymentRequestValidate [{}/{}]: invalid broken for organization expected/actual[{}/{}]",
        request.getFiscalCode(), request.getNoticeNumber(),
        request.getIdBrokerPA(), broker.getBrokerFiscalCode());
      throw new PagoPaNodeFaultException(PagoPaNodeFaults.PAA_ID_INTERMEDIARIO_ERRATO, broker.getBrokerFiscalCode());
    }

    // Sync brokers expects to receive RT on stationId, async brokers expects to receive RT on broadcastStationId. accepting both
    List<String> expectedStations = List.of(
      Objects.requireNonNullElse(broker.getStationId(), "NOTCONFIGUREDSTATIONID"),
      Objects.requireNonNullElse(broker.getBroadcastStationId(), "NOTCONFIGUREBROADCASTSTATIONID"));

    if (!expectedStations.contains(request.getIdStation())) {
      log.warn("paymentRequestValidate [{}/{}]: invalid stationId for organization broker obtained[{}] expected one of {}",
        request.getFiscalCode(), request.getNoticeNumber(),
        request.getIdStation(), expectedStations);
      throw new PagoPaNodeFaultException(PagoPaNodeFaults.PAA_STAZIONE_INT_ERRATA, broker.getBrokerFiscalCode());
    }
  }
}
