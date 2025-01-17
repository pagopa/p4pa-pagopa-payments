package it.gov.pagopa.pu.pagopapayments.service;

import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.organization.dto.generated.Broker;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.pagopapayments.connector.DebtPositionClient;
import it.gov.pagopa.pu.pagopapayments.connector.OrganizationClient;
import it.gov.pagopa.pu.pagopapayments.connector.auth.AuthnService;
import it.gov.pagopa.pu.pagopapayments.dto.RetrievePaymentDTO;
import it.gov.pagopa.pu.pagopapayments.enums.PagoPaNodeFaults;
import it.gov.pagopa.pu.pagopapayments.exception.SynchronousPaymentException;
import it.gov.pagopa.pu.pagopapayments.service.synchronouspayments.PaymentService;
import it.gov.pagopa.pu.pagopapayments.util.TestUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
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
class PaymentServiceTest {

  @Mock
  private DebtPositionClient debtPositionClientMock;
  @Mock
  private OrganizationClient organizationClientMock;
  @Mock
  private AuthnService authnServiceMock;

  @InjectMocks
  private PaymentService paymentService;

  private final PodamFactory podamFactory;

  PaymentServiceTest() {
    podamFactory = TestUtils.getPodamFactory();
  }

  private static final String VALID_ACCEESS_TOKEN = "VALID_ACCESS_TOKEN";


  @BeforeEach
  void setup() {
    Mockito.when(authnServiceMock.getAccessToken()).thenReturn(VALID_ACCEESS_TOKEN);
  }

  //region retrievePayment

  @Test
  void givenValidInstallmentWhenRetrievePaymentThenOk() {
    // Given
    Organization organization = podamFactory.manufacturePojo(Organization.class);
    Broker broker = podamFactory.manufacturePojo(Broker.class);
    organization.setBrokerId(broker.getBrokerId());
    organization.setStatus(Organization.StatusEnum.ACTIVE);
    InstallmentDTO installmentDTO = podamFactory.manufacturePojo(InstallmentDTO.class);
    installmentDTO.setStatus(PaymentService.PaymentStatus.UNPAID.name());

    Mockito.when(organizationClientMock.getOrganizationByFiscalCode(organization.getOrgFiscalCode(), VALID_ACCEESS_TOKEN)).thenReturn(organization);
    Mockito.when(organizationClientMock.getBrokerById(broker.getBrokerId(), VALID_ACCEESS_TOKEN)).thenReturn(broker);
    Mockito.when(debtPositionClientMock.getDebtPositionsByOrganizationIdAndNav(organization.getOrganizationId(), installmentDTO.getNav(), VALID_ACCEESS_TOKEN))
      .thenReturn(List.of(installmentDTO));

    RetrievePaymentDTO retrievePaymentDTO = RetrievePaymentDTO.builder()
      .idStation(broker.getStationId())
      .fiscalCode(organization.getOrgFiscalCode())
      .idPA(organization.getOrgFiscalCode())
      .noticeNumber(installmentDTO.getNav())
      .idBrokerPA(broker.getBrokerFiscalCode())
      .build();
    Pair<InstallmentDTO, Organization> expectedResponse = Pair.of(installmentDTO, organization);

    // When
    Pair<InstallmentDTO, Organization> response = paymentService.retrievePayment(retrievePaymentDTO);

    // Then
    Assertions.assertTrue(new ReflectionEquals(expectedResponse).matches(response));
    Mockito.verify(authnServiceMock, Mockito.times(1)).getAccessToken();
    Mockito.verify(organizationClientMock, Mockito.times(1)).getOrganizationByFiscalCode(organization.getOrgFiscalCode(), VALID_ACCEESS_TOKEN);
    Mockito.verify(organizationClientMock, Mockito.times(1)).getBrokerById(broker.getBrokerId(), VALID_ACCEESS_TOKEN);
    Mockito.verify(debtPositionClientMock, Mockito.times(1)).getDebtPositionsByOrganizationIdAndNav(organization.getOrganizationId(), installmentDTO.getNav(), VALID_ACCEESS_TOKEN);
  }

  @Test
  void givenValidInstallmentAndOtherInstallmentWhenRetrievePaymentThenOk() {
    // Given
    Organization organization = podamFactory.manufacturePojo(Organization.class);
    Broker broker = podamFactory.manufacturePojo(Broker.class);
    organization.setBrokerId(broker.getBrokerId());
    organization.setStatus(Organization.StatusEnum.ACTIVE);
    InstallmentDTO installmentDTO = podamFactory.manufacturePojo(InstallmentDTO.class);
    installmentDTO.setStatus(PaymentService.PaymentStatus.UNPAID.name());

    Mockito.when(organizationClientMock.getOrganizationByFiscalCode(organization.getOrgFiscalCode(), VALID_ACCEESS_TOKEN)).thenReturn(organization);
    Mockito.when(organizationClientMock.getBrokerById(broker.getBrokerId(), VALID_ACCEESS_TOKEN)).thenReturn(broker);
    Mockito.when(debtPositionClientMock.getDebtPositionsByOrganizationIdAndNav(organization.getOrganizationId(), installmentDTO.getNav(), VALID_ACCEESS_TOKEN))
      .thenReturn(List.of(installmentDTO, new InstallmentDTO().status(PaymentService.PaymentStatus.CANCELLED.name())));

    RetrievePaymentDTO retrievePaymentDTO = RetrievePaymentDTO.builder()
      .idStation(broker.getStationId())
      .fiscalCode(organization.getOrgFiscalCode())
      .idPA(organization.getOrgFiscalCode())
      .noticeNumber(installmentDTO.getNav())
      .idBrokerPA(broker.getBrokerFiscalCode())
      .build();
    Pair<InstallmentDTO, Organization> expectedResponse = Pair.of(installmentDTO, organization);

    // When
    Pair<InstallmentDTO, Organization> response = paymentService.retrievePayment(retrievePaymentDTO);

    // Then
    Assertions.assertTrue(new ReflectionEquals(expectedResponse).matches(response));
    Mockito.verify(authnServiceMock, Mockito.times(1)).getAccessToken();
    Mockito.verify(organizationClientMock, Mockito.times(1)).getOrganizationByFiscalCode(organization.getOrgFiscalCode(), VALID_ACCEESS_TOKEN);
    Mockito.verify(organizationClientMock, Mockito.times(1)).getBrokerById(broker.getBrokerId(), VALID_ACCEESS_TOKEN);
    Mockito.verify(debtPositionClientMock, Mockito.times(1)).getDebtPositionsByOrganizationIdAndNav(organization.getOrganizationId(), installmentDTO.getNav(), VALID_ACCEESS_TOKEN);
  }

  @Test
  void givenNotFoundOrgWhenRetrievePaymentThenFault() {
    // Given
    RetrievePaymentDTO retrievePaymentDTO = podamFactory.manufacturePojo(RetrievePaymentDTO.class);

    Mockito.when(organizationClientMock.getOrganizationByFiscalCode(retrievePaymentDTO.getFiscalCode(), VALID_ACCEESS_TOKEN)).thenReturn(null);

    // When
    SynchronousPaymentException response = Assertions.assertThrows(SynchronousPaymentException.class, () -> paymentService.retrievePayment(retrievePaymentDTO));

    // Then
    Assertions.assertEquals(PagoPaNodeFaults.PAA_ID_DOMINIO_ERRATO.code(), response.getErrorCode());
    Assertions.assertEquals(retrievePaymentDTO.getIdBrokerPA(), response.getErrorEmitter());
  }

  @Test
  void givenNotActiveOrgWhenRetrievePaymentThenFault() {
    // Given
    Organization organization = podamFactory.manufacturePojo(Organization.class);
    organization.setStatus(Organization.StatusEnum.DRAFT);
    RetrievePaymentDTO retrievePaymentDTO = podamFactory.manufacturePojo(RetrievePaymentDTO.class);
    retrievePaymentDTO.setIdPA(organization.getOrgFiscalCode());

    Mockito.when(organizationClientMock.getOrganizationByFiscalCode(retrievePaymentDTO.getFiscalCode(), VALID_ACCEESS_TOKEN)).thenReturn(organization);

    // When
    SynchronousPaymentException response = Assertions.assertThrows(SynchronousPaymentException.class, () -> paymentService.retrievePayment(retrievePaymentDTO));

    // Then
    Assertions.assertEquals(PagoPaNodeFaults.PAA_ID_DOMINIO_ERRATO.code(), response.getErrorCode());
    Assertions.assertEquals(organization.getOrgFiscalCode(), response.getErrorEmitter());
  }

  @Test
  void givenInvalidBrokerWhenRetrievePaymentThenFault() {
    // Given
    Organization organization = podamFactory.manufacturePojo(Organization.class);
    organization.setStatus(Organization.StatusEnum.ACTIVE);
    Broker broker = podamFactory.manufacturePojo(Broker.class);
    RetrievePaymentDTO retrievePaymentDTO = podamFactory.manufacturePojo(RetrievePaymentDTO.class);
    retrievePaymentDTO.setIdPA(organization.getOrgFiscalCode());
    retrievePaymentDTO.setIdBrokerPA(broker.getBrokerFiscalCode()+"xxx");

    Mockito.when(organizationClientMock.getOrganizationByFiscalCode(retrievePaymentDTO.getFiscalCode(), VALID_ACCEESS_TOKEN)).thenReturn(organization);
    Mockito.when(organizationClientMock.getBrokerById(organization.getBrokerId(), VALID_ACCEESS_TOKEN)).thenReturn(broker);

    // When
    SynchronousPaymentException response = Assertions.assertThrows(SynchronousPaymentException.class, () -> paymentService.retrievePayment(retrievePaymentDTO));

    // Then
    Assertions.assertEquals(PagoPaNodeFaults.PAA_ID_INTERMEDIARIO_ERRATO.code(), response.getErrorCode());
    Assertions.assertEquals(broker.getBrokerFiscalCode(), response.getErrorEmitter());
  }

  @Test
  void givenInvalidStationWhenRetrievePaymentThenFault() {
    // Given
    Organization organization = podamFactory.manufacturePojo(Organization.class);
    organization.setStatus(Organization.StatusEnum.ACTIVE);
    Broker broker = podamFactory.manufacturePojo(Broker.class);
    RetrievePaymentDTO retrievePaymentDTO = podamFactory.manufacturePojo(RetrievePaymentDTO.class);
    retrievePaymentDTO.setIdPA(organization.getOrgFiscalCode());
    retrievePaymentDTO.setIdBrokerPA(broker.getBrokerFiscalCode());
    retrievePaymentDTO.setIdStation(broker.getStationId()+"xxx");

    Mockito.when(organizationClientMock.getOrganizationByFiscalCode(retrievePaymentDTO.getFiscalCode(), VALID_ACCEESS_TOKEN)).thenReturn(organization);
    Mockito.when(organizationClientMock.getBrokerById(organization.getBrokerId(), VALID_ACCEESS_TOKEN)).thenReturn(broker);

    // When
    SynchronousPaymentException response = Assertions.assertThrows(SynchronousPaymentException.class, () -> paymentService.retrievePayment(retrievePaymentDTO));

    // Then
    Assertions.assertEquals(PagoPaNodeFaults.PAA_STAZIONE_INT_ERRATA.code(), response.getErrorCode());
    Assertions.assertEquals(broker.getBrokerFiscalCode(), response.getErrorEmitter());
  }

  @Test
  void givenNotFoundInstallmentWhenRetrievePaymentThenFault() {
    // Given
    Organization organization = podamFactory.manufacturePojo(Organization.class);
    Broker broker = podamFactory.manufacturePojo(Broker.class);
    organization.setBrokerId(broker.getBrokerId());
    organization.setStatus(Organization.StatusEnum.ACTIVE);
    String nav = "NAV";

    Mockito.when(organizationClientMock.getOrganizationByFiscalCode(organization.getOrgFiscalCode(), VALID_ACCEESS_TOKEN)).thenReturn(organization);
    Mockito.when(organizationClientMock.getBrokerById(broker.getBrokerId(), VALID_ACCEESS_TOKEN)).thenReturn(broker);
    Mockito.when(debtPositionClientMock.getDebtPositionsByOrganizationIdAndNav(organization.getOrganizationId(), nav, VALID_ACCEESS_TOKEN))
      .thenReturn(List.of());

    RetrievePaymentDTO retrievePaymentDTO = RetrievePaymentDTO.builder()
      .idStation(broker.getStationId())
      .fiscalCode(organization.getOrgFiscalCode())
      .idPA(organization.getOrgFiscalCode())
      .noticeNumber(nav)
      .idBrokerPA(broker.getBrokerFiscalCode())
      .build();

    // When
    SynchronousPaymentException response = Assertions.assertThrows(SynchronousPaymentException.class, () -> paymentService.retrievePayment(retrievePaymentDTO));

    // Then
    Assertions.assertEquals(PagoPaNodeFaults.PAA_PAGAMENTO_SCONOSCIUTO.code(), response.getErrorCode());
    Assertions.assertEquals(organization.getOrgFiscalCode(), response.getErrorEmitter());
    Mockito.verify(authnServiceMock, Mockito.times(1)).getAccessToken();
    Mockito.verify(organizationClientMock, Mockito.times(1)).getOrganizationByFiscalCode(organization.getOrgFiscalCode(), VALID_ACCEESS_TOKEN);
    Mockito.verify(organizationClientMock, Mockito.times(1)).getBrokerById(broker.getBrokerId(), VALID_ACCEESS_TOKEN);
    Mockito.verify(debtPositionClientMock, Mockito.times(1)).getDebtPositionsByOrganizationIdAndNav(organization.getOrganizationId(), nav, VALID_ACCEESS_TOKEN);
  }

  @Test
  void givenDuplicatedInstallmentWhenRetrievePaymentThenFault() {
    // Given
    Organization organization = podamFactory.manufacturePojo(Organization.class);
    Broker broker = podamFactory.manufacturePojo(Broker.class);
    organization.setBrokerId(broker.getBrokerId());
    organization.setStatus(Organization.StatusEnum.ACTIVE);
    InstallmentDTO installmentDTO = podamFactory.manufacturePojo(InstallmentDTO.class);
    installmentDTO.setStatus(PaymentService.PaymentStatus.UNPAID.name());

    Mockito.when(organizationClientMock.getOrganizationByFiscalCode(organization.getOrgFiscalCode(), VALID_ACCEESS_TOKEN)).thenReturn(organization);
    Mockito.when(organizationClientMock.getBrokerById(broker.getBrokerId(), VALID_ACCEESS_TOKEN)).thenReturn(broker);
    Mockito.when(debtPositionClientMock.getDebtPositionsByOrganizationIdAndNav(organization.getOrganizationId(), installmentDTO.getNav(), VALID_ACCEESS_TOKEN))
      .thenReturn(List.of(installmentDTO, installmentDTO));

    RetrievePaymentDTO retrievePaymentDTO = RetrievePaymentDTO.builder()
      .idStation(broker.getStationId())
      .fiscalCode(organization.getOrgFiscalCode())
      .idPA(organization.getOrgFiscalCode())
      .noticeNumber(installmentDTO.getNav())
      .idBrokerPA(broker.getBrokerFiscalCode())
      .build();

    // When
    SynchronousPaymentException response = Assertions.assertThrows(SynchronousPaymentException.class, () -> paymentService.retrievePayment(retrievePaymentDTO));

    // Then
    Assertions.assertEquals(PagoPaNodeFaults.PAA_PAGAMENTO_DUPLICATO.code(), response.getErrorCode());
    Assertions.assertEquals(organization.getOrgFiscalCode(), response.getErrorEmitter());
    Mockito.verify(authnServiceMock, Mockito.times(1)).getAccessToken();
    Mockito.verify(organizationClientMock, Mockito.times(1)).getOrganizationByFiscalCode(organization.getOrgFiscalCode(), VALID_ACCEESS_TOKEN);
    Mockito.verify(organizationClientMock, Mockito.times(1)).getBrokerById(broker.getBrokerId(), VALID_ACCEESS_TOKEN);
    Mockito.verify(debtPositionClientMock, Mockito.times(1)).getDebtPositionsByOrganizationIdAndNav(organization.getOrganizationId(), installmentDTO.getNav(), VALID_ACCEESS_TOKEN);
  }

  @Test
  void givenExpiredInstallmentWhenRetrievePaymentThenFault() {
    // Given
    Organization organization = podamFactory.manufacturePojo(Organization.class);
    Broker broker = podamFactory.manufacturePojo(Broker.class);
    organization.setBrokerId(broker.getBrokerId());
    organization.setStatus(Organization.StatusEnum.ACTIVE);
    String nav = "NAV";

    Mockito.when(organizationClientMock.getOrganizationByFiscalCode(organization.getOrgFiscalCode(), VALID_ACCEESS_TOKEN)).thenReturn(organization);
    Mockito.when(organizationClientMock.getBrokerById(broker.getBrokerId(), VALID_ACCEESS_TOKEN)).thenReturn(broker);
    Mockito.when(debtPositionClientMock.getDebtPositionsByOrganizationIdAndNav(organization.getOrganizationId(), nav, VALID_ACCEESS_TOKEN))
      .thenReturn(List.of(
        new InstallmentDTO().status(PaymentService.PaymentStatus.CANCELLED.name())
        , new InstallmentDTO().status(PaymentService.PaymentStatus.EXPIRED.name())
        , new InstallmentDTO().status(PaymentService.PaymentStatus.PAID.name())
      ));

    RetrievePaymentDTO retrievePaymentDTO = RetrievePaymentDTO.builder()
      .idStation(broker.getStationId())
      .fiscalCode(organization.getOrgFiscalCode())
      .idPA(organization.getOrgFiscalCode())
      .noticeNumber(nav)
      .idBrokerPA(broker.getBrokerFiscalCode())
      .build();

    // When
    SynchronousPaymentException response = Assertions.assertThrows(SynchronousPaymentException.class, () -> paymentService.retrievePayment(retrievePaymentDTO));

    // Then
    Assertions.assertEquals(PagoPaNodeFaults.PAA_PAGAMENTO_SCADUTO.code(), response.getErrorCode());
    Assertions.assertEquals(organization.getOrgFiscalCode(), response.getErrorEmitter());
    Mockito.verify(authnServiceMock, Mockito.times(1)).getAccessToken();
    Mockito.verify(organizationClientMock, Mockito.times(1)).getOrganizationByFiscalCode(organization.getOrgFiscalCode(), VALID_ACCEESS_TOKEN);
    Mockito.verify(organizationClientMock, Mockito.times(1)).getBrokerById(broker.getBrokerId(), VALID_ACCEESS_TOKEN);
    Mockito.verify(debtPositionClientMock, Mockito.times(1)).getDebtPositionsByOrganizationIdAndNav(organization.getOrganizationId(), nav, VALID_ACCEESS_TOKEN);
  }

  @Test
  void givenPaidInstallmentWhenRetrievePaymentThenFault() {
    // Given
    Organization organization = podamFactory.manufacturePojo(Organization.class);
    Broker broker = podamFactory.manufacturePojo(Broker.class);
    organization.setBrokerId(broker.getBrokerId());
    organization.setStatus(Organization.StatusEnum.ACTIVE);
    String nav = "NAV";

    Mockito.when(organizationClientMock.getOrganizationByFiscalCode(organization.getOrgFiscalCode(), VALID_ACCEESS_TOKEN)).thenReturn(organization);
    Mockito.when(organizationClientMock.getBrokerById(broker.getBrokerId(), VALID_ACCEESS_TOKEN)).thenReturn(broker);
    Mockito.when(debtPositionClientMock.getDebtPositionsByOrganizationIdAndNav(organization.getOrganizationId(), nav, VALID_ACCEESS_TOKEN))
      .thenReturn(List.of(
        new InstallmentDTO().status(PaymentService.PaymentStatus.CANCELLED.name())
        , new InstallmentDTO().status(PaymentService.PaymentStatus.PAID.name())
      ));

    RetrievePaymentDTO retrievePaymentDTO = RetrievePaymentDTO.builder()
      .idStation(broker.getStationId())
      .fiscalCode(organization.getOrgFiscalCode())
      .idPA(organization.getOrgFiscalCode())
      .noticeNumber(nav)
      .idBrokerPA(broker.getBrokerFiscalCode())
      .build();

    // When
    SynchronousPaymentException response = Assertions.assertThrows(SynchronousPaymentException.class, () -> paymentService.retrievePayment(retrievePaymentDTO));

    // Then
    Assertions.assertEquals(PagoPaNodeFaults.PAA_PAGAMENTO_SCONOSCIUTO.code(), response.getErrorCode());
    Assertions.assertEquals(organization.getOrgFiscalCode(), response.getErrorEmitter());
    Mockito.verify(authnServiceMock, Mockito.times(1)).getAccessToken();
    Mockito.verify(organizationClientMock, Mockito.times(1)).getOrganizationByFiscalCode(organization.getOrgFiscalCode(), VALID_ACCEESS_TOKEN);
    Mockito.verify(organizationClientMock, Mockito.times(1)).getBrokerById(broker.getBrokerId(), VALID_ACCEESS_TOKEN);
    Mockito.verify(debtPositionClientMock, Mockito.times(1)).getDebtPositionsByOrganizationIdAndNav(organization.getOrganizationId(), nav, VALID_ACCEESS_TOKEN);
  }

  @Test
  void givenCancelledInstallmentWhenRetrievePaymentThenFault() {
    // Given
    Organization organization = podamFactory.manufacturePojo(Organization.class);
    Broker broker = podamFactory.manufacturePojo(Broker.class);
    organization.setBrokerId(broker.getBrokerId());
    organization.setStatus(Organization.StatusEnum.ACTIVE);
    String nav = "NAV";

    Mockito.when(organizationClientMock.getOrganizationByFiscalCode(organization.getOrgFiscalCode(), VALID_ACCEESS_TOKEN)).thenReturn(organization);
    Mockito.when(organizationClientMock.getBrokerById(broker.getBrokerId(), VALID_ACCEESS_TOKEN)).thenReturn(broker);
    Mockito.when(debtPositionClientMock.getDebtPositionsByOrganizationIdAndNav(organization.getOrganizationId(), nav, VALID_ACCEESS_TOKEN))
      .thenReturn(List.of(
        new InstallmentDTO().status(PaymentService.PaymentStatus.CANCELLED.name())
        , new InstallmentDTO().status(PaymentService.PaymentStatus.DRAFT.name())
      ));

    RetrievePaymentDTO retrievePaymentDTO = RetrievePaymentDTO.builder()
      .idStation(broker.getStationId())
      .fiscalCode(organization.getOrgFiscalCode())
      .idPA(organization.getOrgFiscalCode())
      .noticeNumber(nav)
      .idBrokerPA(broker.getBrokerFiscalCode())
      .build();

    // When
    SynchronousPaymentException response = Assertions.assertThrows(SynchronousPaymentException.class, () -> paymentService.retrievePayment(retrievePaymentDTO));

    // Then
    Assertions.assertEquals(PagoPaNodeFaults.PAA_PAGAMENTO_ANNULLATO.code(), response.getErrorCode());
    Assertions.assertEquals(organization.getOrgFiscalCode(), response.getErrorEmitter());
    Mockito.verify(authnServiceMock, Mockito.times(1)).getAccessToken();
    Mockito.verify(organizationClientMock, Mockito.times(1)).getOrganizationByFiscalCode(organization.getOrgFiscalCode(), VALID_ACCEESS_TOKEN);
    Mockito.verify(organizationClientMock, Mockito.times(1)).getBrokerById(broker.getBrokerId(), VALID_ACCEESS_TOKEN);
    Mockito.verify(debtPositionClientMock, Mockito.times(1)).getDebtPositionsByOrganizationIdAndNav(organization.getOrganizationId(), nav, VALID_ACCEESS_TOKEN);
  }

  @Test
  void givenFallbackGenericStatusInstallmentWhenRetrievePaymentThenFault() {
    // Given
    Organization organization = podamFactory.manufacturePojo(Organization.class);
    Broker broker = podamFactory.manufacturePojo(Broker.class);
    organization.setBrokerId(broker.getBrokerId());
    organization.setStatus(Organization.StatusEnum.ACTIVE);
    String nav = "NAV";

    Mockito.when(organizationClientMock.getOrganizationByFiscalCode(organization.getOrgFiscalCode(), VALID_ACCEESS_TOKEN)).thenReturn(organization);
    Mockito.when(organizationClientMock.getBrokerById(broker.getBrokerId(), VALID_ACCEESS_TOKEN)).thenReturn(broker);
    Mockito.when(debtPositionClientMock.getDebtPositionsByOrganizationIdAndNav(organization.getOrganizationId(), nav, VALID_ACCEESS_TOKEN))
      .thenReturn(List.of(
        new InstallmentDTO().status(PaymentService.PaymentStatus.DRAFT.name())
        , new InstallmentDTO().status(PaymentService.PaymentStatus.DRAFT.name())
      ));

    RetrievePaymentDTO retrievePaymentDTO = RetrievePaymentDTO.builder()
      .idStation(broker.getStationId())
      .fiscalCode(organization.getOrgFiscalCode())
      .idPA(organization.getOrgFiscalCode())
      .noticeNumber(nav)
      .idBrokerPA(broker.getBrokerFiscalCode())
      .build();

    // When
    SynchronousPaymentException response = Assertions.assertThrows(SynchronousPaymentException.class, () -> paymentService.retrievePayment(retrievePaymentDTO));

    // Then
    Assertions.assertEquals(PagoPaNodeFaults.PAA_PAGAMENTO_SCONOSCIUTO.code(), response.getErrorCode());
    Assertions.assertEquals(organization.getOrgFiscalCode(), response.getErrorEmitter());
    Mockito.verify(authnServiceMock, Mockito.times(1)).getAccessToken();
    Mockito.verify(organizationClientMock, Mockito.times(1)).getOrganizationByFiscalCode(organization.getOrgFiscalCode(), VALID_ACCEESS_TOKEN);
    Mockito.verify(organizationClientMock, Mockito.times(1)).getBrokerById(broker.getBrokerId(), VALID_ACCEESS_TOKEN);
    Mockito.verify(debtPositionClientMock, Mockito.times(1)).getDebtPositionsByOrganizationIdAndNav(organization.getOrganizationId(), nav, VALID_ACCEESS_TOKEN);
  }

  //endregion

}
