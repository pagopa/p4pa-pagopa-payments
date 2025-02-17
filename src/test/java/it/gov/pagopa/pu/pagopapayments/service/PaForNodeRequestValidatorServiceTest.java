package it.gov.pagopa.pu.pagopapayments.service;

import it.gov.pagopa.pu.organization.dto.generated.Broker;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.pagopapayments.connector.organization.BrokerService;
import it.gov.pagopa.pu.pagopapayments.connector.organization.OrganizationService;
import it.gov.pagopa.pu.pagopapayments.dto.RetrievePaymentDTO;
import it.gov.pagopa.pu.pagopapayments.enums.PagoPaNodeFaults;
import it.gov.pagopa.pu.pagopapayments.exception.PagoPaNodeFaultException;
import it.gov.pagopa.pu.pagopapayments.util.TestUtils;
import org.junit.jupiter.api.AfterEach;
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
class PaForNodeRequestValidatorServiceTest {

  @Mock
  private BrokerService brokerServiceMock;
  @Mock
  private OrganizationService organizationServiceMock;

  @InjectMocks
  private PaForNodeRequestValidatorService paForNodeRequestValidatorService;

  private final PodamFactory podamFactory;

  PaForNodeRequestValidatorServiceTest() {
    podamFactory = TestUtils.getPodamFactory();
  }

  private static final String VALID_ACCEESS_TOKEN = "VALID_ACCESS_TOKEN";

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(
      brokerServiceMock,
      organizationServiceMock
    );
  }

  //region paymentRequestValidate

  @Test
  void givenValidRequestWhenPaForNodeRequestValidateThenOk() {
    // Given
    Organization organization = podamFactory.manufacturePojo(Organization.class);
    Broker broker = podamFactory.manufacturePojo(Broker.class);
    organization.setBrokerId(broker.getBrokerId());
    organization.setStatus(Organization.StatusEnum.ACTIVE);

    Mockito.when(organizationServiceMock.getOrganizationByFiscalCode(organization.getOrgFiscalCode(), VALID_ACCEESS_TOKEN)).thenReturn(organization);
    Mockito.when(brokerServiceMock.getBrokerById(broker.getBrokerId(), VALID_ACCEESS_TOKEN)).thenReturn(broker);

    RetrievePaymentDTO request = RetrievePaymentDTO.builder()
      .idStation(broker.getStationId())
      .fiscalCode(organization.getOrgFiscalCode())
      .idPA(organization.getOrgFiscalCode())
      .noticeNumber("NAV")
      .idBrokerPA(broker.getBrokerFiscalCode())
      .build();

    // When
    Organization response = paForNodeRequestValidatorService.paForNodeRequestValidate(request, VALID_ACCEESS_TOKEN);

    // Then
    Assertions.assertTrue(new ReflectionEquals(organization).matches(response));
  }

  @Test
  void givenNotFoundOrgWhenPaForNodeRequestValidateThenFault() {
    // Given
    RetrievePaymentDTO request = podamFactory.manufacturePojo(RetrievePaymentDTO.class);
    request.setIdPA(request.getFiscalCode());

    Mockito.when(organizationServiceMock.getOrganizationByFiscalCode(request.getIdPA(), VALID_ACCEESS_TOKEN)).thenReturn(null);

    // When
    PagoPaNodeFaultException response = Assertions.assertThrows(PagoPaNodeFaultException.class, () -> paForNodeRequestValidatorService.paForNodeRequestValidate(request, VALID_ACCEESS_TOKEN));

    // Then
    Assertions.assertEquals(PagoPaNodeFaults.PAA_ID_DOMINIO_ERRATO, response.getErrorCode());
    Assertions.assertEquals(request.getIdBrokerPA(), response.getErrorEmitter());
  }

  @Test
  void givenNotActiveOrgWhenPaForNodeRequestValidateThenOk() {
    // Given
    RetrievePaymentDTO request = podamFactory.manufacturePojo(RetrievePaymentDTO.class);
    request.setIdPA(request.getFiscalCode());
    Organization organization = podamFactory.manufacturePojo(Organization.class);
    organization.setStatus(Organization.StatusEnum.DRAFT);
    organization.setOrgFiscalCode(request.getFiscalCode());

    Mockito.when(organizationServiceMock.getOrganizationByFiscalCode(request.getIdPA(), VALID_ACCEESS_TOKEN)).thenReturn(organization);

    // When
    PagoPaNodeFaultException response = Assertions.assertThrows(PagoPaNodeFaultException.class, () -> paForNodeRequestValidatorService.paForNodeRequestValidate(request, VALID_ACCEESS_TOKEN));

    // Then
    Assertions.assertEquals(PagoPaNodeFaults.PAA_ID_DOMINIO_ERRATO, response.getErrorCode());
    Assertions.assertEquals(organization.getOrgFiscalCode(), response.getErrorEmitter());
  }

  @Test
  void givenInvalidBrokerWhenPaForNodeRequestValidateThenOk() {
    // Given
    Organization organization = podamFactory.manufacturePojo(Organization.class);
    organization.setStatus(Organization.StatusEnum.ACTIVE);
    Broker broker = podamFactory.manufacturePojo(Broker.class);

    RetrievePaymentDTO request = podamFactory.manufacturePojo(RetrievePaymentDTO.class);
    request.setFiscalCode(organization.getOrgFiscalCode());
    request.setIdPA(request.getFiscalCode());
    request.setIdBrokerPA(broker.getBrokerFiscalCode()+"xxx");

    Mockito.when(organizationServiceMock.getOrganizationByFiscalCode(request.getIdPA(), VALID_ACCEESS_TOKEN)).thenReturn(organization);
    Mockito.when(brokerServiceMock.getBrokerById(organization.getBrokerId(), VALID_ACCEESS_TOKEN)).thenReturn(broker);

    // When
    PagoPaNodeFaultException response = Assertions.assertThrows(PagoPaNodeFaultException.class, () -> paForNodeRequestValidatorService.paForNodeRequestValidate(request, VALID_ACCEESS_TOKEN));

    // Then
    Assertions.assertEquals(PagoPaNodeFaults.PAA_ID_INTERMEDIARIO_ERRATO, response.getErrorCode());
    Assertions.assertEquals(broker.getBrokerFiscalCode(), response.getErrorEmitter());
  }

  @Test
  void givenInvalidStationWhenPaForNodeRequestValidateThenOk() {
    // Given
    Organization organization = podamFactory.manufacturePojo(Organization.class);
    organization.setStatus(Organization.StatusEnum.ACTIVE);
    Broker broker = podamFactory.manufacturePojo(Broker.class);

    RetrievePaymentDTO request = podamFactory.manufacturePojo(RetrievePaymentDTO.class);
    request.setFiscalCode(organization.getOrgFiscalCode());
    request.setIdPA(request.getFiscalCode());
    request.setIdBrokerPA(broker.getBrokerFiscalCode());
    request.setIdStation(broker.getStationId() + "xxx");

    Mockito.when(organizationServiceMock.getOrganizationByFiscalCode(request.getIdPA(), VALID_ACCEESS_TOKEN)).thenReturn(organization);
    Mockito.when(brokerServiceMock.getBrokerById(organization.getBrokerId(), VALID_ACCEESS_TOKEN)).thenReturn(broker);

    // When
    PagoPaNodeFaultException response = Assertions.assertThrows(PagoPaNodeFaultException.class, () -> paForNodeRequestValidatorService.paForNodeRequestValidate(request, VALID_ACCEESS_TOKEN));

    // Then
    Assertions.assertEquals(PagoPaNodeFaults.PAA_STAZIONE_INT_ERRATA, response.getErrorCode());
    Assertions.assertEquals(broker.getBrokerFiscalCode(), response.getErrorEmitter());
  }

  //endregion

}
