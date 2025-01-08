package it.gov.pagopa.pu.pagopapayments.service;

import it.gov.pagopa.pu.p4pa_organization.dto.generated.BrokerApiKeys;
import it.gov.pagopa.pu.p4pa_organization.dto.generated.Organization;
import it.gov.pagopa.pu.pagopapayments.connector.OrganizationClientImpl;
import it.gov.pagopa.pu.pagopapayments.exception.NotFoundException;
import it.gov.pagopa.pu.pagopapayments.service.broker.BrokerService;
import it.gov.pagopa.pu.pagopapayments.util.TestUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class BrokerServiceTest {
  @Mock
  private OrganizationClientImpl organizationClientMock;

  @InjectMocks
  private BrokerService brokerService;

  private static final Long VALID_ORG_ID = 1L;
  private static final Long INVALID_ORG_ID = 2L;
  private static final Long VALID_BROKER_ID = 10L;
  private static final String VALID_SEGREGATION_CODE = "01";
  private static final Organization VALID_ORG = new Organization()
    .organizationId(VALID_ORG_ID)
    .brokerId(VALID_BROKER_ID)
    .applicationCode(VALID_SEGREGATION_CODE);
  private static final BrokerApiKeys VALID_API_KEYS = new BrokerApiKeys()
    .acaKey("validAcaKey")
    .syncKey("validSyncKey");

  @Test
  void givenValidOrganizationWhenGetBrokerApiKeyAndSegregationCodesByOrganizationIdThenOk() {
    //given
    Mockito.when(organizationClientMock.getOrganizationById(VALID_ORG_ID, TestUtils.getFakeAccessToken())).thenReturn(VALID_ORG);
    Mockito.when(organizationClientMock.getApiKeyByBrokerId(VALID_BROKER_ID, TestUtils.getFakeAccessToken())).thenReturn(VALID_API_KEYS);
    //when
    Pair<BrokerApiKeys, String> response = brokerService.getBrokerApiKeyAndSegregationCodesByOrganizationId(VALID_ORG_ID, TestUtils.getFakeAccessToken());
    //verify
    Assertions.assertNotNull(response);
    Assertions.assertEquals(VALID_API_KEYS, response.getLeft());
    Assertions.assertEquals(VALID_SEGREGATION_CODE, response.getRight());
    Mockito.verify(organizationClientMock, Mockito.times(1)).getOrganizationById(VALID_ORG_ID, TestUtils.getFakeAccessToken());
    Mockito.verify(organizationClientMock, Mockito.times(1)).getApiKeyByBrokerId(VALID_BROKER_ID, TestUtils.getFakeAccessToken());
  }

  @Test
  void givenNotFoundOrganizationWhenGetBrokerApiKeyAndSegregationCodesByOrganizationIdThenException() {
    //given
    Mockito.when(organizationClientMock.getOrganizationById(INVALID_ORG_ID, TestUtils.getFakeAccessToken())).thenReturn(null);
    //when
    NotFoundException exception = Assertions.assertThrows(NotFoundException.class, () -> brokerService.getBrokerApiKeyAndSegregationCodesByOrganizationId(INVALID_ORG_ID, TestUtils.getFakeAccessToken()));
    //verify
    Assertions.assertEquals("organization [%s]".formatted(INVALID_ORG_ID), exception.getMessage());
    Mockito.verify(organizationClientMock, Mockito.times(1)).getOrganizationById(INVALID_ORG_ID, TestUtils.getFakeAccessToken());
    Mockito.verify(organizationClientMock, Mockito.never()).getApiKeyByBrokerId(Mockito.any(), Mockito.eq(TestUtils.getFakeAccessToken()));
  }
}
