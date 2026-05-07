package it.gov.pagopa.pu.pagopapayments.connector.organization.client;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import it.gov.pagopa.pu.organization.controller.generated.OrganizationApi;
import it.gov.pagopa.pu.organization.controller.generated.OrganizationEntityControllerApi;
import it.gov.pagopa.pu.organization.controller.generated.OrganizationSearchControllerApi;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.organization.dto.generated.OrganizationApiKeyType;
import it.gov.pagopa.pu.organization.dto.generated.OrganizationStationDTO;
import it.gov.pagopa.pu.pagopapayments.connector.organization.config.OrganizationApisHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

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
  void setUp() {
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

    Mockito.when(organizationApisHolder.getOrganizationEntityControllerApi(accessToken))
      .thenReturn(organizationEntityControllerApiMock);
    Mockito.when(organizationEntityControllerApiMock.crudGetOrganization(orgID+""))
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

    Mockito.when(organizationApisHolder.getOrganizationEntityControllerApi(accessToken))
      .thenReturn(organizationEntityControllerApiMock);
    Mockito.when(organizationEntityControllerApiMock.crudGetOrganization(orgID+""))
      .thenThrow(HttpClientErrorException.create(HttpStatus.NOT_FOUND, "NotFound", null, null, null));

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

    Mockito.when(organizationApisHolder.getOrganizationSearchControllerApi(accessToken))
      .thenReturn(organizationSearchControllerApiMock);
    Mockito.when(organizationSearchControllerApiMock.crudOrganizationsFindByOrgFiscalCode(orgFiscalCode))
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

    Mockito.when(organizationApisHolder.getOrganizationSearchControllerApi(accessToken))
      .thenReturn(organizationSearchControllerApiMock);
    Mockito.when(organizationSearchControllerApiMock.crudOrganizationsFindByOrgFiscalCode(orgFiscalCode))
      .thenThrow(HttpClientErrorException.create(HttpStatus.NOT_FOUND, "NotFound", null, null, null));

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

    Mockito.when(organizationApisHolder.getOrganizationApi(accessToken))
      .thenReturn(organizationApiMock);
    Mockito.when(organizationApiMock.getOrganizationApiKey(organizationId, OrganizationApiKeyType.SEND))
      .thenReturn(apiKey);

    // When
    String result = organizationClient.getOrganizationApiKey(organizationId, OrganizationApiKeyType.SEND, accessToken);

    // Then
    assertSame(apiKey, result);
  }

  @Test
  void givenNotExistentOrganizationIdWhenGetOrganizationApiKeyThenReturnNull() {
    // Given
    Long organizationId = 1L;
    String accessToken = "ACCESSTOKEN";

    Mockito.when(organizationApisHolder.getOrganizationApi(accessToken))
      .thenReturn(organizationApiMock);
    Mockito.when(organizationApiMock.getOrganizationApiKey(organizationId, OrganizationApiKeyType.SEND))
      .thenThrow(HttpClientErrorException.create(HttpStatus.NOT_FOUND, "NotFound", null, null, null));

    // When
    String result = organizationClient.getOrganizationApiKey(organizationId, OrganizationApiKeyType.SEND, accessToken);

    // Then
    assertNull(result);
  }

  @Test
  void givenValidRequestWhenGetOrganizationStationDTOThenVerifyResponse() {
    // Given
    Long organizationId = 1L;
    String stationId = "stationId";
    String accessToken = "ACCESSTOKEN";
    OrganizationStationDTO expectedOrganizationStationDTO = new OrganizationStationDTO();

    Mockito.when(organizationApisHolder.getOrganizationApi(accessToken))
      .thenReturn(organizationApiMock);
    Mockito.when(organizationApiMock.getOrganizationStation(organizationId, stationId))
      .thenReturn(expectedOrganizationStationDTO);

    // When
    OrganizationStationDTO result = organizationClient.getOrganizationStationDTO(organizationId, stationId, accessToken);

    // Then
    assertSame(expectedOrganizationStationDTO, result);
  }

  @Test
  void givenNotExistentOrganizationStationWhenGetOrganizationStationDTOThenReturnNull() {
    // Given
    Long organizationId = 1L;
    String stationId = "stationId";
    String accessToken = "ACCESSTOKEN";

    Mockito.when(organizationApisHolder.getOrganizationApi(accessToken))
      .thenReturn(organizationApiMock);
    Mockito.when(organizationApiMock.getOrganizationStation(organizationId, stationId))
      .thenThrow(HttpClientErrorException.create(HttpStatus.NOT_FOUND, "NotFound", null, null, null));

    // When
    OrganizationStationDTO result = organizationClient.getOrganizationStationDTO(organizationId, stationId, accessToken);

    // Then
    assertNull(result);
  }

}
