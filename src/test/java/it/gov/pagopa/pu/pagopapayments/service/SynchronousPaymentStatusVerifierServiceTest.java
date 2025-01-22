package it.gov.pagopa.pu.pagopapayments.service;

import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.TransferDTO;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.pagopapayments.enums.PagoPaNodeFaults;
import it.gov.pagopa.pu.pagopapayments.exception.PagoPaNodeFaultException;
import it.gov.pagopa.pu.pagopapayments.service.synchronouspayments.SynchronousPaymentStatusVerifierService;
import it.gov.pagopa.pu.pagopapayments.util.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.internal.matchers.apachecommons.ReflectionEquals;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.jemos.podam.api.PodamFactory;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class SynchronousPaymentStatusVerifierServiceTest {

  @InjectMocks
  private SynchronousPaymentStatusVerifierService synchronousPaymentStatusVerifierService;

  private final PodamFactory podamFactory;

  SynchronousPaymentStatusVerifierServiceTest() {
    podamFactory = TestUtils.getPodamFactory();
  }

  //region verifyPaymentStatus

  @Test
  void givenValidInstallmentWhenVerifyPaymentStatusThenOk() {
    // Given
    Organization organization = podamFactory.manufacturePojo(Organization.class);
    InstallmentDTO installmentDTO = podamFactory.manufacturePojo(InstallmentDTO.class);
    installmentDTO.setStatus(InstallmentDTO.StatusEnum.UNPAID);
    InstallmentDTO otherInstallmentDTO = podamFactory.manufacturePojo(InstallmentDTO.class);
    otherInstallmentDTO.setStatus(InstallmentDTO.StatusEnum.EXPIRED);
    List<InstallmentDTO> installmentDTOList = List.of(installmentDTO, otherInstallmentDTO);
    Boolean postalAccess = null;

    // When
    InstallmentDTO response = synchronousPaymentStatusVerifierService.verifyPaymentStatus(organization, installmentDTOList, "NAV", postalAccess);

    // Then
    Assertions.assertTrue(new ReflectionEquals(installmentDTO).matches(response));
  }

  @Test
  void givenValidInstallmentPostalWhenVerifyPaymentStatusThenOk() {
    // Given
    Organization organization = podamFactory.manufacturePojo(Organization.class);
    InstallmentDTO installmentDTO = podamFactory.manufacturePojo(InstallmentDTO.class);
    installmentDTO.setStatus(InstallmentDTO.StatusEnum.UNPAID);
    installmentDTO.setTransfers(List.of(
      new TransferDTO().orgFiscalCode(organization.getOrgFiscalCode()).postalIban("IBAN"),
      new TransferDTO().orgFiscalCode(organization.getOrgFiscalCode()+"xxxx").iban("IBAN")
    ));
    InstallmentDTO otherInstallmentDTO = podamFactory.manufacturePojo(InstallmentDTO.class);
    otherInstallmentDTO.setStatus(InstallmentDTO.StatusEnum.UNPAID);
    otherInstallmentDTO.setTransfers(List.of(new TransferDTO().orgFiscalCode(organization.getOrgFiscalCode()).iban("IBAN")));
    List<InstallmentDTO> installmentDTOList = List.of(installmentDTO, otherInstallmentDTO);
    Boolean postalAccess = true;

    // When
    InstallmentDTO response = synchronousPaymentStatusVerifierService.verifyPaymentStatus(organization, installmentDTOList, "NAV", postalAccess);

    // Then
    Assertions.assertTrue(new ReflectionEquals(installmentDTO).matches(response));
  }

  @Test
  void givenValidInstallmentNonPostalWhenVerifyPaymentStatusThenOk() {
    // Given
    Organization organization = podamFactory.manufacturePojo(Organization.class);
    InstallmentDTO installmentDTO = podamFactory.manufacturePojo(InstallmentDTO.class);
    installmentDTO.setStatus(InstallmentDTO.StatusEnum.UNPAID);
    installmentDTO.setTransfers(List.of(new TransferDTO().orgFiscalCode(organization.getOrgFiscalCode()).postalIban("IBAN")));
    InstallmentDTO otherInstallmentDTO = podamFactory.manufacturePojo(InstallmentDTO.class);
    otherInstallmentDTO.setStatus(InstallmentDTO.StatusEnum.UNPAID);
    otherInstallmentDTO.setTransfers(List.of(
      new TransferDTO().orgFiscalCode(organization.getOrgFiscalCode()).iban("IBAN"),
      new TransferDTO().orgFiscalCode(organization.getOrgFiscalCode()+"xxxx").postalIban("IBAN")
    ));
    List<InstallmentDTO> installmentDTOList = List.of(installmentDTO, otherInstallmentDTO);
    Boolean postalAccess = false;

    // When
    InstallmentDTO response = synchronousPaymentStatusVerifierService.verifyPaymentStatus(organization, installmentDTOList, "NAV", postalAccess);

    // Then
    Assertions.assertTrue(new ReflectionEquals(otherInstallmentDTO).matches(response));
  }

  @Test
  void givenNotFoundInstallmentWhenVerifyPaymentStatusThenOk() {
    // Given
    Organization organization = podamFactory.manufacturePojo(Organization.class);
    List<InstallmentDTO> installmentDTOList = List.of();
    Boolean postalAccess = null;

    // When
    PagoPaNodeFaultException response = Assertions.assertThrows(PagoPaNodeFaultException.class,
      ()->synchronousPaymentStatusVerifierService.verifyPaymentStatus(organization, installmentDTOList, "NAV", postalAccess));

    // Then
    Assertions.assertEquals(PagoPaNodeFaults.PAA_PAGAMENTO_SCONOSCIUTO, response.getErrorCode());
    Assertions.assertEquals(organization.getOrgFiscalCode(), response.getErrorEmitter());
  }

  @Test
  void givenDuplicatedInstallmentWhenVerifyPaymentStatusThenOk() {
    // Given
    Organization organization = podamFactory.manufacturePojo(Organization.class);
    InstallmentDTO installmentDTO = podamFactory.manufacturePojo(InstallmentDTO.class);
    installmentDTO.setStatus(InstallmentDTO.StatusEnum.UNPAID);
    List<InstallmentDTO> installmentDTOList = List.of(installmentDTO, installmentDTO);
    Boolean postalAccess = null;

    // When
    PagoPaNodeFaultException response = Assertions.assertThrows(PagoPaNodeFaultException.class,
      ()->synchronousPaymentStatusVerifierService.verifyPaymentStatus(organization, installmentDTOList, "NAV", postalAccess));

    // Then
    Assertions.assertEquals(PagoPaNodeFaults.PAA_PAGAMENTO_DUPLICATO, response.getErrorCode());
    Assertions.assertEquals(organization.getOrgFiscalCode(), response.getErrorEmitter());
  }

  @Test
  void givenExpiredInstallmentWhenVerifyPaymentStatusThenOk() {
    // Given
    Organization organization = podamFactory.manufacturePojo(Organization.class);
    InstallmentDTO installmentDTO = podamFactory.manufacturePojo(InstallmentDTO.class);
    installmentDTO.setStatus(InstallmentDTO.StatusEnum.TO_SYNC);
    InstallmentDTO otherInstallmentDTO = podamFactory.manufacturePojo(InstallmentDTO.class);
    otherInstallmentDTO.setStatus(InstallmentDTO.StatusEnum.EXPIRED);
    List<InstallmentDTO> installmentDTOList = List.of(installmentDTO, otherInstallmentDTO);
    Boolean postalAccess = null;

    // When
    PagoPaNodeFaultException response = Assertions.assertThrows(PagoPaNodeFaultException.class,
      ()->synchronousPaymentStatusVerifierService.verifyPaymentStatus(organization, installmentDTOList, "NAV", postalAccess));

    // Then
    Assertions.assertEquals(PagoPaNodeFaults.PAA_PAGAMENTO_SCADUTO, response.getErrorCode());
    Assertions.assertEquals(organization.getOrgFiscalCode(), response.getErrorEmitter());
  }

  @Test
  void givenPaidInstallmentWhenVerifyPaymentStatusThenOk() {
    // Given
    Organization organization = podamFactory.manufacturePojo(Organization.class);
    InstallmentDTO installmentDTO = podamFactory.manufacturePojo(InstallmentDTO.class);
    installmentDTO.setStatus(InstallmentDTO.StatusEnum.PAID);
    List<InstallmentDTO> installmentDTOList = List.of(installmentDTO);
    Boolean postalAccess = null;

    // When
    PagoPaNodeFaultException response = Assertions.assertThrows(PagoPaNodeFaultException.class,
      ()->synchronousPaymentStatusVerifierService.verifyPaymentStatus(organization, installmentDTOList, "NAV", postalAccess));

    // Then
    Assertions.assertEquals(PagoPaNodeFaults.PAA_PAGAMENTO_SCONOSCIUTO, response.getErrorCode());
    Assertions.assertEquals(organization.getOrgFiscalCode(), response.getErrorEmitter());
  }

  @Test
  void givenCancelledInstallmentWhenVerifyPaymentStatusThenOk() {
    // Given
    Organization organization = podamFactory.manufacturePojo(Organization.class);
    InstallmentDTO installmentDTO = podamFactory.manufacturePojo(InstallmentDTO.class);
    installmentDTO.setStatus(InstallmentDTO.StatusEnum.TO_SYNC);
    InstallmentDTO otherInstallmentDTO = podamFactory.manufacturePojo(InstallmentDTO.class);
    otherInstallmentDTO.setStatus(InstallmentDTO.StatusEnum.CANCELLED);
    List<InstallmentDTO> installmentDTOList = List.of(installmentDTO, otherInstallmentDTO);
    Boolean postalAccess = null;

    // When
    PagoPaNodeFaultException response = Assertions.assertThrows(PagoPaNodeFaultException.class,
      ()->synchronousPaymentStatusVerifierService.verifyPaymentStatus(organization, installmentDTOList, "NAV", postalAccess));

    // Then
    Assertions.assertEquals(PagoPaNodeFaults.PAA_PAGAMENTO_ANNULLATO, response.getErrorCode());
    Assertions.assertEquals(organization.getOrgFiscalCode(), response.getErrorEmitter());
  }

  @Test
  void givenFallbackGenericStatusInstallmentWhenVerifyPaymentStatusThenOk() {
    // Given
    Organization organization = podamFactory.manufacturePojo(Organization.class);
    InstallmentDTO installmentDTO = podamFactory.manufacturePojo(InstallmentDTO.class);
    installmentDTO.setStatus(InstallmentDTO.StatusEnum.TO_SYNC);
    List<InstallmentDTO> installmentDTOList = List.of(installmentDTO);
    Boolean postalAccess = null;

    // When
    PagoPaNodeFaultException response = Assertions.assertThrows(PagoPaNodeFaultException.class,
      ()->synchronousPaymentStatusVerifierService.verifyPaymentStatus(organization, installmentDTOList, "NAV", postalAccess));

    // Then
    Assertions.assertEquals(PagoPaNodeFaults.PAA_PAGAMENTO_SCONOSCIUTO, response.getErrorCode());
    Assertions.assertEquals(organization.getOrgFiscalCode(), response.getErrorEmitter());
  }

  //endregion

}
