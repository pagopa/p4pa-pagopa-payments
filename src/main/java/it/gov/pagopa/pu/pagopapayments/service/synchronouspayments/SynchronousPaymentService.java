package it.gov.pagopa.pu.pagopapayments.service.synchronouspayments;

import it.gov.pagopa.pu.debtpositions.dto.generated.ActualizeAmountRequestDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionOrigin;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
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
import it.gov.pagopa.pu.pagopapayments.mapper.BalanceMapper;
import it.gov.pagopa.pu.pagopapayments.service.PaForNodeRequestValidatorService;
import it.gov.pagopa.pu.pusil.dto.generated.ActualizationResultDTO;
import it.gov.pagopa.pu.sendnotification.dto.generated.NotificationPriceResponseV23DTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class SynchronousPaymentService {

  public static final List<DebtPositionOrigin> ORDINARY_DEBT_POSITION_ORIGINS = List.of(
    DebtPositionOrigin.ORDINARY,
    DebtPositionOrigin.ORDINARY_SIL,
    DebtPositionOrigin.SPONTANEOUS);

  private final DebtPositionService debtPositionService;
  private final PaForNodeRequestValidatorService paForNodeRequestValidatorService;
  private final SynchronousPaymentStatusVerifierService synchronousPaymentStatusVerifierService;
  private final AuthnService authnService;
  private final OrganizationService organizationService;
  private final SendNotificationService sendNotificationService;
  private final PuSilService puSilService;
  private final BalanceMapper balanceMapper;

  public SynchronousPaymentService(DebtPositionService debtPositionService,
                                   PaForNodeRequestValidatorService paForNodeRequestValidatorService,
                                   SynchronousPaymentStatusVerifierService synchronousPaymentStatusVerifierService,
                                   AuthnService authnService,
    OrganizationService organizationService,
    SendNotificationService sendNotificationService, PuSilService puSilService,
    BalanceMapper balanceMapper) {
    this.debtPositionService = debtPositionService;
    this.paForNodeRequestValidatorService = paForNodeRequestValidatorService;
    this.synchronousPaymentStatusVerifierService = synchronousPaymentStatusVerifierService;
    this.authnService = authnService;
    this.organizationService = organizationService;
    this.sendNotificationService = sendNotificationService;
    this.puSilService = puSilService;
    this.balanceMapper = balanceMapper;
  }

  public Pair<InstallmentDTO, Organization> retrievePayment(RetrievePaymentDTO request) {
    String accessToken = authnService.getAccessToken();
    String requestIdMessage = request.getFiscalCode()+"/"+request.getNoticeNumber();
    String nav = request.getNoticeNumber();

    if (!Objects.equals(request.getIdPA(), request.getFiscalCode())) {
      log.warn("paymentRequestValidate [{}]: unexpected idPA[{}]", requestIdMessage, request.getIdPA());
      throw new PagoPaNodeFaultException(PagoPaNodeFaults.PAA_ID_DOMINIO_ERRATO, request.getFiscalCode());
    }
    Organization organization = paForNodeRequestValidatorService.paForNodeRequestValidate(request, accessToken);
    ActualizeAmountRequestDTO actualizeAmountRequest = retrieveNotificationFeeCents(organization, nav, accessToken);

    InstallmentDTO installment = (actualizeAmountRequest.getNewFeeCents() > 0)
      ? debtPositionService.updateInstallmentNotificationFee(actualizeAmountRequest, accessToken)
      : getPayableDebtPositionByOrganizationAndNav(organization, nav, request.getPostalTransfer(), accessToken);

    return Pair.of(installment, organization);
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
        log.info("Retrieve notification fee from pu-sil by OrgSilServiceId {} and nav {}", debtPositionTypeOrg.getAmountActualizationOrgSilServiceId(), nav);
        ActualizationResultDTO amountUpdatesDTO = puSilService.actualize(debtPositionTypeOrg.getAmountActualizationOrgSilServiceId(), nav, accessToken);
        amountRequest.setBalance(balanceMapper.mapBalanceFromPuSil(amountUpdatesDTO.getBalance()));
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
    String sendAPIKey = organizationService.getOrganizationApiKey(organizationId, OrganizationApiKeyType.SEND, accessToken);
    ActualizeAmountRequestDTO amountRequest = new ActualizeAmountRequestDTO();
    amountRequest.setOrganizationId(organizationId);
    amountRequest.setNav(nav);
    amountRequest.setActualizedFromPuSil(false);
    amountRequest.setNewFeeCents(0L);
    if(sendAPIKey!=null && !sendAPIKey.isEmpty()){
      try{
        NotificationPriceResponseV23DTO notificationPrice = sendNotificationService.retrieveNotificationPrice(organizationId, nav, accessToken);
        log.info("Retrieve notification price from SEND by organizationId {} and nav {} with result: {}", organizationId, nav, notificationPrice);
        amountRequest.setNewFeeCents(Long.valueOf(Objects.requireNonNullElse(notificationPrice.getTotalPrice(), 0)));
      } catch (Exception e) {
        log.warn("Failed to retrieve notification price for organizationId {} and nav {}: {}", organizationId, nav, e.getMessage());
      }
    }
    return amountRequest;
  }
}
