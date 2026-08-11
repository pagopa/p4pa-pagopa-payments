package it.gov.pagopa.pu.pagopapayments.connector.debtpositions.client;

import it.gov.pagopa.pu.debtpositions.client.generated.DebtPositionApi;
import it.gov.pagopa.pu.debtpositions.client.generated.DebtPositionTypeOrgEntityControllerApi;
import it.gov.pagopa.pu.debtpositions.client.generated.DebtPositionTypeOrgSearchControllerApi;
import it.gov.pagopa.pu.debtpositions.client.generated.InstallmentApi;
import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import it.gov.pagopa.pu.pagopapayments.connector.debtpositions.config.DebtPositionsApisHolder;
import it.gov.pagopa.pu.pagopapayments.exception.PagoPaNodeFaultException;
import it.gov.pagopa.pu.pagopapayments.exception.common.RestInvokeConflictException;
import it.gov.pagopa.pu.pagopapayments.exception.common.RestInvokeInvalidValueException;
import it.gov.pagopa.pu.pagopapayments.exception.common.RestInvokeNotFoundException;
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
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

import static it.gov.pagopa.pu.pagopapayments.util.DebtPositionUtils.ORDINARY_DEBT_POSITION_ORIGINS;
import static org.mockito.Mockito.when;

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

    when(apisHolderMock.getDebtPositionTypeOrgEntityControllerApi(accessToken))
      .thenReturn(debtPositionTypeOrgEntityControllerApiMock);
    when(debtPositionTypeOrgEntityControllerApiMock.crudGetDebtpositiontypeorg(debtPositionTypeOrgId+""))
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

    when(apisHolderMock.getDebtPositionTypeOrgEntityControllerApi(accessToken))
      .thenReturn(debtPositionTypeOrgEntityControllerApiMock);
    when(debtPositionTypeOrgEntityControllerApiMock.crudGetDebtpositiontypeorg(debtPositionTypeOrgId+""))
      .thenThrow(new RestInvokeNotFoundException("APPNAME", HttpStatus.NOT_FOUND, "ERROR", "ERRORCODE", "ERRORMESSAGE"));

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

    when(apisHolderMock.getInstallmentApi(accessToken))
      .thenReturn(installmentApiMock);
    when(installmentApiMock.getInstallmentsByOrganizationIdAndNav(organizationId, nav, debtPositionOriginList))
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

    when(apisHolderMock.getDebtPositionApi(accessToken))
      .thenReturn(debtPositionApiMock);
    when(debtPositionApiMock.updateInstallmentNotificationFee(request))
      .thenReturn(expectedResult);

    // When
    InstallmentDTO result = client.updateInstallmentNotificationFee(request, accessToken);

    // Then
    Assertions.assertSame(expectedResult, result);
  }

  private Exception createException(String type) {
    return switch (type) {
      case "NotFoundException" ->
        new RestInvokeNotFoundException("APPNAME", HttpStatus.NOT_FOUND, "ERROR", "ERRORCODE", "ERRORMESSAGE");
      case "ConflictException" ->
        new RestInvokeConflictException("APPNAME", HttpStatus.CONFLICT, "ERROR", "ERRORCODE", "ERRORMESSAGE", null);
      case "PreconditionFailedException" ->
        new RestInvokeInvalidValueException("APPNAME", HttpStatus.PRECONDITION_FAILED, "ERROR", "ERRORCODE", "ERRORMESSAGE", null);
      case "HttpClientErrorExceptionNotHandled" ->
        new RestInvokeInvalidValueException("APPNAME", HttpStatus.BAD_REQUEST, "ERROR", "ERRORCODE", "ERRORMESSAGE", null);
      default ->
        throw new IllegalArgumentException("Unknown exception type: " + type);
    };
  }

  @ParameterizedTest
  @CsvSource({
    "NotFoundException, 'PAA_PAGAMENTO_SCONOSCIUTO'",
    "ConflictException, 'PAA_PAGAMENTO_DUPLICATO'",
    "PreconditionFailedException, 'PAA_PAGAMENTO_SCADUTO'",
    "HttpClientErrorExceptionNotHandled, 'PAA_SYSTEM_ERROR'"
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

    when(apisHolderMock.getDebtPositionApi(accessToken))
      .thenReturn(debtPositionApiMock);
    Exception mockedException = createException(exceptionType);
    when(debtPositionApiMock.updateInstallmentNotificationFee(request))
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

    when(apisHolderMock.getDebtPositionTypeOrgSearchControllerApi(accessToken))
      .thenReturn(debtPositionTypeOrgSearchControllerApiMock);
    when(debtPositionTypeOrgSearchControllerApiMock.crudDebtPositionTypeOrgsFindDebtPositionTypeOrgByOrgIdAndNavAndOrigins(organizationId, nav, debtPositionOriginList))
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

    when(apisHolderMock.getDebtPositionTypeOrgSearchControllerApi(accessToken))
      .thenReturn(debtPositionTypeOrgSearchControllerApiMock);

    when(debtPositionTypeOrgSearchControllerApiMock
        .crudDebtPositionTypeOrgsFindDebtPositionTypeOrgByOrgIdAndNavAndOrigins(organizationId, nav, debtPositionOriginList))
      .thenThrow(new RestInvokeNotFoundException("APPNAME", HttpStatus.NOT_FOUND, "ERROR", "ERRORCODE", "ERRORMESSAGE"));

    // When
    DebtPositionTypeOrg result = client.findDebtPositionTypeOrgByOrgIdAndNavAndOrigins(organizationId, nav, debtPositionOriginList, accessToken);

    // Then
    Assertions.assertNull(result);
  }

  @Test
  void whenCreateDebtPositionThenInvokeApi() {
    // Given
    String accessToken = "ACCESSTOKEN";
    DebtPositionDTO debtPositionDTO = new DebtPositionDTO();
    ResponseEntity<DebtPositionDTO> expectedResponse = ResponseEntity.ok(debtPositionDTO);

    when(apisHolderMock.getDebtPositionApi(accessToken))
      .thenReturn(debtPositionApiMock);
    when(debtPositionApiMock.createDebtPositionWithHttpInfo(debtPositionDTO, false))
      .thenReturn(expectedResponse);

    // When
    ResponseEntity<DebtPositionDTO> result = client.createDebtPosition(debtPositionDTO, accessToken);

    // Then
    Assertions.assertEquals(expectedResponse, result);
  }

  @Test
  void whenFindDebtPositionTypeOrgByOrgIdAndCodeThenInvokeApi() {
    //Given
    String accessToken = "ACCESSTOKEN";
    Long organizationId = 1L;
    String code = "CODE";
    DebtPositionTypeOrg expectedResult = new DebtPositionTypeOrg();
    expectedResult.setCode(code);

    when(apisHolderMock.getDebtPositionTypeOrgSearchControllerApi(accessToken))
      .thenReturn(debtPositionTypeOrgSearchControllerApiMock);
    when(debtPositionTypeOrgSearchControllerApiMock.crudDebtPositionTypeOrgsFindByOrganizationIdAndCode(organizationId, code))
      .thenReturn(expectedResult);

    // When
    DebtPositionTypeOrg result = client.findDebtPositionTypeOrgByOrgIdAndCode(organizationId, code, accessToken);

    // Then
    Assertions.assertSame(expectedResult, result);
  }

  @Test
  void givenNotExistentDebtPositionTypeOrgWhenFindDebtPositionTypeOrgByOrgIdAndCodeThenNull() {
    //Given
    String accessToken = "ACCESSTOKEN";
    Long organizationId = 1L;
    String code = "CODE";

    when(apisHolderMock.getDebtPositionTypeOrgSearchControllerApi(accessToken))
      .thenReturn(debtPositionTypeOrgSearchControllerApiMock);

    when(debtPositionTypeOrgSearchControllerApiMock
      .crudDebtPositionTypeOrgsFindByOrganizationIdAndCode(organizationId, code))
      .thenThrow(new RestInvokeNotFoundException("APPNAME", HttpStatus.NOT_FOUND, "ERROR", "ERRORCODE", "ERRORMESSAGE"));

    // When
    DebtPositionTypeOrg result = client.findDebtPositionTypeOrgByOrgIdAndCode(organizationId, code, accessToken);

    // Then
    Assertions.assertNull(result);
  }

  @Test
  void whenGetDebtPositionsByOrganizationIdAndNavThenInvokeApi() {
    //Given
    String accessToken = "ACCESSTOKEN";
    Long organizationId = 1L;
    String nav = "301000000026066731";
    List<DebtPositionDTO> expectedResult = new ArrayList<>();

    when(apisHolderMock.getDebtPositionApi(accessToken))
      .thenReturn(debtPositionApiMock);
    when(debtPositionApiMock.getDebtPositionsByOrganizationIdAndNav(organizationId, nav, ORDINARY_DEBT_POSITION_ORIGINS))
      .thenReturn(expectedResult);

    // When
    List<DebtPositionDTO> result = client.getDebtPositionsByOrganizationIdAndNav(organizationId, nav, ORDINARY_DEBT_POSITION_ORIGINS, accessToken);

    // Then
    Assertions.assertSame(expectedResult, result);
  }
}
