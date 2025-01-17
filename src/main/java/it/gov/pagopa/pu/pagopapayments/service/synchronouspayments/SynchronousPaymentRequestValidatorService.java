package it.gov.pagopa.pu.pagopapayments.service.synchronouspayments;

import it.gov.pagopa.pu.organization.dto.generated.Broker;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.pagopapayments.connector.OrganizationClient;
import it.gov.pagopa.pu.pagopapayments.dto.RetrievePaymentDTO;
import it.gov.pagopa.pu.pagopapayments.enums.PagoPaNodeFaults;
import it.gov.pagopa.pu.pagopapayments.exception.SynchronousPaymentException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@Slf4j
public class SynchronousPaymentRequestValidatorService {

  private final OrganizationClient organizationClient;

  public SynchronousPaymentRequestValidatorService(OrganizationClient organizationClient) {
    this.organizationClient = organizationClient;
  }

  public Organization paymentRequestValidate(RetrievePaymentDTO request, String accessToken){
    Organization organization = organizationClient.getOrganizationByFiscalCode(request.getFiscalCode(), accessToken);
    if (organization == null) {
      throw new SynchronousPaymentException(PagoPaNodeFaults.PAA_ID_DOMINIO_ERRATO, request.getIdBrokerPA());
    }
    if (!Objects.equals(organization.getStatus(), Organization.StatusEnum.ACTIVE)) {
      log.warn("retrievePayment [{}/{}]: organization is not active", request.getFiscalCode(), request.getNoticeNumber());
      throw new SynchronousPaymentException(PagoPaNodeFaults.PAA_ID_DOMINIO_ERRATO, organization.getOrgFiscalCode());
    }
    //broker cannot be null if organization is found
    Broker broker = organizationClient.getBrokerById(organization.getBrokerId(), accessToken);
    if (!Objects.equals(request.getIdBrokerPA(), broker.getBrokerFiscalCode())) {
      log.warn("retrievePayment [{}/{}]: invalid broken for organization expected/actual[{}/{}]",
        request.getFiscalCode(), request.getNoticeNumber(),
        request.getIdBrokerPA(), broker.getBrokerFiscalCode());
      throw new SynchronousPaymentException(PagoPaNodeFaults.PAA_ID_INTERMEDIARIO_ERRATO, broker.getBrokerFiscalCode());
    }
    if (!Objects.equals(request.getIdStation(), broker.getStationId())) {
      log.warn("retrievePayment [{}/{}]: invalid stationId for organization broker expected/actual[{}/{}]",
        request.getFiscalCode(), request.getNoticeNumber(),
        request.getIdStation(), broker.getStationId());
      throw new SynchronousPaymentException(PagoPaNodeFaults.PAA_STAZIONE_INT_ERRATA, broker.getBrokerFiscalCode());
    }
    return organization;
  }
}
