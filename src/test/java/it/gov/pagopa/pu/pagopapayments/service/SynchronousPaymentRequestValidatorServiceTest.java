package it.gov.pagopa.pu.pagopapayments.service;

import it.gov.pagopa.pu.organization.dto.generated.Broker;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.pagopapayments.connector.OrganizationClient;
import it.gov.pagopa.pu.pagopapayments.dto.RetrievePaymentDTO;
import it.gov.pagopa.pu.pagopapayments.enums.PagoPaNodeFaults;
import it.gov.pagopa.pu.pagopapayments.exception.SynchronousPaymentException;
import it.gov.pagopa.pu.pagopapayments.service.synchronouspayments.SynchronousPaymentRequestValidatorService;
import it.gov.pagopa.pu.pagopapayments.util.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.internal.matchers.apachecommons.ReflectionEquals;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.jemos.podam.api.PodamFactory;

@ExtendWith(MockitoExtension.class)
class SynchronousPaymentRequestValidatorServiceTest {

  @Mock
  private OrganizationClient organizationClientMock;

  @InjectMocks
  private SynchronousPaymentRequestValidatorService synchronousPaymentRequestValidatorService;

  private final PodamFactory podamFactory;

  SynchronousPaymentRequestValidatorServiceTest() {
    podamFactory = TestUtils.getPodamFactory();
  }

  private static final String VALID_ACCEESS_TOKEN = "VALID_ACCESS_TOKEN";

  //region paymentRequestValidate

  @Test
  void givenValidRequestWhenPaymentRequestValidateThenOk() {
    // Given
    Organization organization = podamFactory.manufacturePojo(Organization.class);
    Broker broker = podamFactory.manufacturePojo(Broker.class);
    organization.setBrokerId(broker.getBrokerId());
    organization.setStatus(Organization.StatusEnum.ACTIVE);

    Mockito.when(organizationClientMock.getOrganizationByFiscalCode(organization.getOrgFiscalCode(), VALID_ACCEESS_TOKEN)).thenReturn(organization);
    Mockito.when(organizationClientMock.getBrokerById(broker.getBrokerId(), VALID_ACCEESS_TOKEN)).thenReturn(broker);

    RetrievePaymentDTO request = RetrievePaymentDTO.builder()
      .idStation(broker.getStationId())
      .fiscalCode(organization.getOrgFiscalCode())
      .idPA(organization.getOrgFiscalCode())
      .noticeNumber("NAV")
      .idBrokerPA(broker.getBrokerFiscalCode())
      .build();

    // When
    Organization response = synchronousPaymentRequestValidatorService.paymentRequestValidate(request, VALID_ACCEESS_TOKEN);

    // Then
    Assertions.assertTrue(new ReflectionEquals(organization).matches(response));
    Mockito.verify(organizationClientMock, Mockito.times(1)).getOrganizationByFiscalCode(organization.getOrgFiscalCode(), VALID_ACCEESS_TOKEN);
    Mockito.verify(organizationClientMock, Mockito.times(1)).getBrokerById(broker.getBrokerId(), VALID_ACCEESS_TOKEN);
  }

  @Test
  void givenNotFoundOrgWhenPaymentRequestValidateThenFault() {
    // Given
    RetrievePaymentDTO request = podamFactory.manufacturePojo(RetrievePaymentDTO.class);

    Mockito.when(organizationClientMock.getOrganizationByFiscalCode(request.getFiscalCode(), VALID_ACCEESS_TOKEN)).thenReturn(null);

    // When
    SynchronousPaymentException response = Assertions.assertThrows(SynchronousPaymentException.class, () -> synchronousPaymentRequestValidatorService.paymentRequestValidate(request, VALID_ACCEESS_TOKEN));

    // Then
    Assertions.assertEquals(PagoPaNodeFaults.PAA_ID_DOMINIO_ERRATO, response.getErrorCode());
    Assertions.assertEquals(request.getIdBrokerPA(), response.getErrorEmitter());
    Mockito.verify(organizationClientMock, Mockito.times(1)).getOrganizationByFiscalCode(request.getFiscalCode(), VALID_ACCEESS_TOKEN);
    Mockito.verify(organizationClientMock, Mockito.times(0)).getBrokerById(Mockito.any(), Mockito.any());
  }

  @Test
  void givenNotActiveOrgWhenPaymentRequestValidateThenOk() {
    // Given
    RetrievePaymentDTO request = podamFactory.manufacturePojo(RetrievePaymentDTO.class);
    Organization organization = podamFactory.manufacturePojo(Organization.class);
    organization.setStatus(Organization.StatusEnum.DRAFT);

    Mockito.when(organizationClientMock.getOrganizationByFiscalCode(request.getFiscalCode(), VALID_ACCEESS_TOKEN)).thenReturn(organization);

    // When
    SynchronousPaymentException response = Assertions.assertThrows(SynchronousPaymentException.class, () -> synchronousPaymentRequestValidatorService.paymentRequestValidate(request, VALID_ACCEESS_TOKEN));

    // Then
    Assertions.assertEquals(PagoPaNodeFaults.PAA_ID_DOMINIO_ERRATO, response.getErrorCode());
    Assertions.assertEquals(organization.getOrgFiscalCode(), response.getErrorEmitter());
    Mockito.verify(organizationClientMock, Mockito.times(1)).getOrganizationByFiscalCode(request.getFiscalCode(), VALID_ACCEESS_TOKEN);
    Mockito.verify(organizationClientMock, Mockito.times(0)).getBrokerById(Mockito.any(), Mockito.any());
  }

  @Test
  void givenInvalidBrokerWhenPaymentRequestValidateThenOk() {
    // Given
    Organization organization = podamFactory.manufacturePojo(Organization.class);
    organization.setStatus(Organization.StatusEnum.ACTIVE);
    Broker broker = podamFactory.manufacturePojo(Broker.class);

    RetrievePaymentDTO request = podamFactory.manufacturePojo(RetrievePaymentDTO.class);
    request.setFiscalCode(organization.getOrgFiscalCode());
    request.setIdBrokerPA(broker.getBrokerFiscalCode()+"xxx");

    Mockito.when(organizationClientMock.getOrganizationByFiscalCode(request.getFiscalCode(), VALID_ACCEESS_TOKEN)).thenReturn(organization);
    Mockito.when(organizationClientMock.getBrokerById(organization.getBrokerId(), VALID_ACCEESS_TOKEN)).thenReturn(broker);

    // When
    SynchronousPaymentException response = Assertions.assertThrows(SynchronousPaymentException.class, () -> synchronousPaymentRequestValidatorService.paymentRequestValidate(request, VALID_ACCEESS_TOKEN));

    // Then
    Assertions.assertEquals(PagoPaNodeFaults.PAA_ID_INTERMEDIARIO_ERRATO, response.getErrorCode());
    Assertions.assertEquals(broker.getBrokerFiscalCode(), response.getErrorEmitter());
    Mockito.verify(organizationClientMock, Mockito.times(1)).getOrganizationByFiscalCode(organization.getOrgFiscalCode(), VALID_ACCEESS_TOKEN);
    Mockito.verify(organizationClientMock, Mockito.times(1)).getBrokerById(Mockito.any(), Mockito.any());
  }

  @Test
  void givenInvalidStationWhenPaymentRequestValidateThenOk() {
    // Given
    Organization organization = podamFactory.manufacturePojo(Organization.class);
    organization.setStatus(Organization.StatusEnum.ACTIVE);
    Broker broker = podamFactory.manufacturePojo(Broker.class);

    RetrievePaymentDTO request = podamFactory.manufacturePojo(RetrievePaymentDTO.class);
    request.setFiscalCode(organization.getOrgFiscalCode());
    request.setIdBrokerPA(broker.getBrokerFiscalCode());
    request.setIdStation(broker.getStationId() + "xxx");

    Mockito.when(organizationClientMock.getOrganizationByFiscalCode(request.getFiscalCode(), VALID_ACCEESS_TOKEN)).thenReturn(organization);
    Mockito.when(organizationClientMock.getBrokerById(organization.getBrokerId(), VALID_ACCEESS_TOKEN)).thenReturn(broker);

    // When
    SynchronousPaymentException response = Assertions.assertThrows(SynchronousPaymentException.class, () -> synchronousPaymentRequestValidatorService.paymentRequestValidate(request, VALID_ACCEESS_TOKEN));

    // Then
    Assertions.assertEquals(PagoPaNodeFaults.PAA_STAZIONE_INT_ERRATA, response.getErrorCode());
    Assertions.assertEquals(broker.getBrokerFiscalCode(), response.getErrorEmitter());
    Mockito.verify(organizationClientMock, Mockito.times(1)).getOrganizationByFiscalCode(organization.getOrgFiscalCode(), VALID_ACCEESS_TOKEN);
    Mockito.verify(organizationClientMock, Mockito.times(1)).getBrokerById(Mockito.any(), Mockito.any());
  }

  //endregion

}
