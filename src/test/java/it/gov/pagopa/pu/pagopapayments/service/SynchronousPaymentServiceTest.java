package it.gov.pagopa.pu.pagopapayments.service;

import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.pagopapayments.connector.DebtPositionClient;
import it.gov.pagopa.pu.pagopapayments.connector.auth.AuthnService;
import it.gov.pagopa.pu.pagopapayments.dto.RetrievePaymentDTO;
import it.gov.pagopa.pu.pagopapayments.enums.PagoPaNodeFaults;
import it.gov.pagopa.pu.pagopapayments.exception.PagoPaNodeFaultException;
import it.gov.pagopa.pu.pagopapayments.service.synchronouspayments.SynchronousPaymentService;
import it.gov.pagopa.pu.pagopapayments.service.synchronouspayments.SynchronousPaymentStatusVerifierService;
import it.gov.pagopa.pu.pagopapayments.util.TestUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.internal.matchers.apachecommons.ReflectionEquals;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.jemos.podam.api.PodamFactory;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class SynchronousPaymentServiceTest {

  @Mock
  private DebtPositionClient debtPositionClientMock;
  @Mock
  private AuthnService authnServiceMock;
  @Mock
  private PaForNodeRequestValidatorService paForNodeRequestValidatorServiceMock;
  @Mock
  private SynchronousPaymentStatusVerifierService synchronousPaymentStatusVerifierServiceMock;

  @InjectMocks
  private SynchronousPaymentService synchronousPaymentService;

  private final PodamFactory podamFactory;

  SynchronousPaymentServiceTest() {
    podamFactory = TestUtils.getPodamFactory();
  }

  private static final String VALID_ACCEESS_TOKEN = "VALID_ACCESS_TOKEN";


  @BeforeEach
  void setup() {
    Mockito.when(authnServiceMock.getAccessToken()).thenReturn(VALID_ACCEESS_TOKEN);
  }

  //region retrievePayment

  @Test
  void givenValidInstallmentWhenRetrievePaymentThenOk() {
    // Given
    Organization organization = podamFactory.manufacturePojo(Organization.class);
    InstallmentDTO installmentDTO = podamFactory.manufacturePojo(InstallmentDTO.class);
    List<InstallmentDTO> installmentDTOList = List.of(installmentDTO);
    RetrievePaymentDTO retrievePaymentDTO = podamFactory.manufacturePojo(RetrievePaymentDTO.class);
    retrievePaymentDTO.setIdPA(retrievePaymentDTO.getFiscalCode());

    Mockito.when(paForNodeRequestValidatorServiceMock.paForNodeRequestValidate(retrievePaymentDTO, VALID_ACCEESS_TOKEN)).thenReturn(organization);
    Mockito.when(debtPositionClientMock.getDebtPositionsByOrganizationIdAndNav(organization.getOrganizationId(), retrievePaymentDTO.getNoticeNumber(), VALID_ACCEESS_TOKEN))
      .thenReturn(installmentDTOList);
    Mockito.when(synchronousPaymentStatusVerifierServiceMock.verifyPaymentStatus(organization, installmentDTOList, retrievePaymentDTO.getNoticeNumber(), retrievePaymentDTO.getPostalTransfer())).thenReturn(installmentDTO);

    Pair<InstallmentDTO, Organization> expectedResponse = Pair.of(installmentDTO, organization);

    // When
    Pair<InstallmentDTO, Organization> response = synchronousPaymentService.retrievePayment(retrievePaymentDTO);

    // Then
    Assertions.assertTrue(new ReflectionEquals(expectedResponse).matches(response));
    Mockito.verify(authnServiceMock, Mockito.times(1)).getAccessToken();
    Mockito.verify(paForNodeRequestValidatorServiceMock, Mockito.times(1)).paForNodeRequestValidate(retrievePaymentDTO, VALID_ACCEESS_TOKEN);
    Mockito.verify(debtPositionClientMock, Mockito.times(1)).getDebtPositionsByOrganizationIdAndNav(organization.getOrganizationId(), retrievePaymentDTO.getNoticeNumber(), VALID_ACCEESS_TOKEN);
    Mockito.verify(synchronousPaymentStatusVerifierServiceMock, Mockito.times(1))
      .verifyPaymentStatus(organization, installmentDTOList, retrievePaymentDTO.getNoticeNumber(), retrievePaymentDTO.getPostalTransfer());
  }

  @Test
  void givenInvalidInstallmentWhenRetrievePaymentThenFault() {
    // Given
    RetrievePaymentDTO retrievePaymentDTO = podamFactory.manufacturePojo(RetrievePaymentDTO.class);
    retrievePaymentDTO.setIdPA(retrievePaymentDTO.getFiscalCode()+"XXX");

    // When
    PagoPaNodeFaultException exception = Assertions.assertThrows(PagoPaNodeFaultException.class,()->synchronousPaymentService.retrievePayment(retrievePaymentDTO));

    // Then
    Assertions.assertEquals(PagoPaNodeFaults.PAA_ID_DOMINIO_ERRATO, exception.getErrorCode());
    Assertions.assertEquals(retrievePaymentDTO.getFiscalCode(), exception.getErrorEmitter());
    Mockito.verify(authnServiceMock, Mockito.times(1)).getAccessToken();
    Mockito.verifyNoInteractions(paForNodeRequestValidatorServiceMock, debtPositionClientMock, synchronousPaymentStatusVerifierServiceMock);
  }

  //endregion

}
