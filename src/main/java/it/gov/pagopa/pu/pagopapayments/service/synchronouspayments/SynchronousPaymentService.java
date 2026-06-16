package it.gov.pagopa.pu.pagopapayments.service.synchronouspayments;

import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import it.gov.pagopa.pu.organization.dto.generated.Broker;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.organization.dto.generated.OrganizationApiKeyType;
import it.gov.pagopa.pu.pagopapayments.connector.auth.AuthnService;
import it.gov.pagopa.pu.pagopapayments.connector.debtpositions.DebtPositionService;
import it.gov.pagopa.pu.pagopapayments.connector.organization.OrganizationService;
import it.gov.pagopa.pu.pagopapayments.connector.pu_sil.PuSilService;
import it.gov.pagopa.pu.pagopapayments.connector.send_notification.SendNotificationService;
import it.gov.pagopa.pu.pagopapayments.dto.RetrievePaymentDTO;
import it.gov.pagopa.pu.pagopapayments.enums.PagoPaNodeFaults;
import it.gov.pagopa.pu.pagopapayments.exception.NotPayableSilActualizedAmountException;
import it.gov.pagopa.pu.pagopapayments.exception.PagoPaNodeFaultException;
import it.gov.pagopa.pu.pagopapayments.service.PaForNodeRequestValidatorService;
import it.gov.pagopa.pu.pusil.dto.generated.ActualizationResultDTO;
import it.gov.pagopa.pu.sendnotification.dto.generated.NotificationPriceResponseV23DTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;

import static it.gov.pagopa.pu.pagopapayments.util.DebtPositionUtils.ORDINARY_DEBT_POSITION_ORIGINS;

@Service
@Slf4j
public class SynchronousPaymentService {

  private final DebtPositionService debtPositionService;
  private final PaForNodeRequestValidatorService paForNodeRequestValidatorService;
  private final SynchronousPaymentStatusVerifierService synchronousPaymentStatusVerifierService;
  private final AuthnService authnService;
  private final OrganizationService organizationService;
  private final SendNotificationService sendNotificationService;
  private final PuSilService puSilService;

  public SynchronousPaymentService(DebtPositionService debtPositionService,
                                   PaForNodeRequestValidatorService paForNodeRequestValidatorService,
                                   SynchronousPaymentStatusVerifierService synchronousPaymentStatusVerifierService,
                                   AuthnService authnService,
    OrganizationService organizationService,
    SendNotificationService sendNotificationService, PuSilService puSilService) {
    this.debtPositionService = debtPositionService;
    this.paForNodeRequestValidatorService = paForNodeRequestValidatorService;
    this.synchronousPaymentStatusVerifierService = synchronousPaymentStatusVerifierService;
    this.authnService = authnService;
    this.organizationService = organizationService;
    this.sendNotificationService = sendNotificationService;
    this.puSilService = puSilService;
  }

  public Triple<InstallmentDTO, Organization, Broker> retrievePayment(RetrievePaymentDTO request) {
    String accessToken = authnService.getAccessToken();
    String requestIdMessage = request.getFiscalCode()+"/"+request.getNoticeNumber();
    String nav = request.getNoticeNumber();

    if (!Objects.equals(request.getIdPA(), request.getFiscalCode())) {
      log.warn("paymentRequestValidate [{}]: unexpected idPA[{}]", requestIdMessage, request.getIdPA());
      throw new PagoPaNodeFaultException(PagoPaNodeFaults.PAA_ID_DOMINIO_ERRATO, request.getFiscalCode());
    }

    Pair<Broker, Organization> pair = paForNodeRequestValidatorService.paForNodeRequestValidate(request, accessToken);
    Broker broker = pair.getLeft();
    Organization organization = pair.getRight();

    ActualizeAmountRequestDTO actualizeAmountRequest = retrieveNotificationFeeCents(organization, nav, accessToken);

    InstallmentDTO installment = (actualizeAmountRequest.getNewFeeCents() > 0)
      ? debtPositionService.updateInstallmentNotificationFee(actualizeAmountRequest, accessToken)
      : getPayableDebtPositionByOrganizationAndNav(organization, nav, request.getPostalTransfer(), accessToken);

    if (Boolean.TRUE.equals(broker.getFlagDelegate())) {

      List<TransferDTO> transfers = installment.getTransfers();

      if (CollectionUtils.isEmpty(transfers)) {
        throw new PagoPaNodeFaultException(PagoPaNodeFaults.PAA_SYSTEM_ERROR, request.getIdPA());
      }

      TransferDTO ownerTransfer = transfers.stream()
        .filter(t -> Boolean.TRUE.equals(t.getFlagOwner()))
        .findFirst()
        .orElseThrow(() -> new PagoPaNodeFaultException(PagoPaNodeFaults.PAA_SYSTEM_ERROR, request.getIdPA()));

      if (!Objects.equals(ownerTransfer.getOrgFiscalCode(), request.getIdPA())) {
        throw new PagoPaNodeFaultException(PagoPaNodeFaults.PAA_ID_DOMINIO_ERRATO, request.getIdPA());
      }
    }

    return Triple.of(installment, organization, broker);
  }

  private InstallmentDTO getPayableDebtPositionByOrganizationAndNav(Organization organization, String noticeNumber, Boolean postalTransfer, String accessToken) {
    List<InstallmentDTO> installmentDTOList = debtPositionService.getInstallmentsByOrganizationIdAndNav(organization.getOrganizationId(), noticeNumber, ORDINARY_DEBT_POSITION_ORIGINS, accessToken);
    return synchronousPaymentStatusVerifierService.verifyPaymentStatus(organization, installmentDTOList, noticeNumber, postalTransfer);
  }

  public ActualizeAmountRequestDTO retrieveNotificationFeeCents(Organization organization, String nav, String accessToken){
    DebtPositionTypeOrg debtPositionTypeOrg = debtPositionService.findDebtPositionTypeOrgByOrgIdAndNavAndOrigins(organization.getOrganizationId(), nav, ORDINARY_DEBT_POSITION_ORIGINS, accessToken);
    if(debtPositionTypeOrg!=null && Boolean.TRUE.equals(debtPositionTypeOrg.getFlagAmountActualization())) {
        String orgAccessToken = authnService.getAccessToken(organization.getIpaCode());
        return retrieveNotificationFeeCentsFromPuSil(debtPositionTypeOrg, organization.getOrganizationId(), nav, orgAccessToken);
    } else {
        return retrieveNotificationFeeCentsFromSend(organization.getOrganizationId(), nav, accessToken);
    }
  }

  private ActualizeAmountRequestDTO retrieveNotificationFeeCentsFromPuSil(DebtPositionTypeOrg debtPositionTypeOrg,
    Long organizationId, String nav, String accessToken) {
    ActualizeAmountRequestDTO amountRequest = new ActualizeAmountRequestDTO();
    amountRequest.setOrganizationId(organizationId);
    amountRequest.setNav(nav);
    amountRequest.setActualizedFromPuSil(true);
    amountRequest.setNewFeeCents(0L);
    try{
      if(debtPositionTypeOrg.getAmountActualizationOrgSilServiceId()!=null)
      {
        log.debug("Retrieve notification fee from pu-sil by OrgSilServiceId {} and nav {}", debtPositionTypeOrg.getAmountActualizationOrgSilServiceId(), nav);
        ActualizationResultDTO amountUpdatesDTO = puSilService.actualize(debtPositionTypeOrg.getAmountActualizationOrgSilServiceId(), nav, accessToken);
        amountRequest.setBalance(amountUpdatesDTO.getBalance());
        amountRequest.setIun(amountUpdatesDTO.getIun());
        amountRequest.setNotificationDate(amountUpdatesDTO.getDisplayDate());
        if (amountUpdatesDTO.getNotificationFeeCents()!=null && amountUpdatesDTO.getNotificationFeeCents()>0)
          amountRequest.setNewFeeCents(amountUpdatesDTO.getNotificationFeeCents());
      }else {
        log.error("Failed to retrieve notification fee from pu-sil because amountActualizationOrgSilServiceId is null"
                + " on debtPositionTypeOrgId {}", debtPositionTypeOrg.getDebtPositionTypeOrgId());
      }
    }catch (NotPayableSilActualizedAmountException e){
      throw new PagoPaNodeFaultException(PagoPaNodeFaults.PAA_DOVUTO_NON_PAGABILE, nav);
    }catch (Exception e){
      log.warn("Failed to retrieve notification fee from pu-sil: {}", e.getMessage());
      amountRequest.setNewFeeCents(0L);
    }
    return amountRequest;
  }

  private ActualizeAmountRequestDTO retrieveNotificationFeeCentsFromSend(Long organizationId, String nav, String accessToken) {
    String sendAPIKey = organizationService.getOrganizationApiKey(organizationId, OrganizationApiKeyType.SEND, null, accessToken);
    ActualizeAmountRequestDTO amountRequest = new ActualizeAmountRequestDTO();
    amountRequest.setOrganizationId(organizationId);
    amountRequest.setNav(nav);
    amountRequest.setActualizedFromPuSil(false);
    amountRequest.setNewFeeCents(0L);
    if(sendAPIKey!=null && !sendAPIKey.isEmpty()){
      try{
        NotificationPriceResponseV23DTO notificationPrice = sendNotificationService.retrieveNotificationPrice(organizationId, nav, accessToken);
        log.debug("Retrieve notification price from SEND by organizationId {} and nav {} with result: {}", organizationId, nav, notificationPrice);
        amountRequest.setNewFeeCents(Long.valueOf(Objects.requireNonNullElse(notificationPrice.getTotalPrice(), 0)));
      } catch (Exception e) {
        log.warn("Failed to retrieve notification price for organizationId {} and nav {}: {}", organizationId, nav, e.getMessage());
      }
    }
    return amountRequest;
  }
}
