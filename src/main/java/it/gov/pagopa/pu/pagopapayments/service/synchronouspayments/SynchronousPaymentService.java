package it.gov.pagopa.pu.pagopapayments.service.synchronouspayments;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionOrigin;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.organization.dto.generated.OrganizationApiKeyType;
import it.gov.pagopa.pu.pagopapayments.connector.auth.AuthnService;
import it.gov.pagopa.pu.pagopapayments.connector.debtpositions.DebtPositionService;
import it.gov.pagopa.pu.pagopapayments.connector.organization.OrganizationService;
import it.gov.pagopa.pu.pagopapayments.connector.send_notification.SendNotificationService;
import it.gov.pagopa.pu.pagopapayments.dto.RetrievePaymentDTO;
import it.gov.pagopa.pu.pagopapayments.enums.PagoPaNodeFaults;
import it.gov.pagopa.pu.pagopapayments.exception.PagoPaNodeFaultException;
import it.gov.pagopa.pu.pagopapayments.service.PaForNodeRequestValidatorService;
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

  public SynchronousPaymentService(DebtPositionService debtPositionService,
                                   PaForNodeRequestValidatorService paForNodeRequestValidatorService,
                                   SynchronousPaymentStatusVerifierService synchronousPaymentStatusVerifierService,
                                   AuthnService authnService,
    OrganizationService organizationService,
    SendNotificationService sendNotificationService) {
    this.debtPositionService = debtPositionService;
    this.paForNodeRequestValidatorService = paForNodeRequestValidatorService;
    this.synchronousPaymentStatusVerifierService = synchronousPaymentStatusVerifierService;
    this.authnService = authnService;
    this.organizationService = organizationService;
    this.sendNotificationService = sendNotificationService;
  }

  public Pair<InstallmentDTO, Organization> retrievePayment(RetrievePaymentDTO request) {
    String accessToken = authnService.getAccessToken();
    String requestIdMessage = request.getFiscalCode()+"/"+request.getNoticeNumber();
    if (!Objects.equals(request.getIdPA(), request.getFiscalCode())) {
      log.warn("paymentRequestValidate [{}]: unexpected idPA[{}]", requestIdMessage, request.getIdPA());
      throw new PagoPaNodeFaultException(PagoPaNodeFaults.PAA_ID_DOMINIO_ERRATO, request.getFiscalCode());
    }
    Organization organization = paForNodeRequestValidatorService.paForNodeRequestValidate(request, accessToken);
    //TODO - P4ADEV-2622
    InstallmentDTO installment = getPayableDebtPositionByOrganizationAndNav(organization, request.getNoticeNumber(), request.getPostalTransfer(), accessToken);
    return Pair.of(installment, organization);
  }

  private InstallmentDTO getPayableDebtPositionByOrganizationAndNav(Organization organization, String noticeNumber, Boolean postalTransfer, String accessToken) {
    List<InstallmentDTO> installmentDTOList = debtPositionService.getDebtPositionsByOrganizationIdAndNav(organization.getOrganizationId(), noticeNumber, ORDINARY_DEBT_POSITION_ORIGINS, accessToken);
    return synchronousPaymentStatusVerifierService.verifyPaymentStatus(organization, installmentDTOList, noticeNumber, postalTransfer);
  }

  public long retrieveNotificationFee(Long organizationId, String nav){
    String accessToken = authnService.getAccessToken();
    String sendAPIKey = organizationService.getOrganizationApiKey(organizationId, OrganizationApiKeyType.SEND, accessToken);
    if(!sendAPIKey.isEmpty()){
      NotificationPriceResponseV23DTO notificationPrice = sendNotificationService.retrieveNotificationPrice(organizationId, nav, accessToken);
      return Objects.requireNonNullElse(notificationPrice.getTotalPrice(), 0);
    } else {
      //TODO - P4ADEV-2694 if SENDApiKey doesn't exists call external third part API to retrieve notificationFee
      return 0;
    }
  }
}
