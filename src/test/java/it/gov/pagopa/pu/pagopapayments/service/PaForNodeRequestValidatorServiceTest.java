package it.gov.pagopa.pu.pagopapayments.service;

import it.gov.pagopa.pu.organization.dto.generated.Broker;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.organization.dto.generated.OrganizationStatus;
import it.gov.pagopa.pu.organization.dto.generated.Station;
import it.gov.pagopa.pu.pagopapayments.connector.organization.BrokerService;
import it.gov.pagopa.pu.pagopapayments.connector.organization.OrganizationService;
import it.gov.pagopa.pu.pagopapayments.connector.organization.StationService;
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
  @Mock
  private StationService stationServiceMock;

  @InjectMocks
  private PaForNodeRequestValidatorService paForNodeRequestValidatorService;

  private final PodamFactory podamFactory;

  PaForNodeRequestValidatorServiceTest() {
    podamFactory = TestUtils.getPodamFactory();
  }

  private static final String VALID_ACCESS_TOKEN = "VALID_ACCESS_TOKEN";

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(
      brokerServiceMock,
      organizationServiceMock,
      stationServiceMock
    );
  }

  //region paymentRequestValidate
  @Test
  void givenValidRequestWhenPaForNodeRequestValidateThenOk() {
    // Given
    Organization organization = podamFactory.manufacturePojo(Organization.class);
    Broker broker = podamFactory.manufacturePojo(Broker.class);
    Station station = podamFactory.manufacturePojo(Station.class);

    broker.setFlagDelegate(false);
    organization.setBrokerId(broker.getBrokerId());
    organization.setStatus(OrganizationStatus.ACTIVE);

    RetrievePaymentDTO request = RetrievePaymentDTO.builder()
      .idStation(broker.getStationId())
      .fiscalCode(organization.getOrgFiscalCode())
      .idPA(organization.getOrgFiscalCode())
      .noticeNumber("NAV")
      .idBrokerPA(broker.getBrokerFiscalCode())
      .build();

    Mockito.when(brokerServiceMock.getBrokerByBrokerFiscalCode(broker.getBrokerFiscalCode(), VALID_ACCESS_TOKEN))
      .thenReturn(broker);
    Mockito.when(organizationServiceMock.getOrganizationByFiscalCode(organization.getOrgFiscalCode(), VALID_ACCESS_TOKEN))
      .thenReturn(organization);
    Mockito.when(stationServiceMock.getStationByBrokerIdAndStationId(broker.getBrokerId(), request.getIdStation(), VALID_ACCESS_TOKEN))
      .thenReturn(station);

    // When
    Pair<Broker, Organization> response = paForNodeRequestValidatorService.paForNodeRequestValidate(request, VALID_ACCESS_TOKEN);

    // Then
    Assertions.assertTrue(new ReflectionEquals(Pair.of(broker, organization)).matches(response));
  }

  @Test
  void givenValidRequestOnBroadcastStationWhenPaForNodeRequestValidateThenOk() {
    // Given
    Organization organization = podamFactory.manufacturePojo(Organization.class);
    Broker broker = podamFactory.manufacturePojo(Broker.class);
    Station station = podamFactory.manufacturePojo(Station.class);

    broker.setFlagDelegate(false);
    organization.setBrokerId(broker.getBrokerId());
    organization.setStatus(OrganizationStatus.ACTIVE);

    RetrievePaymentDTO request = RetrievePaymentDTO.builder()
      .idStation(broker.getBroadcastStationId())
      .fiscalCode(organization.getOrgFiscalCode())
      .idPA(organization.getOrgFiscalCode())
      .noticeNumber("NAV")
      .idBrokerPA(broker.getBrokerFiscalCode())
      .build();

    Mockito.when(brokerServiceMock.getBrokerByBrokerFiscalCode(broker.getBrokerFiscalCode(), VALID_ACCESS_TOKEN))
      .thenReturn(broker);
    Mockito.when(organizationServiceMock.getOrganizationByFiscalCode(organization.getOrgFiscalCode(), VALID_ACCESS_TOKEN))
      .thenReturn(organization);
    Mockito.when(stationServiceMock.getStationByBrokerIdAndStationId(broker.getBrokerId(), request.getIdStation(), VALID_ACCESS_TOKEN))
      .thenReturn(null);
    Mockito.when(stationServiceMock.getStationByBrokerIdAndBroadcastStationId(broker.getBrokerId(), request.getIdStation(), VALID_ACCESS_TOKEN))
      .thenReturn(station);

    // When
    Pair<Broker, Organization> response = paForNodeRequestValidatorService.paForNodeRequestValidate(request, VALID_ACCESS_TOKEN);

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

    Mockito.when(brokerServiceMock.getBrokerByBrokerFiscalCode(request.getIdBrokerPA(), VALID_ACCESS_TOKEN))
      .thenReturn(broker);
    Mockito.when(organizationServiceMock.getOrganizationByFiscalCode(request.getIdPA(), VALID_ACCESS_TOKEN))
      .thenReturn(null);

    // When
    PagoPaNodeFaultException response = Assertions.assertThrows(
      PagoPaNodeFaultException.class,
      () -> paForNodeRequestValidatorService.paForNodeRequestValidate(request, VALID_ACCESS_TOKEN)
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

    Mockito.when(brokerServiceMock.getBrokerByBrokerFiscalCode(request.getIdBrokerPA(), VALID_ACCESS_TOKEN))
      .thenReturn(broker);

    Mockito.when(organizationServiceMock.getOrganizationByFiscalCode(request.getIdPA(), VALID_ACCESS_TOKEN))
      .thenReturn(organization);

    // When
    PagoPaNodeFaultException response = Assertions.assertThrows(
      PagoPaNodeFaultException.class,
      () -> paForNodeRequestValidatorService.paForNodeRequestValidate(request, VALID_ACCESS_TOKEN)
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

    Mockito.when(brokerServiceMock.getBrokerByBrokerFiscalCode(request.getIdBrokerPA(), VALID_ACCESS_TOKEN))
      .thenReturn(broker);
    Mockito.when(organizationServiceMock.getOrganizationByFiscalCode(request.getIdPA(), VALID_ACCESS_TOKEN))
      .thenReturn(organization);

    // When
    PagoPaNodeFaultException response = Assertions.assertThrows(
      PagoPaNodeFaultException.class,
      () -> paForNodeRequestValidatorService.paForNodeRequestValidate(request, VALID_ACCESS_TOKEN)
    );

    // Then
    Assertions.assertEquals(PagoPaNodeFaults.PAA_ID_INTERMEDIARIO_ERRATO, response.getErrorCode());
    Assertions.assertEquals(broker.getBrokerFiscalCode(), response.getErrorEmitter());
  }

  @Test
  void givenInvalidStationWhenPaForNodeRequestValidateThenOk() {
    // Given
    Broker broker = podamFactory.manufacturePojo(Broker.class);
    broker.setFlagDelegate(false);

    Organization organization = podamFactory.manufacturePojo(Organization.class);
    organization.setStatus(OrganizationStatus.ACTIVE);

    RetrievePaymentDTO request = podamFactory.manufacturePojo(RetrievePaymentDTO.class);
    request.setIdPA(organization.getOrgFiscalCode());
    request.setIdBrokerPA(broker.getBrokerFiscalCode());
    request.setIdStation(broker.getStationId() + "xxx");

    Mockito.when(brokerServiceMock.getBrokerByBrokerFiscalCode(request.getIdBrokerPA(), VALID_ACCESS_TOKEN))
      .thenReturn(broker);
    Mockito.when(organizationServiceMock.getOrganizationByFiscalCode(request.getIdPA(), VALID_ACCESS_TOKEN))
      .thenReturn(organization);
    Mockito.when(stationServiceMock.getStationByBrokerIdAndStationId(broker.getBrokerId(), request.getIdStation(), VALID_ACCESS_TOKEN))
      .thenReturn(null);
    Mockito.when(stationServiceMock.getStationByBrokerIdAndBroadcastStationId(broker.getBrokerId(), request.getIdStation(), VALID_ACCESS_TOKEN))
      .thenReturn(null);

    // When
    PagoPaNodeFaultException response = Assertions.assertThrows(
      PagoPaNodeFaultException.class,
      () -> paForNodeRequestValidatorService.paForNodeRequestValidate(request, VALID_ACCESS_TOKEN)
    );

    // Then
    Assertions.assertEquals(PagoPaNodeFaults.PAA_STAZIONE_INT_ERRATA, response.getErrorCode());
    Assertions.assertEquals(request.getIdBrokerPA(), response.getErrorEmitter());
  }

  @Test
  void givenDelegateBrokerWhenPaForNodeRequestValidateThenReturnAssociatedOrganization() {
    // Given
    Broker broker = podamFactory.manufacturePojo(Broker.class);
    broker.setFlagDelegate(true);

    Organization orgAssociated = podamFactory.manufacturePojo(Organization.class);
    orgAssociated.setStatus(OrganizationStatus.ACTIVE);

    Station station = podamFactory.manufacturePojo(Station.class);

    RetrievePaymentDTO request = RetrievePaymentDTO.builder()
      .idStation(broker.getStationId())
      .idPA("ANY_IDPA")
      .fiscalCode("ANY_FISCAL_CODE")
      .noticeNumber("NAV")
      .idBrokerPA(broker.getBrokerFiscalCode())
      .build();

    Mockito.when(brokerServiceMock.getBrokerByBrokerFiscalCode(request.getIdBrokerPA(), VALID_ACCESS_TOKEN))
      .thenReturn(broker);
    Mockito.when(organizationServiceMock.getOrganizationById(broker.getOrganizationId(), VALID_ACCESS_TOKEN))
      .thenReturn(orgAssociated);
    Mockito.when(stationServiceMock.getStationByBrokerIdAndStationId(broker.getBrokerId(), request.getIdStation(), VALID_ACCESS_TOKEN))
      .thenReturn(station);

    // When
    Pair<Broker, Organization> response =
      paForNodeRequestValidatorService.paForNodeRequestValidate(request, VALID_ACCESS_TOKEN);

    // Then
    Assertions.assertTrue(new ReflectionEquals(Pair.of(broker, orgAssociated)).matches(response));

    Mockito.verify(brokerServiceMock, Mockito.times(1))
      .getBrokerByBrokerFiscalCode(request.getIdBrokerPA(), VALID_ACCESS_TOKEN);

    Mockito.verify(organizationServiceMock, Mockito.times(1))
      .getOrganizationById(broker.getOrganizationId(), VALID_ACCESS_TOKEN);

    Mockito.verify(organizationServiceMock, Mockito.never())
      .getOrganizationByFiscalCode(Mockito.anyString(), Mockito.anyString());
  }

  @Test
  void givenDelegateBrokerAndOrgAssociatedNotFoundWhenPaForNodeRequestValidateThenFault() {
    // Given
    Broker broker = podamFactory.manufacturePojo(Broker.class);
    broker.setFlagDelegate(true);

    RetrievePaymentDTO request = RetrievePaymentDTO.builder()
      .idStation(broker.getStationId())
      .idPA("ANY_IDPA")
      .fiscalCode("ANY_FISCAL_CODE")
      .noticeNumber("NAV")
      .idBrokerPA(broker.getBrokerFiscalCode())
      .build();

    Mockito.when(brokerServiceMock.getBrokerByBrokerFiscalCode(request.getIdBrokerPA(), VALID_ACCESS_TOKEN))
      .thenReturn(broker);

    Mockito.when(organizationServiceMock.getOrganizationById(broker.getOrganizationId(), VALID_ACCESS_TOKEN))
      .thenReturn(null);

    // When
    PagoPaNodeFaultException ex = Assertions.assertThrows(
      PagoPaNodeFaultException.class,
      () -> paForNodeRequestValidatorService.paForNodeRequestValidate(request, VALID_ACCESS_TOKEN)
    );

    // Then
    Assertions.assertEquals(PagoPaNodeFaults.PAA_SYSTEM_ERROR, ex.getErrorCode());
    Assertions.assertEquals(request.getIdPA(), ex.getErrorEmitter());

    Mockito.verify(brokerServiceMock, Mockito.times(1))
      .getBrokerByBrokerFiscalCode(request.getIdBrokerPA(), VALID_ACCESS_TOKEN);
    Mockito.verify(organizationServiceMock, Mockito.times(1))
      .getOrganizationById(broker.getOrganizationId(), VALID_ACCESS_TOKEN);

    Mockito.verify(organizationServiceMock, Mockito.never())
      .getOrganizationByFiscalCode(Mockito.anyString(), Mockito.anyString());
  }

  @Test
  void givenNotFoundBrokerWhenPaForNodeRequestValidateThenFault() {
    RetrievePaymentDTO request = podamFactory.manufacturePojo(RetrievePaymentDTO.class);
    String brokerFiscalCode = request.getIdBrokerPA();

    Mockito.when(brokerServiceMock.getBrokerByBrokerFiscalCode(brokerFiscalCode, VALID_ACCESS_TOKEN))
      .thenReturn(null);

    PagoPaNodeFaultException response = Assertions.assertThrows(
      PagoPaNodeFaultException.class,
      () -> paForNodeRequestValidatorService.paForNodeRequestValidate(request, VALID_ACCESS_TOKEN)
    );

    Assertions.assertEquals(PagoPaNodeFaults.PAA_ID_INTERMEDIARIO_ERRATO, response.getErrorCode());
    Assertions.assertEquals(brokerFiscalCode, response.getErrorEmitter());
  }
  //endregion

  // region paSendRtRequestValidate
  @Test
  void givenValidPaSendRtRequestWhenPaSendRtRequestValidateThenOk() {
    Broker broker = podamFactory.manufacturePojo(Broker.class);
    broker.setFlagDelegate(false);

    Organization organization = podamFactory.manufacturePojo(Organization.class);
    organization.setBrokerId(broker.getBrokerId());
    organization.setStatus(OrganizationStatus.ACTIVE);
    organization.setOrganizationId(100L);

    Station station = podamFactory.manufacturePojo(Station.class);

    PaSendRtDTO request = podamFactory.manufacturePojo(PaSendRtDTO.class);
    request.setIdPA(organization.getOrgFiscalCode());
    request.setIdBrokerPA(broker.getBrokerFiscalCode());
    request.setIdStation(broker.getStationId());

    Mockito.when(brokerServiceMock.getBrokerByBrokerFiscalCode(request.getIdBrokerPA(), VALID_ACCESS_TOKEN)).thenReturn(broker);
    Mockito.when(organizationServiceMock.getOrganizationByFiscalCode(request.getIdPA(), VALID_ACCESS_TOKEN)).thenReturn(organization);
    Mockito.when(stationServiceMock.getStationByBrokerIdAndStationId(broker.getBrokerId(), request.getIdStation(), VALID_ACCESS_TOKEN))
      .thenReturn(station);

    Organization response = paForNodeRequestValidatorService.paSendRtRequestValidate(request, VALID_ACCESS_TOKEN);

    Assertions.assertNotNull(response);
    Assertions.assertEquals(organization.getOrgFiscalCode(), response.getOrgFiscalCode());
  }

  @Test
  void givenExternalOrgWithManagedTransferWhenPaSendRtRequestValidateThenOk() {
    PaSendRtDTO request = podamFactory.manufacturePojo(PaSendRtDTO.class);
    String managedTransferFiscalCode = "FISCAL_CODE";
    request.getTransferList().getFirst().setFiscalCodePA(managedTransferFiscalCode);

    Broker broker = podamFactory.manufacturePojo(Broker.class);
    broker.setFlagDelegate(false);
    request.setIdBrokerPA(broker.getBrokerFiscalCode());
    request.setIdStation(broker.getStationId());

    Organization technicalOrg = new Organization();
    technicalOrg.setOrganizationId(-1L);
    technicalOrg.setStatus(OrganizationStatus.ACTIVE);
    technicalOrg.setOrgFiscalCode("TECHNICAL_ORG_FISCAL_CODE");

    Station station = podamFactory.manufacturePojo(Station.class);

    Mockito.when(brokerServiceMock.getBrokerByBrokerFiscalCode(request.getIdBrokerPA(), VALID_ACCESS_TOKEN)).thenReturn(broker);
    Mockito.when(organizationServiceMock.getOrganizationByFiscalCode(request.getIdPA(), VALID_ACCESS_TOKEN)).thenReturn(null);
    Mockito.when(organizationServiceMock.getOrganizationByFiscalCode(managedTransferFiscalCode, VALID_ACCESS_TOKEN)).thenReturn(new Organization());
    Mockito.when(organizationServiceMock.getOrganizationById(-1L, VALID_ACCESS_TOKEN)).thenReturn(technicalOrg);
    Mockito.when(stationServiceMock.getStationByBrokerIdAndStationId(broker.getBrokerId(), request.getIdStation(), VALID_ACCESS_TOKEN))
      .thenReturn(station);

    Organization response = paForNodeRequestValidatorService.paSendRtRequestValidate(request, VALID_ACCESS_TOKEN);

    Assertions.assertEquals(-1L, response.getOrganizationId());
  }

  @Test
  void givenNoOrgAndNoManagedTransfersWhenPaSendRtRequestValidateThenThrowFault() {
    PaSendRtDTO request = podamFactory.manufacturePojo(PaSendRtDTO.class);

    Broker broker = podamFactory.manufacturePojo(Broker.class);
    broker.setFlagDelegate(false);
    request.setIdBrokerPA(broker.getBrokerFiscalCode());

    Mockito.when(brokerServiceMock.getBrokerByBrokerFiscalCode(request.getIdBrokerPA(), VALID_ACCESS_TOKEN)).thenReturn(broker);
    Mockito.when(organizationServiceMock.getOrganizationByFiscalCode(Mockito.anyString(), Mockito.eq(VALID_ACCESS_TOKEN)))
      .thenReturn(null);

    PagoPaNodeFaultException ex = Assertions.assertThrows(PagoPaNodeFaultException.class,
      () -> paForNodeRequestValidatorService.paSendRtRequestValidate(request, VALID_ACCESS_TOKEN));

    Assertions.assertEquals(PagoPaNodeFaults.PAA_ID_DOMINIO_ERRATO, ex.getErrorCode());
  }

  @Test
  void givenDelegateBrokerWhenPaSendRtRequestValidateThenReturnAssociatedOrganization() {
    Broker broker = podamFactory.manufacturePojo(Broker.class);
    broker.setFlagDelegate(true);

    Organization orgAssociated = podamFactory.manufacturePojo(Organization.class);
    orgAssociated.setOrganizationId(123L);
    orgAssociated.setStatus(OrganizationStatus.ACTIVE);

    Station station = podamFactory.manufacturePojo(Station.class);

    PaSendRtDTO request = podamFactory.manufacturePojo(PaSendRtDTO.class);
    request.setIdStation(broker.getStationId());
    request.setIdBrokerPA(broker.getBrokerFiscalCode());
    request.setIdPA("ANY_ID_PA");

    Mockito.when(brokerServiceMock.getBrokerByBrokerFiscalCode(request.getIdBrokerPA(), VALID_ACCESS_TOKEN))
      .thenReturn(broker);
    Mockito.when(organizationServiceMock.getOrganizationById(broker.getOrganizationId(), VALID_ACCESS_TOKEN))
      .thenReturn(orgAssociated);
    Mockito.when(stationServiceMock.getStationByBrokerIdAndStationId(broker.getBrokerId(), request.getIdStation(), VALID_ACCESS_TOKEN))
      .thenReturn(station);

    Organization response = paForNodeRequestValidatorService.paSendRtRequestValidate(request, VALID_ACCESS_TOKEN);

    Assertions.assertEquals(orgAssociated.getOrganizationId(), response.getOrganizationId());
  }
  // endregion
}
