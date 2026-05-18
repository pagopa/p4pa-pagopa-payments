package it.gov.pagopa.pu.pagopapayments.service;

import it.gov.pagopa.pu.organization.dto.generated.Broker;
import it.gov.pagopa.pu.organization.dto.generated.BrokerApiKeys;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.organization.dto.generated.OrganizationStationDTO;
import it.gov.pagopa.pu.pagopapayments.connector.organization.BrokerService;
import it.gov.pagopa.pu.pagopapayments.connector.organization.OrganizationService;
import it.gov.pagopa.pu.pagopapayments.dto.BrokerForNodoPaDTO;
import it.gov.pagopa.pu.pagopapayments.exception.NotFoundException;
import it.gov.pagopa.pu.pagopapayments.service.broker.BrokerRetrieverService;
import it.gov.pagopa.pu.pagopapayments.util.TestUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class BrokerRetrieverServiceTest {
  @Mock
  private BrokerService brokerServiceMock;
  @Mock
  private OrganizationService organizationServiceMock;

  @InjectMocks
  private BrokerRetrieverService brokerRetrieverService;

  private static final Long VALID_ORG_ID = 1L;
  private static final Long INVALID_ORG_ID = 2L;
  private static final Long VALID_BROKER_ID = 10L;
  private static final String VALID_SEGREGATION_CODE = "01";
  private static final Organization VALID_ORG = new Organization()
    .organizationId(VALID_ORG_ID)
    .brokerId(VALID_BROKER_ID);
  private static final OrganizationStationDTO VALID_ORG_STATION = new OrganizationStationDTO()
    .organizationId(VALID_ORG_ID)
    .brokerId(VALID_BROKER_ID)
    .segregationCode(VALID_SEGREGATION_CODE);
  private static final BrokerApiKeys VALID_API_KEYS = new BrokerApiKeys()
    .acaKey("validAcaKey")
    .syncKey("validSyncKey");

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(
      brokerServiceMock,
      organizationServiceMock
    );
  }

  @Test
  void givenValidOrganizationWhenGetBrokerApiKeyAndSegregationCodesByOrganizationIdThenOk() {
    //given
    String accessToken = TestUtils.getFakeAccessToken();
    Mockito.when(organizationServiceMock.findOrganizationStation(VALID_ORG_ID, null, accessToken))
      .thenReturn(Optional.of(VALID_ORG_STATION));
    Mockito.when(brokerServiceMock.getApiKeyByBrokerId(VALID_BROKER_ID, accessToken)).thenReturn(VALID_API_KEYS);

    //when
    Pair<BrokerApiKeys, String> response = brokerRetrieverService.getBrokerApiKeyAndSegregationCodesByOrganizationId(VALID_ORG_ID, accessToken);

    //verify
    Assertions.assertNotNull(response);
    Assertions.assertEquals(VALID_API_KEYS, response.getLeft());
    Assertions.assertEquals(VALID_SEGREGATION_CODE, response.getRight());
  }

  @Test
  void givenNotFoundOrganizationStationWhenGetBrokerApiKeyAndSegregationCodesByOrganizationIdThenException() {
    //given
    String accessToken = TestUtils.getFakeAccessToken();
    Mockito.when(organizationServiceMock.findOrganizationStation(INVALID_ORG_ID, null, accessToken))
      .thenReturn(Optional.empty());

    //when
    NotFoundException exception = Assertions.assertThrows(NotFoundException.class, () -> brokerRetrieverService.getBrokerApiKeyAndSegregationCodesByOrganizationId(INVALID_ORG_ID, accessToken));

    //verify
    Assertions.assertEquals("ORGANIZATION_STATION_NOT_FOUND", exception.getCode());
  }

  @Test
  void givenValidBrokerIdWhenGetBrokerThenReturnBrokerForNodoPaDTO() {
    String accessToken = TestUtils.getFakeAccessToken();
    Broker broker = new Broker();

    Mockito.when(organizationServiceMock.getOrganizationById(VALID_ORG_ID, accessToken)).thenReturn(VALID_ORG);
    Mockito.when(brokerServiceMock.getBrokerById(VALID_BROKER_ID, accessToken)).thenReturn(broker);
    Mockito.when(brokerServiceMock.getApiKeyByBrokerId(VALID_BROKER_ID, accessToken)).thenReturn(VALID_API_KEYS);

    BrokerForNodoPaDTO result = brokerRetrieverService.getBrokerForNodoPaDTOByOrganizationId(VALID_ORG_ID, accessToken);

    Assertions.assertNotNull(result);
    Assertions.assertSame(broker, result.getBroker());
    Assertions.assertEquals(VALID_API_KEYS, result.getBrokerApiKeys());
    Assertions.assertEquals(VALID_ORG, result.getOrganization());
  }


  @Test
  void givenNotFoundOrganizationWhenGetBrokerForNodoPaDTOByOrganizationIdThenException() {
    //given
    String accessToken = TestUtils.getFakeAccessToken();
    Mockito.when(organizationServiceMock.getOrganizationById(INVALID_ORG_ID, accessToken)).thenReturn(null);
    //when
    NotFoundException exception = Assertions.assertThrows(NotFoundException.class, () -> brokerRetrieverService.getBrokerForNodoPaDTOByOrganizationId(INVALID_ORG_ID, accessToken));
    //verify
    Assertions.assertEquals("ORGANIZATION_NOT_FOUND", exception.getCode());
    Assertions.assertEquals("organization [%s]".formatted(INVALID_ORG_ID), exception.getMessage());
  }

}
