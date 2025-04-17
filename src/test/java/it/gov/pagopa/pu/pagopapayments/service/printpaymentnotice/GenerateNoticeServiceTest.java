package it.gov.pagopa.pu.pagopapayments.service.printpaymentnotice;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.PaymentOptionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.PersonDTO;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.pagopapayments.connector.organization.OrganizationService;
import it.gov.pagopa.pu.pagopapayments.connector.pagopa.printpaymentnotice.PrintPaymentNoticeService;
import it.gov.pagopa.pu.pagopapayments.enums.GenerateNoticeTemplates;
import it.gov.pagopa.pu.pagopapayments.mapper.NoticeRequestMapper;
import it.gov.pagopa.pu.pagopapayments.util.TestUtils;
import it.gov.pagopa.pu.printpaymentnotice.connector.printpaymentnotice.generated.dto.NoticeGenerationRequestItemDTO;
import it.gov.pagopa.pu.printpaymentnotice.connector.printpaymentnotice.generated.dto.NoticeRequestDataDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.jemos.podam.api.PodamFactory;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class GenerateNoticeServiceTest {

  @Mock
  private PrintPaymentNoticeService printPaymentNoticeServiceMock;

  @Mock
  private OrganizationService organizationServiceMock;

  @InjectMocks
  private GenerateNoticeService generateNoticeService;

  private final PodamFactory podamFactory;

  private static final String ACCESS_TOKEN = "access-token";
  private static final String TEST_IUV = "IUV123";
  private static final String TEST_TAX_CODE = "99999999982";

  public GenerateNoticeServiceTest() {
    podamFactory = TestUtils.getPodamFactory();
  }

  @Test
  void givenValidDataWhenGenerateNoticeThenFileReturned() {
    // given
    Long organizationId = 1L;
    Organization organization = podamFactory.manufacturePojo(Organization.class);
    organization.setIban("IT123456");

    InstallmentDTO installment = podamFactory.manufacturePojo(InstallmentDTO.class);
    installment.setIuv(TEST_IUV);

    PaymentOptionDTO paymentOption = podamFactory.manufacturePojo(PaymentOptionDTO.class);
    paymentOption.setInstallments(List.of(installment));

    DebtPositionDTO debtPosition = podamFactory.manufacturePojo(DebtPositionDTO.class);
    debtPosition.setPaymentOptions(List.of(paymentOption));

    NoticeRequestDataDTO noticeRequestData = NoticeRequestMapper.toNoticeRequestDataDTO(
      TEST_TAX_CODE,
      installment,
      installment.getDebtor()
    );

    NoticeGenerationRequestItemDTO noticeGenerationRequestItem = new NoticeGenerationRequestItemDTO();
    noticeGenerationRequestItem.setData(noticeRequestData);
    noticeGenerationRequestItem.setTemplateId(GenerateNoticeTemplates.TEMPLATE_SINGLE_INSTALMENT.templateId());

    File expectedFile = mock(File.class);

    Mockito.when(organizationServiceMock.getOrganizationById(organizationId, ACCESS_TOKEN))
      .thenReturn(organization);

    Mockito.when(printPaymentNoticeServiceMock.generateNotice(
      organization.getBrokerId(),
      noticeGenerationRequestItem,
      ACCESS_TOKEN)
    ).thenReturn(expectedFile);

    // when
    File result = generateNoticeService.generateNotice(organizationId, TEST_TAX_CODE, TEST_IUV, debtPosition, ACCESS_TOKEN);

    // then
    assertNotNull(result);
    assertEquals(expectedFile, result);

    Mockito.verify(organizationServiceMock).getOrganizationById(organizationId, ACCESS_TOKEN);
    Mockito.verify(printPaymentNoticeServiceMock).generateNotice(
      organization.getBrokerId(),
      noticeGenerationRequestItem,
      ACCESS_TOKEN);
  }

  @Test
  void givenValidDataInstalmentPosteWhenGenerateNoticeThenFileReturned() {
    // given
    Long organizationId = 1L;
    Organization organization = podamFactory.manufacturePojo(Organization.class);
    organization.setIban(null);

    InstallmentDTO installment = podamFactory.manufacturePojo(InstallmentDTO.class);
    installment.setIuv(TEST_IUV);

    PaymentOptionDTO paymentOption = podamFactory.manufacturePojo(PaymentOptionDTO.class);
    paymentOption.setInstallments(List.of(installment));

    DebtPositionDTO debtPosition = podamFactory.manufacturePojo(DebtPositionDTO.class);
    debtPosition.setPaymentOptions(List.of(paymentOption));

    NoticeRequestDataDTO noticeRequestData = NoticeRequestMapper.toNoticeRequestDataDTO(
      TEST_TAX_CODE,
      installment,
      installment.getDebtor()
    );

    NoticeGenerationRequestItemDTO noticeGenerationRequestItem = new NoticeGenerationRequestItemDTO();
    noticeGenerationRequestItem.setData(noticeRequestData);
    noticeGenerationRequestItem.setTemplateId(GenerateNoticeTemplates.TEMPLATE_SINGLE_INSTALMENT_POSTE.templateId());

    File expectedFile = mock(File.class);

    Mockito.when(organizationServiceMock.getOrganizationById(organizationId, ACCESS_TOKEN))
      .thenReturn(organization);

    Mockito.when(printPaymentNoticeServiceMock.generateNotice(
      organization.getBrokerId(),
      noticeGenerationRequestItem,
      ACCESS_TOKEN)
    ).thenReturn(expectedFile);

    // when
    File result = generateNoticeService.generateNotice(organizationId, TEST_TAX_CODE, TEST_IUV, debtPosition, ACCESS_TOKEN);

    // then
    assertNotNull(result);
    assertEquals(expectedFile, result);

    Mockito.verify(organizationServiceMock).getOrganizationById(organizationId, ACCESS_TOKEN);
    Mockito.verify(printPaymentNoticeServiceMock).generateNotice(
      organization.getBrokerId(),
      noticeGenerationRequestItem,
      ACCESS_TOKEN);
  }

  @Test
  void givenInvalidIuvWhenGenerateNoticeThenThrowsException() {
    // given
    Long organizationId = 1L;
    String invalidIuv = "INVALID-IUV";
    Organization organization = podamFactory.manufacturePojo(Organization.class);

    InstallmentDTO installment = podamFactory.manufacturePojo(InstallmentDTO.class);
    installment.setIuv("DIFFERENT-IUV");

    PaymentOptionDTO paymentOption = new PaymentOptionDTO();
    paymentOption.setInstallments(List.of(installment));

    DebtPositionDTO debtPosition = podamFactory.manufacturePojo(DebtPositionDTO.class);

    Mockito.when(organizationServiceMock.getOrganizationById(organizationId, ACCESS_TOKEN))
      .thenReturn(organization);

    // when & then
    IllegalArgumentException exception = assertThrows(
      IllegalArgumentException.class,
      () -> generateNoticeService.generateNotice(organizationId, TEST_TAX_CODE, invalidIuv, debtPosition, ACCESS_TOKEN)
    );

    assertEquals("No installment found for the provided IUV: " + invalidIuv, exception.getMessage());
    Mockito.verify(organizationServiceMock).getOrganizationById(organizationId, ACCESS_TOKEN);
    Mockito.verifyNoInteractions(printPaymentNoticeServiceMock);
  }

  @Test
  void givenValidIuvWhenFindInstallmentAndDebtorByIuvThenReturnsPair() {
    // given
    PersonDTO debtor = podamFactory.manufacturePojo(PersonDTO.class);
    InstallmentDTO installment = new InstallmentDTO();
    installment.setIuv(TEST_IUV);
    installment.setDebtor(debtor);

    PaymentOptionDTO paymentOption = new PaymentOptionDTO();
    paymentOption.setInstallments(List.of(installment));

    DebtPositionDTO debtPosition = podamFactory.manufacturePojo(DebtPositionDTO.class);
    debtPosition.setPaymentOptions(List.of(paymentOption));

    // when
    InstallmentDTO result = generateNoticeService.findInstallmentAndDebtorByIuv(debtPosition, TEST_IUV);

    // then
    assertNotNull(result);
    assertEquals(installment, result);
  }

  @Test
  void givenMissingIuvWhenFindInstallmentAndDebtorByIuvThenReturnsNull() {
    // given
    InstallmentDTO installment = new InstallmentDTO();
    installment.setIuv("OTHER-IUV");

    PaymentOptionDTO paymentOption = new PaymentOptionDTO();
    paymentOption.setInstallments(List.of(installment));

    DebtPositionDTO debtPosition = podamFactory.manufacturePojo(DebtPositionDTO.class);
    debtPosition.setPaymentOptions(List.of(paymentOption));

    // when
   InstallmentDTO result = generateNoticeService.findInstallmentAndDebtorByIuv(debtPosition, TEST_IUV);

    // then
    assertNull(result);
  }
}

