package it.gov.pagopa.pu.pagopapayments.connector.debtpositions.client;

import it.gov.pagopa.pu.debtpositions.controller.generated.DebtPositionApi;
import it.gov.pagopa.pu.debtpositions.controller.generated.DebtPositionTypeOrgEntityControllerApi;
import it.gov.pagopa.pu.debtpositions.controller.generated.DebtPositionTypeOrgSearchControllerApi;
import it.gov.pagopa.pu.debtpositions.controller.generated.InstallmentApi;
import it.gov.pagopa.pu.debtpositions.dto.generated.ActualizeAmountRequestDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionOrigin;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.pagopapayments.connector.debtpositions.config.DebtPositionsApisHolder;
import it.gov.pagopa.pu.pagopapayments.exception.PagoPaNodeFaultException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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
  private DebtPositionTypeOrgSearchControllerApi debtPositionTypeOrgSearchControllerApiMock;
  @Mock
  private DebtPositionApi debtPositionApiMock;
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
      debtPositionTypeOrgSearchControllerApiMock,
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
    List<InstallmentDTO> result = client.getInstallmentsByOrganizationIdAndNav(organizationId, nav, debtPositionOriginList, accessToken);

    // Then
    Assertions.assertSame(expectedResult, result);
  }

  @Test
  void whenUpdateInstallmentNotificationFeeThenInvokeApi(){
    //Given
    String accessToken = "ACCESSTOKEN";
    ActualizeAmountRequestDTO request = ActualizeAmountRequestDTO.builder()
      .organizationId(1L)
      .nav("NAV")
      .newFeeCents(100L)
      .actualizedFromPuSil(false)
      .build();

    InstallmentDTO expectedResult = new InstallmentDTO();

    Mockito.when(apisHolderMock.getDebtPositionApi(accessToken))
      .thenReturn(debtPositionApiMock);
    Mockito.when(debtPositionApiMock.updateInstallmentNotificationFee(request))
      .thenReturn(expectedResult);

    // When
    InstallmentDTO result = client.updateInstallmentNotificationFee(request, accessToken);

    // Then
    Assertions.assertSame(expectedResult, result);
  }

  private Exception createException(String type) {
    return switch (type) {
      case "NotFoundException" ->
        HttpClientErrorException.create(HttpStatus.NOT_FOUND, "NotFound", null, null, null);
      case "ConflictException" ->
        HttpClientErrorException.create(HttpStatus.CONFLICT, "Conflict", null, null, null);
      case "PreconditionFailedException" ->
        HttpClientErrorException.create(HttpStatus.PRECONDITION_FAILED, "PreconditionFailed", null, null, null);
      case "InternalServerErrorException" ->
        HttpClientErrorException.create(HttpStatus.INTERNAL_SERVER_ERROR, "InternalServerError", null, null, null);
      default ->
        throw new IllegalArgumentException("Unknown exception type: " + type);
    };
  }

  @ParameterizedTest
  @CsvSource({
    "NotFoundException, 'PAA_PAGAMENTO_SCONOSCIUTO'",
    "ConflictException, 'PAA_PAGAMENTO_DUPLICATO'",
    "PreconditionFailedException, 'PAA_PAGAMENTO_SCADUTO'",
    "InternalServerErrorException, 'PAA_SYSTEM_ERROR'"
  })
  void whenUpdateInstallmentNotificationFeeWithErrorThenException(String exceptionType, String errorMessage){
    //Given
    String accessToken = "ACCESSTOKEN";
    ActualizeAmountRequestDTO request = ActualizeAmountRequestDTO.builder()
      .organizationId(1L)
      .nav("NAV")
      .newFeeCents(100L)
      .actualizedFromPuSil(false)
      .build();

    Mockito.when(apisHolderMock.getDebtPositionApi(accessToken))
      .thenReturn(debtPositionApiMock);
    Exception mockedException = createException(exceptionType);
    Mockito.when(debtPositionApiMock.updateInstallmentNotificationFee(request))
      .thenThrow(mockedException);

    // When
    PagoPaNodeFaultException exception = Assertions.assertThrows(PagoPaNodeFaultException.class,
      () -> client.updateInstallmentNotificationFee(request, accessToken));

    // Then
    Assertions.assertEquals(errorMessage, exception.getErrorCode().code());
  }

  @ParameterizedTest
  @ValueSource(strings = "ORDINARY")
  @NullAndEmptySource
  void whenCrudDebtPositionTypeOrgsFindDebtPositionTypeOrgByOrgIdAndNavAndOriginsThenInvokeApi(String debtPositionOrigin){
    //Given
    String accessToken = "ACCESSTOKEN";
    Long organizationId = 1L;
    String nav = "NAV";
    List<DebtPositionOrigin> debtPositionOriginList = debtPositionOrigin==null ? null :
      (debtPositionOrigin.isEmpty() ? List.of() : List.of(DebtPositionOrigin.valueOf(debtPositionOrigin)));
    DebtPositionTypeOrg expectedResult = new DebtPositionTypeOrg();

    Mockito.when(apisHolderMock.getDebtPositionTypeOrgSearchControllerApi(accessToken))
      .thenReturn(debtPositionTypeOrgSearchControllerApiMock);
    Mockito.when(debtPositionTypeOrgSearchControllerApiMock.crudDebtPositionTypeOrgsFindDebtPositionTypeOrgByOrgIdAndNavAndOrigins(organizationId, nav, debtPositionOriginList))
      .thenReturn(expectedResult);

    // When
    DebtPositionTypeOrg result = client.findDebtPositionTypeOrgByOrgIdAndNavAndOrigins(organizationId, nav, debtPositionOriginList, accessToken);

    // Then
    Assertions.assertSame(expectedResult, result);
  }

  @ParameterizedTest
  @ValueSource(strings = "ORDINARY")
  @NullAndEmptySource
  void givenNotExistentDebtPositionTypeOrgCrudDebtPositionTypeOrgsFindDebtPositionTypeOrgByOrgIdAndNavAndOriginsThenNull(String debtPositionOrigin){
    //Given
    String accessToken = "ACCESSTOKEN";
    Long organizationId = 1L;
    String nav = "NAV";
    List<DebtPositionOrigin> debtPositionOriginList = debtPositionOrigin==null ? null :
      (debtPositionOrigin.isEmpty() ? List.of() : List.of(DebtPositionOrigin.valueOf(debtPositionOrigin)));

    Mockito.when(apisHolderMock.getDebtPositionTypeOrgSearchControllerApi(accessToken))
      .thenReturn(debtPositionTypeOrgSearchControllerApiMock);

    Mockito.when(debtPositionTypeOrgSearchControllerApiMock
        .crudDebtPositionTypeOrgsFindDebtPositionTypeOrgByOrgIdAndNavAndOrigins(organizationId, nav, debtPositionOriginList))
      .thenThrow(HttpClientErrorException.create(HttpStatus.NOT_FOUND, "NotFound", null, null, null));

    // When
    DebtPositionTypeOrg result = client.findDebtPositionTypeOrgByOrgIdAndNavAndOrigins(organizationId, nav, debtPositionOriginList, accessToken);

    // Then
    Assertions.assertNull(result);
  }

}
