package it.gov.pagopa.pu.pagopapayments.service;

import it.gov.pagopa.pu.organization.dto.generated.Broker;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.organization.dto.generated.OrganizationStatus;
import it.gov.pagopa.pu.pagopapayments.connector.organization.BrokerService;
import it.gov.pagopa.pu.pagopapayments.connector.organization.OrganizationService;
import it.gov.pagopa.pu.pagopapayments.dto.PaSendRtDTO;
import it.gov.pagopa.pu.pagopapayments.dto.RetrievePaymentDTO;
import it.gov.pagopa.pu.pagopapayments.enums.PagoPaNodeFaults;
import it.gov.pagopa.pu.pagopapayments.exception.PagoPaNodeFaultException;
import it.gov.pagopa.pu.pagopapayments.util.TestUtils;
import org.apache.commons.lang3.tuple.Pair;
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

    broker.setFlagDelegate(false);
    organization.setBrokerId(broker.getBrokerId());
    organization.setStatus(OrganizationStatus.ACTIVE);

    Mockito.when(brokerServiceMock.getBrokerByStationId(broker.getStationId(), VALID_ACCEESS_TOKEN))
      .thenReturn(broker);
    Mockito.when(organizationServiceMock.getOrganizationByFiscalCode(organization.getOrgFiscalCode(), VALID_ACCEESS_TOKEN))
      .thenReturn(organization);
    Mockito.when(brokerServiceMock.getBrokerById(broker.getBrokerId(), VALID_ACCEESS_TOKEN))
      .thenReturn(broker);

    RetrievePaymentDTO request = RetrievePaymentDTO.builder()
      .idStation(broker.getStationId())
      .fiscalCode(organization.getOrgFiscalCode())
      .idPA(organization.getOrgFiscalCode())
      .noticeNumber("NAV")
      .idBrokerPA(broker.getBrokerFiscalCode())
      .build();

    // When
    Pair<Broker, Organization> response = paForNodeRequestValidatorService.paForNodeRequestValidate(request, VALID_ACCEESS_TOKEN);

    // Then
    Assertions.assertTrue(new ReflectionEquals(Pair.of(broker, organization)).matches(response));
  }

  @Test
  void givenValidRequestOnBroadcastStationWhenPaForNodeRequestValidateThenOk() {
    // Given
    Organization organization = podamFactory.manufacturePojo(Organization.class);
    Broker broker = podamFactory.manufacturePojo(Broker.class);

    broker.setFlagDelegate(false);
    organization.setBrokerId(broker.getBrokerId());
    organization.setStatus(OrganizationStatus.ACTIVE);

    Mockito.when(brokerServiceMock.getBrokerByStationId(broker.getBroadcastStationId(), VALID_ACCEESS_TOKEN))
      .thenReturn(broker);
    Mockito.when(organizationServiceMock.getOrganizationByFiscalCode(organization.getOrgFiscalCode(), VALID_ACCEESS_TOKEN))
      .thenReturn(organization);
    Mockito.when(brokerServiceMock.getBrokerById(broker.getBrokerId(), VALID_ACCEESS_TOKEN))
      .thenReturn(broker);

    RetrievePaymentDTO request = RetrievePaymentDTO.builder()
      .idStation(broker.getBroadcastStationId())
      .fiscalCode(organization.getOrgFiscalCode())
      .idPA(organization.getOrgFiscalCode())
      .noticeNumber("NAV")
      .idBrokerPA(broker.getBrokerFiscalCode())
      .build();

    // When
    Pair<Broker, Organization> response = paForNodeRequestValidatorService.paForNodeRequestValidate(request, VALID_ACCEESS_TOKEN);

    // Then
    Assertions.assertTrue(new ReflectionEquals(Pair.of(broker, organization)).matches(response));
  }

  @Test
  void givenNotFoundOrgWhenPaForNodeRequestValidateThenFault() {
    // Given
    RetrievePaymentDTO request = podamFactory.manufacturePojo(RetrievePaymentDTO.class);
    request.setIdPA(request.getFiscalCode());

    Broker broker = podamFactory.manufacturePojo(Broker.class);
    broker.setFlagDelegate(false);
    request.setIdStation(broker.getStationId());

    Mockito.when(brokerServiceMock.getBrokerByStationId(request.getIdStation(), VALID_ACCEESS_TOKEN))
      .thenReturn(broker);
    Mockito.when(organizationServiceMock.getOrganizationByFiscalCode(request.getIdPA(), VALID_ACCEESS_TOKEN))
      .thenReturn(null);

    // When
    PagoPaNodeFaultException response = Assertions.assertThrows(
      PagoPaNodeFaultException.class,
      () -> paForNodeRequestValidatorService.paForNodeRequestValidate(request, VALID_ACCEESS_TOKEN)
    );

    // Then
    Assertions.assertEquals(PagoPaNodeFaults.PAA_ID_DOMINIO_ERRATO, response.getErrorCode());
    Assertions.assertEquals(request.getIdBrokerPA(), response.getErrorEmitter());
  }

  @Test
  void givenNotActiveOrgWhenPaForNodeRequestValidateThenOk() {
    // Given
    RetrievePaymentDTO request = podamFactory.manufacturePojo(RetrievePaymentDTO.class);
    request.setIdPA(request.getFiscalCode());

    Broker broker = podamFactory.manufacturePojo(Broker.class);
    broker.setFlagDelegate(false);
    request.setIdStation(broker.getStationId());

    Organization organization = podamFactory.manufacturePojo(Organization.class);
    organization.setStatus(OrganizationStatus.DRAFT);
    organization.setOrgFiscalCode(request.getFiscalCode());
    organization.setBrokerId(broker.getBrokerId());

    Mockito.when(brokerServiceMock.getBrokerByStationId(request.getIdStation(), VALID_ACCEESS_TOKEN))
      .thenReturn(broker);

    Mockito.when(organizationServiceMock.getOrganizationByFiscalCode(request.getIdPA(), VALID_ACCEESS_TOKEN))
      .thenReturn(organization);

    // When
    PagoPaNodeFaultException response = Assertions.assertThrows(
      PagoPaNodeFaultException.class,
      () -> paForNodeRequestValidatorService.paForNodeRequestValidate(request, VALID_ACCEESS_TOKEN)
    );

    // Then
    Assertions.assertEquals(PagoPaNodeFaults.PAA_ID_DOMINIO_ERRATO, response.getErrorCode());
    Assertions.assertEquals(organization.getOrgFiscalCode(), response.getErrorEmitter());
  }

  @Test
  void givenInvalidBrokerWhenPaForNodeRequestValidateThenOk() {
    // Given
    Organization organization = podamFactory.manufacturePojo(Organization.class);
    organization.setStatus(OrganizationStatus.ACTIVE);

    Broker broker = podamFactory.manufacturePojo(Broker.class);
    broker.setFlagDelegate(false);
    organization.setBrokerId(broker.getBrokerId());

    RetrievePaymentDTO request = podamFactory.manufacturePojo(RetrievePaymentDTO.class);
    request.setFiscalCode(organization.getOrgFiscalCode());
    request.setIdPA(request.getFiscalCode());
    request.setIdStation(broker.getStationId());
    request.setIdBrokerPA(broker.getBrokerFiscalCode() + "xxx");

    Mockito.when(brokerServiceMock.getBrokerByStationId(request.getIdStation(), VALID_ACCEESS_TOKEN))
      .thenReturn(broker);
    Mockito.when(organizationServiceMock.getOrganizationByFiscalCode(request.getIdPA(), VALID_ACCEESS_TOKEN))
      .thenReturn(organization);
    Mockito.when(brokerServiceMock.getBrokerById(organization.getBrokerId(), VALID_ACCEESS_TOKEN))
      .thenReturn(broker);

    // When
    PagoPaNodeFaultException response = Assertions.assertThrows(
      PagoPaNodeFaultException.class,
      () -> paForNodeRequestValidatorService.paForNodeRequestValidate(request, VALID_ACCEESS_TOKEN)
    );

    // Then
    Assertions.assertEquals(PagoPaNodeFaults.PAA_ID_INTERMEDIARIO_ERRATO, response.getErrorCode());
    Assertions.assertEquals(broker.getBrokerFiscalCode(), response.getErrorEmitter());
  }

  @Test
  void givenInvalidStationWhenPaForNodeRequestValidateThenOk() {
    // Given
    Broker broker = podamFactory.manufacturePojo(Broker.class);

    RetrievePaymentDTO request = podamFactory.manufacturePojo(RetrievePaymentDTO.class);
    request.setIdPA(request.getFiscalCode());
    request.setIdBrokerPA(broker.getBrokerFiscalCode());
    request.setIdStation(broker.getStationId() + "xxx");

    Mockito.when(brokerServiceMock.getBrokerByStationId(request.getIdStation(), VALID_ACCEESS_TOKEN))
      .thenReturn(null);

    // When
    PagoPaNodeFaultException response = Assertions.assertThrows(
      PagoPaNodeFaultException.class,
      () -> paForNodeRequestValidatorService.paForNodeRequestValidate(request, VALID_ACCEESS_TOKEN)
    );

    // Then
    Assertions.assertEquals(PagoPaNodeFaults.PAA_STAZIONE_INT_ERRATA, response.getErrorCode());
    Assertions.assertEquals(request.getIdStation(), response.getErrorEmitter());
  }

  //endregion

  // region paSendRtRequestValidate

  @Test
  void givenValidPaSendRtRequestWhenPaSendRtRequestValidateThenOk() {
    Organization organization = podamFactory.manufacturePojo(Organization.class);
    Broker broker = podamFactory.manufacturePojo(Broker.class);
    organization.setBrokerId(broker.getBrokerId());
    organization.setStatus(OrganizationStatus.ACTIVE);
    organization.setOrganizationId(100L);

    PaSendRtDTO request = podamFactory.manufacturePojo(PaSendRtDTO.class);
    request.setIdPA(organization.getOrgFiscalCode());
    request.setIdBrokerPA(broker.getBrokerFiscalCode());
    request.setIdStation(broker.getStationId());

    Mockito.when(organizationServiceMock.getOrganizationByFiscalCode(request.getIdPA(), VALID_ACCEESS_TOKEN)).thenReturn(organization);
    Mockito.when(brokerServiceMock.getBrokerById(broker.getBrokerId(), VALID_ACCEESS_TOKEN)).thenReturn(broker);

    Organization response = paForNodeRequestValidatorService.paSendRtRequestValidate(request, VALID_ACCEESS_TOKEN);

    Assertions.assertNotNull(response);
    Assertions.assertEquals(organization.getOrgFiscalCode(), response.getOrgFiscalCode());
    Mockito.verify(brokerServiceMock).getBrokerById(organization.getBrokerId(), VALID_ACCEESS_TOKEN);
  }

  @Test
  void givenExternalOrgWithManagedTransferWhenPaSendRtRequestValidateThenOk() {
    PaSendRtDTO request = podamFactory.manufacturePojo(PaSendRtDTO.class);
    String managedTransferFiscalCode = "FISCAL_CODE";
    request.getTransferList().getFirst().setFiscalCodePA(managedTransferFiscalCode);

    Organization technicalOrg = new Organization();
    technicalOrg.setOrganizationId(-1L);
    technicalOrg.setStatus(OrganizationStatus.ACTIVE);
    technicalOrg.setOrgFiscalCode("TECHNICAL_ORG_FISCAL_CODE");

    Mockito.when(organizationServiceMock.getOrganizationByFiscalCode(request.getIdPA(), VALID_ACCEESS_TOKEN)).thenReturn(null);
    Mockito.when(organizationServiceMock.getOrganizationByFiscalCode(managedTransferFiscalCode, VALID_ACCEESS_TOKEN)).thenReturn(new Organization());
    Mockito.when(organizationServiceMock.getOrganizationById(-1L, VALID_ACCEESS_TOKEN)).thenReturn(technicalOrg);

    Organization response = paForNodeRequestValidatorService.paSendRtRequestValidate(request, VALID_ACCEESS_TOKEN);

    Assertions.assertEquals(-1L, response.getOrganizationId());
    Mockito.verifyNoInteractions(brokerServiceMock);
  }

  @Test
  void givenNoOrgAndNoManagedTransfersWhenPaSendRtRequestValidateThenThrowFault() {
    PaSendRtDTO request = podamFactory.manufacturePojo(PaSendRtDTO.class);

    Mockito.when(organizationServiceMock.getOrganizationByFiscalCode(Mockito.anyString(), Mockito.eq(VALID_ACCEESS_TOKEN)))
      .thenReturn(null);

    PagoPaNodeFaultException ex = Assertions.assertThrows(PagoPaNodeFaultException.class,
      () -> paForNodeRequestValidatorService.paSendRtRequestValidate(request, VALID_ACCEESS_TOKEN));

    Assertions.assertEquals(PagoPaNodeFaults.PAA_ID_DOMINIO_ERRATO, ex.getErrorCode());
  }

  // endregion
}
