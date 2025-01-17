package it.gov.pagopa.pu.pagopapayments.service.synchronouspayments;

import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.pagopapayments.connector.DebtPositionClient;
import it.gov.pagopa.pu.pagopapayments.connector.auth.AuthnService;
import it.gov.pagopa.pu.pagopapayments.dto.RetrievePaymentDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class SynchronousPaymentService {

  public enum PaymentStatus {
    UNPAID,
    EXPIRED,
    PAID,
    CANCELLED,
    DRAFT,
    TO_SYNCH
  }

  private final DebtPositionClient debtPositionClient;
  private final SynchronousPaymentRequestValidatorService synchronousPaymentRequestValidatorService;
  private final SynchronousPaymentStatusVerifierService synchronousPaymentStatusVerifierService;
  private final AuthnService authnService;

  public SynchronousPaymentService(DebtPositionClient debtPositionClient,
                                   SynchronousPaymentRequestValidatorService synchronousPaymentRequestValidatorService,
                                   SynchronousPaymentStatusVerifierService synchronousPaymentStatusVerifierService,
                                   AuthnService authnService) {
    this.debtPositionClient = debtPositionClient;
    this.synchronousPaymentRequestValidatorService = synchronousPaymentRequestValidatorService;
    this.synchronousPaymentStatusVerifierService = synchronousPaymentStatusVerifierService;
    this.authnService = authnService;
  }

  public Pair<InstallmentDTO, Organization> retrievePayment(RetrievePaymentDTO request) {
    String accessToken = authnService.getAccessToken();
    Organization organization = synchronousPaymentRequestValidatorService.paymentRequestValidate(request, accessToken);
    InstallmentDTO installment = getPayableDebtPositionByOrganizationAndNav(organization, request.getNoticeNumber(), request.getPostalTransfer(), accessToken);
    return Pair.of(installment, organization);
  }

  private InstallmentDTO getPayableDebtPositionByOrganizationAndNav(Organization organization, String noticeNumber, Boolean postalTransfer, String accessToken) {
    List<InstallmentDTO> installmentDTOList = debtPositionClient.getDebtPositionsByOrganizationIdAndNav(organization.getOrganizationId(), noticeNumber, accessToken);
    return synchronousPaymentStatusVerifierService.verifyPaymentStatus(organization, installmentDTOList, noticeNumber, postalTransfer);
  }

}
