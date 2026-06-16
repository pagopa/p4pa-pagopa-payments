package it.gov.pagopa.pu.pagopapayments.connector.organization;

import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.organization.dto.generated.OrganizationApiKeyType;
import it.gov.pagopa.pu.organization.dto.generated.OrganizationStationDTO;
import it.gov.pagopa.pu.pagopapayments.connector.organization.client.OrganizationClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrganizationServiceTest {

  @Mock
  private OrganizationClient client;

  private OrganizationService service;

  @BeforeEach
  void setUp() {
    service = new OrganizationServiceImpl(client);
  }

  @Test
  void testGetOrganizationById() {
    Organization expected = new Organization();
    Long orgId = 1L;
    String accessToken = "accessToken";

    when(client.getOrganizationById(orgId, accessToken)).thenReturn(expected);

    Organization result = service.getOrganizationById(orgId, accessToken);

    assertEquals(expected, result);
  }

  @Test
  void testGetOrganizationByFiscalCode() {
    Organization expected = new Organization();
    String fiscalCode = "fiscalCode";
    String accessToken = "accessToken";

    when(client.getOrganizationByFiscalCode(fiscalCode, accessToken)).thenReturn(expected);

    Organization result = service.getOrganizationByFiscalCode(fiscalCode, accessToken);

    assertEquals(expected, result);
  }

  @Test
  void whenGetOrganizationApiKeyThenInvokeClient(){
    // Given
    Long organizationId = 1L;
    String accessToken = "accessToken";
    String apiKey = "apiKey";
    String subUnitCode = "CODE";

    Mockito.when(client.getOrganizationApiKey(organizationId, OrganizationApiKeyType.SEND, subUnitCode, accessToken))
      .thenReturn(apiKey);

    // When
    String result = service.getOrganizationApiKey(organizationId, OrganizationApiKeyType.SEND, subUnitCode, accessToken);

    // Then
    Assertions.assertSame(apiKey, result);
  }

  @Test
  void whenFindOrganizationStationThenInvokeClient(){
    // Given
    Long organizationId = 1L;
    String stationId = "STATIONID";
    String accessToken = "accessToken";
    OrganizationStationDTO expectedResult = new OrganizationStationDTO();
    Mockito.when(client.findOrganizationStation(organizationId, stationId, accessToken))
      .thenReturn(expectedResult);

    // When
    Optional<OrganizationStationDTO> result = service.findOrganizationStation(organizationId, stationId, accessToken);

    // Then
    Assertions.assertTrue(result.isPresent());
    assertSame(expectedResult, result.get());
  }
}
