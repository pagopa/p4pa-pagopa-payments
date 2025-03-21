package it.gov.pagopa.pu.pagopapayments.connector.debtpositions.client;

import it.gov.pagopa.pu.debtpositions.controller.generated.DebtPositionTypeOrgEntityControllerApi;
import it.gov.pagopa.pu.debtpositions.controller.generated.InstallmentApi;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionOrigin;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.pagopapayments.connector.debtpositions.config.DebtPositionsApisHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class DebtPositionClientTest {

  @Mock
  private DebtPositionsApisHolder apisHolderMock;
  @Mock
  private DebtPositionTypeOrgEntityControllerApi debtPositionTypeOrgEntityControllerApiMock;
  @Mock
  private InstallmentApi installmentApiMock;


  private DebtPositionClient client;

  @BeforeEach
  void setUp() {
    client = new DebtPositionClient(apisHolderMock);
  }

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(
      apisHolderMock,
      debtPositionTypeOrgEntityControllerApiMock,
      installmentApiMock
      );
  }

  @Test
  void whenGetDebtPositionTypeOrgByIdThenInvokeApi(){
    //Given
    String accessToken = "ACCESSTOKEN";
    long debtPositionTypeOrgId = 1L;
    DebtPositionTypeOrg expectedResult = new DebtPositionTypeOrg();

    Mockito.when(apisHolderMock.getDebtPositionTypeOrgEntityControllerApi(accessToken))
      .thenReturn(debtPositionTypeOrgEntityControllerApiMock);
    Mockito.when(debtPositionTypeOrgEntityControllerApiMock.crudGetDebtpositiontypeorg(debtPositionTypeOrgId+""))
      .thenReturn(expectedResult);

    // When
    DebtPositionTypeOrg result = client.getDebtPositionTypeOrgById(debtPositionTypeOrgId, accessToken);

    // Then
    Assertions.assertSame(expectedResult, result);
  }

  @Test
  void givenNotExistentDebtPositionTypeOrgWhenGetDebtPositionTypeOrgByIdThenNull(){
    //Given
    String accessToken = "ACCESSTOKEN";
    long debtPositionTypeOrgId = 1L;

    Mockito.when(apisHolderMock.getDebtPositionTypeOrgEntityControllerApi(accessToken))
      .thenReturn(debtPositionTypeOrgEntityControllerApiMock);
    Mockito.when(debtPositionTypeOrgEntityControllerApiMock.crudGetDebtpositiontypeorg(debtPositionTypeOrgId+""))
      .thenThrow(HttpClientErrorException.create(HttpStatus.NOT_FOUND, "NotFound", null, null, null));

    // When
    DebtPositionTypeOrg result = client.getDebtPositionTypeOrgById(debtPositionTypeOrgId, accessToken);

    // Then
    Assertions.assertNull(result);
  }

  @ParameterizedTest
  @ValueSource(strings = "ORDINARY")
  @NullAndEmptySource
  void whenGetDebtPositionsByOrganizationIdAndNavThenInvokeApi(String debtPositionOrigin){
    //Given
    String accessToken = "ACCESSTOKEN";
    long organizationId = 1L;
    String nav = "NAV";
    List<InstallmentDTO> expectedResult = List.of();
    List<DebtPositionOrigin> debtPositionOriginList = debtPositionOrigin==null ? null :
      (debtPositionOrigin.isEmpty() ? List.of() : List.of(DebtPositionOrigin.valueOf(debtPositionOrigin)));

    Mockito.when(apisHolderMock.getInstallmentApi(accessToken))
      .thenReturn(installmentApiMock);
    Mockito.when(installmentApiMock.getInstallmentsByOrganizationIdAndNav(organizationId, nav, debtPositionOriginList))
      .thenReturn(expectedResult);


    // When
    List<InstallmentDTO> result = client.getDebtPositionsByOrganizationIdAndNav(organizationId, nav, debtPositionOriginList, accessToken);

    // Then
    Assertions.assertSame(expectedResult, result);
  }

}
