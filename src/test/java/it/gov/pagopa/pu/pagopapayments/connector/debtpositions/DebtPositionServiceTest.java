package it.gov.pagopa.pu.pagopapayments.connector.debtpositions;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.pagopapayments.connector.debtpositions.client.DebtPositionClient;
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

import java.util.List;

@ExtendWith(MockitoExtension.class)
class DebtPositionServiceTest {

  @Mock
  private DebtPositionClient clientMock;

  private DebtPositionService service;

  @BeforeEach
  void init(){
    service = new DebtPositionServiceImpl(clientMock);
  }

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(clientMock);
  }

  @Test
  void whenGetDebtPositionTypeOrgByIdThenInvokeClient(){
    // Given
    Long debtPositionTypeOrgId = 1L;
    String accessToken = "ACCESSTOKEN";
    DebtPositionTypeOrg expectedResult = new DebtPositionTypeOrg();

    Mockito.when(clientMock.getDebtPositionTypeOrgById(Mockito.same(debtPositionTypeOrgId), Mockito.same(accessToken)))
      .thenReturn(expectedResult);

    // When
    DebtPositionTypeOrg result = service.getDebtPositionTypeOrgById(debtPositionTypeOrgId, accessToken);

    // Then
    Assertions.assertSame(expectedResult, result);
  }

  @ParameterizedTest
  @ValueSource(strings = "ORDINARY")
  @NullAndEmptySource
  void whenGetDebtPositionsByOrganizationIdAndNavThenInvokeClient(String debtPositionOrigin){
    // Given
    Long organizationId = 1L;
    String nav = "NAV";
    String accessToken = "ACCESSTOKEN";
    List<InstallmentDTO> expectedResult = List.of();
    List<DebtPositionDTO.DebtPositionOriginEnum> debtPositionOriginList = debtPositionOrigin==null ? null :
      (debtPositionOrigin.isEmpty() ? List.of() : List.of(DebtPositionDTO.DebtPositionOriginEnum.valueOf(debtPositionOrigin)));

    Mockito.when(clientMock.getDebtPositionsByOrganizationIdAndNav(Mockito.same(organizationId), Mockito.same(nav), Mockito.same(debtPositionOriginList), Mockito.same(accessToken)))
      .thenReturn(expectedResult);

    // When
    List<InstallmentDTO> result = service.getDebtPositionsByOrganizationIdAndNav(organizationId, nav, debtPositionOriginList, accessToken);

    // Then
    Assertions.assertSame(expectedResult, result);
  }

}
