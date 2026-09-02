package it.gov.pagopa.pu.pagopapayments.connector.organization.client;

import it.gov.pagopa.pu.organization.client.generated.OrganizationApi;
import it.gov.pagopa.pu.organization.client.generated.OrganizationEntityControllerApi;
import it.gov.pagopa.pu.organization.client.generated.OrganizationSearchControllerApi;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.organization.dto.generated.OrganizationApiKeyType;
import it.gov.pagopa.pu.organization.dto.generated.OrganizationStationDTO;
import it.gov.pagopa.pu.pagopapayments.connector.organization.config.OrganizationApisHolder;
import it.gov.pagopa.pu.pagopapayments.exception.common.RestInvokeNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrganizationClientTest {
  @Mock
  private OrganizationApisHolder organizationApisHolder;
  @Mock
  private OrganizationEntityControllerApi organizationEntityControllerApiMock;
  @Mock
  private OrganizationSearchControllerApi organizationSearchControllerApiMock;
  @Mock
  private OrganizationApi organizationApiMock;

  private OrganizationClient organizationClient;

  @BeforeEach
  void init() {
    organizationClient = new OrganizationClient(organizationApisHolder);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      organizationApisHolder,
      organizationEntityControllerApiMock,
      organizationSearchControllerApiMock,
      organizationApiMock
    );
  }

  @Test
  void whenGetOrganizationByIdThenInvokeWithAccessToken() {
    // Given
    Long orgID = 1L;
    String accessToken = "ACCESSTOKEN";
    Organization expectedResult = new Organization();

    when(organizationApisHolder.getOrganizationEntityControllerApi(accessToken))
      .thenReturn(organizationEntityControllerApiMock);
    when(organizationEntityControllerApiMock.crudGetOrganization(orgID+""))
      .thenReturn(expectedResult);

    // When
    Organization result = organizationClient.getOrganizationById(orgID, accessToken);

    // Then
    Assertions.assertSame(expectedResult, result);
  }

  @Test
  void givenNoExistentIpaCodeWhenGetOrganizationByIdThenNull() {
    // Given
    Long orgID = 1L;
    String accessToken = "ACCESSTOKEN";

    when(organizationApisHolder.getOrganizationEntityControllerApi(accessToken))
      .thenReturn(organizationEntityControllerApiMock);
    when(organizationEntityControllerApiMock.crudGetOrganization(orgID+""))
      .thenThrow(new RestInvokeNotFoundException("APPNAME", HttpStatus.NOT_FOUND, "ERROR", "ERRORCODE", "ERRORMESSAGE"));

    // When
    Organization result = organizationClient.getOrganizationById(orgID, accessToken);

    // Then
    Assertions.assertNull(result);
  }

  @Test
  void whenGetOrganizationByFiscalCodeThenInvokeWithAccessToken() {
    // Given
    String orgFiscalCode = "ORGIPACODE";
    String accessToken = "ACCESSTOKEN";
    Organization expectedResult = new Organization();

    when(organizationApisHolder.getOrganizationSearchControllerApi(accessToken))
      .thenReturn(organizationSearchControllerApiMock);
    when(organizationSearchControllerApiMock.crudOrganizationsFindByOrgFiscalCode(orgFiscalCode))
      .thenReturn(expectedResult);

    // When
    Organization result = organizationClient.getOrganizationByFiscalCode(orgFiscalCode, accessToken);

    // Then
    Assertions.assertSame(expectedResult, result);
  }

  @Test
  void givenNoExistentFiscalCodeWhenGetOrganizationByFiscalCodeThenNull() {
    // Given
    String orgFiscalCode = "ORGIPACODE";
    String accessToken = "ACCESSTOKEN";

    when(organizationApisHolder.getOrganizationSearchControllerApi(accessToken))
      .thenReturn(organizationSearchControllerApiMock);
    when(organizationSearchControllerApiMock.crudOrganizationsFindByOrgFiscalCode(orgFiscalCode))
      .thenThrow(new RestInvokeNotFoundException("APPNAME", HttpStatus.NOT_FOUND, "ERROR", "ERRORCODE", "ERRORMESSAGE"));

    // When
    Organization result = organizationClient.getOrganizationByFiscalCode(orgFiscalCode, accessToken);

    // Then
    Assertions.assertNull(result);
  }

  @Test
  void givenValidRequestWhenGetOrganizationApiKeyThenVerifyResponse() {
    // Given
    Long organizationId = 1L;
    String accessToken = "ACCESSTOKEN";
    String apiKey = "apiKey";
    String subUnitCode = "CODE";

    when(organizationApisHolder.getOrganizationApi(accessToken))
      .thenReturn(organizationApiMock);
    when(organizationApiMock.getOrganizationApiKey(organizationId, OrganizationApiKeyType.SEND, subUnitCode))
      .thenReturn(apiKey);

    // When
    String result = organizationClient.getOrganizationApiKey(organizationId, OrganizationApiKeyType.SEND, subUnitCode, accessToken);

    // Then
    assertSame(apiKey, result);
  }

  @Test
  void givenNotExistentOrganizationIdWhenGetOrganizationApiKeyThenReturnNull() {
    // Given
    Long organizationId = 1L;
    String accessToken = "ACCESSTOKEN";
    String subUnitCode = "CODE";

    when(organizationApisHolder.getOrganizationApi(accessToken))
      .thenReturn(organizationApiMock);
    when(organizationApiMock.getOrganizationApiKey(organizationId, OrganizationApiKeyType.SEND, subUnitCode))
      .thenThrow(new RestInvokeNotFoundException("APPNAME", HttpStatus.NOT_FOUND, "ERROR", "ERRORCODE", "ERRORMESSAGE"));

    // When
    String result = organizationClient.getOrganizationApiKey(organizationId, OrganizationApiKeyType.SEND, subUnitCode, accessToken);

    // Then
    assertNull(result);
  }

  @Test
  void whenFindOrganizationStationThenInvokeWithAccessToken() {
    // Given
    String accessToken = "ACCESSTOKEN";
    Long organizationId = 1L;
    String stationId = "STATIONID";
    OrganizationStationDTO expectedResult = new OrganizationStationDTO();

    when(organizationApisHolder.getOrganizationApi(accessToken))
      .thenReturn(organizationApiMock);
    when(organizationApiMock.getOrganizationStation(organizationId, stationId))
      .thenReturn(expectedResult);

    // When
    OrganizationStationDTO result = organizationClient.findOrganizationStation(organizationId, stationId, accessToken);

    // Then
    Assertions.assertSame(expectedResult, result);
  }

  @Test
  void givenNotExistentStationIdWhenFindOrganizationStationThenNull() {
    // Given
    String accessToken = "ACCESSTOKEN";
    Long organizationId = 1L;
    String stationId = "STATIONID";

    when(organizationApisHolder.getOrganizationApi(accessToken))
      .thenReturn(organizationApiMock);
    when(organizationApiMock.getOrganizationStation(organizationId, stationId))
      .thenThrow(new RestInvokeNotFoundException("APPNAME", HttpStatus.NOT_FOUND, "ERROR", "ERRORCODE", "ERRORMESSAGE"));

    // When
    OrganizationStationDTO result = organizationClient.findOrganizationStation(organizationId, stationId, accessToken);

    // Then
    Assertions.assertNull(result);
  }
}
