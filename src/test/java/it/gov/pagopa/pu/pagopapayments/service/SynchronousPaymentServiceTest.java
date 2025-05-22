package it.gov.pagopa.pu.pagopapayments.service;

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
import it.gov.pagopa.pu.pagopapayments.service.synchronouspayments.SynchronousPaymentService;
import it.gov.pagopa.pu.pagopapayments.service.synchronouspayments.SynchronousPaymentStatusVerifierService;
import it.gov.pagopa.pu.pagopapayments.util.TestUtils;
import it.gov.pagopa.pu.sendnotification.dto.generated.NotificationPriceResponseV23DTO;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.internal.matchers.apachecommons.ReflectionEquals;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.jemos.podam.api.PodamFactory;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class SynchronousPaymentServiceTest {

  @Mock
  private DebtPositionService debtPositionServiceMock;
  @Mock
  private AuthnService authnServiceMock;
  @Mock
  private PaForNodeRequestValidatorService paForNodeRequestValidatorServiceMock;
  @Mock
  private SynchronousPaymentStatusVerifierService synchronousPaymentStatusVerifierServiceMock;
  @Mock
  private OrganizationService organizationServiceMock;
  @Mock
  private SendNotificationService sendNotificationServiceMock;

  @InjectMocks
  private SynchronousPaymentService synchronousPaymentService;

  private final PodamFactory podamFactory;

  SynchronousPaymentServiceTest() {
    podamFactory = TestUtils.getPodamFactory();
  }

  private static final String VALID_ACCEESS_TOKEN = "VALID_ACCESS_TOKEN";

  //region retrievePayment

  @Test
  void givenValidInstallmentWhenRetrievePaymentThenOk() {
    // Given
    Organization organization = podamFactory.manufacturePojo(Organization.class);
    InstallmentDTO installmentDTO = podamFactory.manufacturePojo(InstallmentDTO.class);
    List<InstallmentDTO> installmentDTOList = List.of(installmentDTO);
    RetrievePaymentDTO retrievePaymentDTO = podamFactory.manufacturePojo(RetrievePaymentDTO.class);
    retrievePaymentDTO.setIdPA(retrievePaymentDTO.getFiscalCode());

    Mockito.when(authnServiceMock.getAccessToken()).thenReturn(VALID_ACCEESS_TOKEN);
    Mockito.when(paForNodeRequestValidatorServiceMock.paForNodeRequestValidate(retrievePaymentDTO, VALID_ACCEESS_TOKEN)).thenReturn(organization);
    Mockito.when(organizationServiceMock.getOrganizationApiKey(organization.getOrganizationId(), OrganizationApiKeyType.SEND, VALID_ACCEESS_TOKEN)).thenReturn(null);
    Mockito.when(debtPositionServiceMock.getDebtPositionsByOrganizationIdAndNav(organization.getOrganizationId(), retrievePaymentDTO.getNoticeNumber(),
        SynchronousPaymentService.ORDINARY_DEBT_POSITION_ORIGINS, VALID_ACCEESS_TOKEN))
      .thenReturn(installmentDTOList);
    Mockito.when(synchronousPaymentStatusVerifierServiceMock.verifyPaymentStatus(organization, installmentDTOList, retrievePaymentDTO.getNoticeNumber(), retrievePaymentDTO.getPostalTransfer())).thenReturn(installmentDTO);

    Pair<InstallmentDTO, Organization> expectedResponse = Pair.of(installmentDTO, organization);

    // When
    Pair<InstallmentDTO, Organization> response = synchronousPaymentService.retrievePayment(retrievePaymentDTO);

    // Then
    Assertions.assertTrue(new ReflectionEquals(expectedResponse).matches(response));
    Mockito.verify(authnServiceMock, Mockito.times(1)).getAccessToken();
    Mockito.verify(paForNodeRequestValidatorServiceMock, Mockito.times(1)).paForNodeRequestValidate(retrievePaymentDTO, VALID_ACCEESS_TOKEN);
    Mockito.verify(debtPositionServiceMock, Mockito.times(1)).getDebtPositionsByOrganizationIdAndNav(organization.getOrganizationId(), retrievePaymentDTO.getNoticeNumber(),
      SynchronousPaymentService.ORDINARY_DEBT_POSITION_ORIGINS, VALID_ACCEESS_TOKEN);
    Mockito.verify(synchronousPaymentStatusVerifierServiceMock, Mockito.times(1))
      .verifyPaymentStatus(organization, installmentDTOList, retrievePaymentDTO.getNoticeNumber(), retrievePaymentDTO.getPostalTransfer());
  }

  @Test
  void givenNotificationFeeGreaterThanZeroWhenRetrievePaymentThenUpdateInstallmentNotificationFeeIsCalled() {
    // Given
    Organization organization = podamFactory.manufacturePojo(Organization.class);
    InstallmentDTO installmentDTO = podamFactory.manufacturePojo(InstallmentDTO.class);
    RetrievePaymentDTO retrievePaymentDTO = podamFactory.manufacturePojo(RetrievePaymentDTO.class);
    retrievePaymentDTO.setIdPA(retrievePaymentDTO.getFiscalCode());
    int notificationFeeCents = 100;
    NotificationPriceResponseV23DTO notificationPriceResponse = new NotificationPriceResponseV23DTO();
    notificationPriceResponse.setTotalPrice(notificationFeeCents);
    String apiKey = "API_KEY";

    Mockito.when(authnServiceMock.getAccessToken()).thenReturn(VALID_ACCEESS_TOKEN);
    Mockito.when(paForNodeRequestValidatorServiceMock.paForNodeRequestValidate(retrievePaymentDTO, VALID_ACCEESS_TOKEN))
      .thenReturn(organization);
    Mockito.when(organizationServiceMock.getOrganizationApiKey(organization.getOrganizationId(), OrganizationApiKeyType.SEND, VALID_ACCEESS_TOKEN)).thenReturn(apiKey);
    Mockito.when(sendNotificationServiceMock.retrieveNotificationPrice(organization.getOrganizationId(),
      retrievePaymentDTO.getNoticeNumber(), VALID_ACCEESS_TOKEN)).thenReturn(notificationPriceResponse);
    Mockito.when(debtPositionServiceMock.updateInstallmentNotificationFee(
        organization.getOrganizationId(),
        retrievePaymentDTO.getNoticeNumber(),
        (long) notificationFeeCents,
        VALID_ACCEESS_TOKEN))
      .thenReturn(installmentDTO);

    // When
    Pair<InstallmentDTO, Organization> response = synchronousPaymentService.retrievePayment(retrievePaymentDTO);

    // Then
    Assertions.assertNotNull(response);
    Mockito.verify(authnServiceMock, Mockito.times(1)).getAccessToken();
    Mockito.verify(paForNodeRequestValidatorServiceMock, Mockito.times(1))
      .paForNodeRequestValidate(retrievePaymentDTO, VALID_ACCEESS_TOKEN);
    Mockito.verify(sendNotificationServiceMock, Mockito.times(1))
      .retrieveNotificationPrice(organization.getOrganizationId(), retrievePaymentDTO.getNoticeNumber(), VALID_ACCEESS_TOKEN);
    Mockito.verify(debtPositionServiceMock, Mockito.times(1))
      .updateInstallmentNotificationFee(organization.getOrganizationId(), retrievePaymentDTO.getNoticeNumber(),
        (long) notificationFeeCents, VALID_ACCEESS_TOKEN);
    Mockito.verify(debtPositionServiceMock, Mockito.never())
      .getDebtPositionsByOrganizationIdAndNav(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
  }

  @Test
  void givenInvalidInstallmentWhenRetrievePaymentThenFault() {
    // Given
    RetrievePaymentDTO retrievePaymentDTO = podamFactory.manufacturePojo(RetrievePaymentDTO.class);
    retrievePaymentDTO.setIdPA(retrievePaymentDTO.getFiscalCode()+"XXX");
    Mockito.when(authnServiceMock.getAccessToken()).thenReturn(VALID_ACCEESS_TOKEN);
    // When
    PagoPaNodeFaultException exception = Assertions.assertThrows(PagoPaNodeFaultException.class,()->synchronousPaymentService.retrievePayment(retrievePaymentDTO));

    // Then
    Assertions.assertEquals(PagoPaNodeFaults.PAA_ID_DOMINIO_ERRATO, exception.getErrorCode());
    Assertions.assertEquals(retrievePaymentDTO.getFiscalCode(), exception.getErrorEmitter());
    Mockito.verify(authnServiceMock, Mockito.times(1)).getAccessToken();
    Mockito.verifyNoInteractions(paForNodeRequestValidatorServiceMock, debtPositionServiceMock, synchronousPaymentStatusVerifierServiceMock);
  }

  //endregion

  //region retrieveNotificationFee
  @Test
  void givenApiKeyIsPresentWhenRetrieveNotificationFeeThenReturnNotificationPrice() {
    Long organizationId = 1L;
    String nav = "NAV";
    String apiKey = "API-KEY";
    int expectedPrice = 100;

    NotificationPriceResponseV23DTO mockResponse = Mockito.mock(NotificationPriceResponseV23DTO.class);

    Mockito.when(organizationServiceMock.getOrganizationApiKey(organizationId, OrganizationApiKeyType.SEND, VALID_ACCEESS_TOKEN)).thenReturn(apiKey);
    Mockito.when(sendNotificationServiceMock.retrieveNotificationPrice(organizationId, nav, VALID_ACCEESS_TOKEN)).thenReturn(mockResponse);
    Mockito.when(mockResponse.getTotalPrice()).thenReturn(expectedPrice);

    long result = synchronousPaymentService.retrieveNotificationFeeCents(organizationId, nav, VALID_ACCEESS_TOKEN);

    Assertions.assertEquals(expectedPrice, result);
  }

  @Test
  void givenApiKeyAbsentWhenRetrieveNotificationFeeThenReturnNotificationPrice() {
    Long organizationId = 1L;
    String nav = "NAV";
    String emptyApiKey = "";

    Mockito.when(organizationServiceMock.getOrganizationApiKey(organizationId, OrganizationApiKeyType.SEND, VALID_ACCEESS_TOKEN)).thenReturn(emptyApiKey);

    long result = synchronousPaymentService.retrieveNotificationFeeCents(organizationId, nav, VALID_ACCEESS_TOKEN);

    Assertions.assertEquals(0, result);
  }

  @Test
  void givenApiKeyIsPresentWhenRetrieveNotificationPriceThrowsExceptionThenReturnZero() {
    Long organizationId = 1L;
    String nav = "NAV";
    String apiKey = "API-KEY";

    Mockito.when(organizationServiceMock.getOrganizationApiKey(organizationId, OrganizationApiKeyType.SEND, VALID_ACCEESS_TOKEN)).thenReturn(apiKey);
    Mockito.when(sendNotificationServiceMock.retrieveNotificationPrice(organizationId, nav, VALID_ACCEESS_TOKEN))
      .thenThrow(new RuntimeException("Not Found"));

    long result = synchronousPaymentService.retrieveNotificationFeeCents(organizationId, nav, VALID_ACCEESS_TOKEN);

    Assertions.assertEquals(0, result);
  }
  //end region
}
