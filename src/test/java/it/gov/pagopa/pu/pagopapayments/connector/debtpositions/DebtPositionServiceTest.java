package it.gov.pagopa.pu.pagopapayments.connector.debtpositions;

import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import it.gov.pagopa.pu.pagopapayments.connector.debtpositions.client.DebtPositionClient;
import org.apache.commons.lang3.tuple.Pair;
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
import org.springframework.http.ResponseEntity;

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
    List<DebtPositionOrigin> debtPositionOriginList = debtPositionOrigin==null ? null :
      (debtPositionOrigin.isEmpty() ? List.of() : List.of(DebtPositionOrigin.valueOf(debtPositionOrigin)));

    Mockito.when(clientMock.getInstallmentsByOrganizationIdAndNav(Mockito.same(organizationId), Mockito.same(nav), Mockito.same(debtPositionOriginList), Mockito.same(accessToken)))
      .thenReturn(expectedResult);

    // When
    List<InstallmentDTO> result = service.getInstallmentsByOrganizationIdAndNav(organizationId, nav, debtPositionOriginList, accessToken);

    // Then
    Assertions.assertSame(expectedResult, result);
  }

  @Test
  void whenUpdateInstallmentNotificationFeeThenInvokeClient(){
    // Given
    String accessToken = "ACCESSTOKEN";

    ActualizeAmountRequestDTO request = ActualizeAmountRequestDTO.builder()
      .organizationId(1L)
      .nav("NAV")
      .newFeeCents(100L)
      .actualizedFromPuSil(false)
      .build();

    InstallmentDTO expectedResult = new InstallmentDTO();

    Mockito.when(clientMock.updateInstallmentNotificationFee(Mockito.same(request),
      Mockito.same(accessToken))).thenReturn(expectedResult);

    // When
    InstallmentDTO result = service.updateInstallmentNotificationFee(request, accessToken);

    // Then
    Assertions.assertSame(expectedResult, result);
  }

  @ParameterizedTest
  @ValueSource(strings = "ORDINARY")
  @NullAndEmptySource
  void whenFindDebtPositionTypeOrgByOrgIdAndNavAndOriginsThenInvokeClient(String debtPositionOrigin){
    // Given
    String accessToken = "ACCESSTOKEN";
    Long organizationId = 1L;
    String nav = "NAV";
    DebtPositionTypeOrg expectedResult = new DebtPositionTypeOrg();
    List<DebtPositionOrigin> debtPositionOriginList = debtPositionOrigin==null ? null :
      (debtPositionOrigin.isEmpty() ? List.of() : List.of(DebtPositionOrigin.valueOf(debtPositionOrigin)));

    Mockito.when(clientMock.findDebtPositionTypeOrgByOrgIdAndNavAndOrigins(Mockito.same(organizationId),Mockito.same(nav),
      Mockito.same(debtPositionOriginList), Mockito.same(accessToken))).thenReturn(expectedResult);

    // When
    DebtPositionTypeOrg result = service.findDebtPositionTypeOrgByOrgIdAndNavAndOrigins(organizationId, nav, debtPositionOriginList, accessToken);

    // Then
    Assertions.assertSame(expectedResult, result);
  }

  @Test
  void whenCreateDebtPositionThenReturnDebtPositionDTO() {
    // Given
    DebtPositionDTO debtPositionDTO = new DebtPositionDTO();
    String accessToken = "ACCESSTOKEN";
    ResponseEntity<DebtPositionDTO> expectedResult = ResponseEntity.ok().header("X-Workflow-Id", "workflow-id").body(debtPositionDTO);

    Mockito.when(clientMock.createDebtPosition(debtPositionDTO, accessToken)).thenReturn(expectedResult);

    // When
    Pair<DebtPositionDTO, String> result = service.createDebtPosition(debtPositionDTO, accessToken);

    // Then
    Assertions.assertNotNull(result);
    Assertions.assertSame(expectedResult.getBody(), result.getLeft());
    Assertions.assertSame("workflow-id", result.getRight());
  }

}
