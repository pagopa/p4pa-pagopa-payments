package it.gov.pagopa.pu.pagopapayments.service.printpaymentnotice;

import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.pagopapayments.connector.organization.OrganizationService;
import it.gov.pagopa.pu.pagopapayments.connector.pagopa.printpaymentnotice.PrintPaymentNoticeService;
import it.gov.pagopa.pu.pagopapayments.dto.NoticeDataDTO;
import it.gov.pagopa.pu.pagopapayments.dto.generated.GeneratedNoticeMassiveFolderDTO;
import it.gov.pagopa.pu.pagopapayments.dto.generated.NoticeRequestMassiveDTO;
import it.gov.pagopa.pu.pagopapayments.enums.GenerateNoticeTemplates;
import it.gov.pagopa.pu.pagopapayments.mapper.NoticeRequestMapper;
import it.gov.pagopa.pu.pagopapayments.util.TestUtils;
import it.gov.pagopa.pu.printpaymentnotice.connector.printpaymentnotice.generated.dto.NoticeGenerationMassiveRequestDTO;
import it.gov.pagopa.pu.printpaymentnotice.connector.printpaymentnotice.generated.dto.NoticeGenerationMassiveResourceDTO;
import it.gov.pagopa.pu.printpaymentnotice.connector.printpaymentnotice.generated.dto.NoticeGenerationRequestItemDTO;
import it.gov.pagopa.pu.printpaymentnotice.connector.printpaymentnotice.generated.dto.NoticeRequestDataDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.jemos.podam.api.PodamFactory;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

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
  private static final Long ORGANIZATION_ID = 1L;

  public GenerateNoticeServiceTest() {
    podamFactory = TestUtils.getPodamFactory();
  }

  @Test
  void givenValidDataWhenGenerateNoticeThenFileReturned() {
    // given
    Organization organization = podamFactory.manufacturePojo(Organization.class);
    organization.setOrgFiscalCode("99999999982");
    organization.setIban("IT123456");

    InstallmentDTO installment = podamFactory.manufacturePojo(InstallmentDTO.class);
    installment.setIuv(TEST_IUV);

    PaymentOptionDTO paymentOption = podamFactory.manufacturePojo(PaymentOptionDTO.class);
    paymentOption.setInstallments(List.of(installment));

    DebtPositionDTO debtPosition = podamFactory.manufacturePojo(DebtPositionDTO.class);
    debtPosition.setPaymentOptions(List.of(paymentOption));

    NoticeRequestDataDTO noticeRequestData = NoticeRequestMapper.toNoticeRequestDataDTO(
      organization.getOrgFiscalCode(),
      installment
    );

    NoticeGenerationRequestItemDTO noticeGenerationRequestItem = new NoticeGenerationRequestItemDTO();
    noticeGenerationRequestItem.setData(noticeRequestData);
    noticeGenerationRequestItem.setTemplateId(GenerateNoticeTemplates.TEMPLATE_SINGLE_INSTALMENT.templateId());

    byte[] expectedResult = "PDF-DATA".getBytes();
    NoticeDataDTO noticeData = NoticeDataDTO.builder()
      .notice(expectedResult)
      .fileName("99999999982_IUV123.pdf")
      .build();

    Mockito.when(organizationServiceMock.getOrganizationById(ORGANIZATION_ID, ACCESS_TOKEN))
      .thenReturn(organization);

    Mockito.when(printPaymentNoticeServiceMock.generateNotice(
      organization.getBrokerId(),
      noticeGenerationRequestItem,
      ACCESS_TOKEN)
    ).thenReturn(expectedResult);

    // when
    NoticeDataDTO result = generateNoticeService.generateNotice(ORGANIZATION_ID, TEST_IUV, debtPosition, ACCESS_TOKEN);

    // then
    assertNotNull(result);
    assertEquals(noticeData, result);

    Mockito.verify(organizationServiceMock).getOrganizationById(ORGANIZATION_ID, ACCESS_TOKEN);
    Mockito.verify(printPaymentNoticeServiceMock).generateNotice(
      organization.getBrokerId(),
      noticeGenerationRequestItem,
      ACCESS_TOKEN);
  }

  @Test
  void givenValidDataInstalmentPosteWhenGenerateNoticeThenFileReturned() {
    // given
    Organization organization = podamFactory.manufacturePojo(Organization.class);
    organization.setOrgFiscalCode("99999999982");
    organization.setIban(null);

    InstallmentDTO installment = podamFactory.manufacturePojo(InstallmentDTO.class);
    installment.setIuv(TEST_IUV);

    PaymentOptionDTO paymentOption = podamFactory.manufacturePojo(PaymentOptionDTO.class);
    paymentOption.setInstallments(List.of(installment));

    DebtPositionDTO debtPosition = podamFactory.manufacturePojo(DebtPositionDTO.class);
    debtPosition.setPaymentOptions(List.of(paymentOption));

    NoticeRequestDataDTO noticeRequestData = NoticeRequestMapper.toNoticeRequestDataDTO(
      organization.getOrgFiscalCode(),
      installment
    );

    NoticeGenerationRequestItemDTO noticeGenerationRequestItem = new NoticeGenerationRequestItemDTO();
    noticeGenerationRequestItem.setData(noticeRequestData);
    noticeGenerationRequestItem.setTemplateId(GenerateNoticeTemplates.TEMPLATE_SINGLE_INSTALMENT_POSTE.templateId());

    byte[] expectedResult = "PDF-DATA".getBytes();
    NoticeDataDTO noticeData = NoticeDataDTO.builder()
      .notice(expectedResult)
      .fileName("99999999982_IUV123.pdf")
      .build();

    Mockito.when(organizationServiceMock.getOrganizationById(ORGANIZATION_ID, ACCESS_TOKEN))
      .thenReturn(organization);

    Mockito.when(printPaymentNoticeServiceMock.generateNotice(
      organization.getBrokerId(),
      noticeGenerationRequestItem,
      ACCESS_TOKEN)
    ).thenReturn(expectedResult);

    // when
    NoticeDataDTO result = generateNoticeService.generateNotice(ORGANIZATION_ID, TEST_IUV, debtPosition, ACCESS_TOKEN);

    // then
    assertNotNull(result);
    assertEquals(noticeData, result);

    Mockito.verify(organizationServiceMock).getOrganizationById(ORGANIZATION_ID, ACCESS_TOKEN);
    Mockito.verify(printPaymentNoticeServiceMock).generateNotice(
      organization.getBrokerId(),
      noticeGenerationRequestItem,
      ACCESS_TOKEN);
  }

  @Test
  void givenInvalidIuvWhenGenerateNoticeThenThrowsException() {
    // given
    String invalidIuv = "INVALID-IUV";
    Organization organization = podamFactory.manufacturePojo(Organization.class);

    InstallmentDTO installment = podamFactory.manufacturePojo(InstallmentDTO.class);
    installment.setIuv("DIFFERENT-IUV");

    PaymentOptionDTO paymentOption = new PaymentOptionDTO();
    paymentOption.setInstallments(List.of(installment));

    DebtPositionDTO debtPosition = podamFactory.manufacturePojo(DebtPositionDTO.class);

    Mockito.when(organizationServiceMock.getOrganizationById(ORGANIZATION_ID, ACCESS_TOKEN))
      .thenReturn(organization);

    // when & then
    IllegalArgumentException exception = assertThrows(
      IllegalArgumentException.class,
      () -> generateNoticeService.generateNotice(ORGANIZATION_ID, invalidIuv, debtPosition, ACCESS_TOKEN)
    );

    assertEquals("No installment found for the provided IUV: " + invalidIuv, exception.getMessage());
    Mockito.verify(organizationServiceMock).getOrganizationById(ORGANIZATION_ID, ACCESS_TOKEN);
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

  @Test
  void givenValidRequestWithoutIuvListWhenGenerateNoticeMassiveThenOk() {
    //given
    Organization organization = podamFactory.manufacturePojo(Organization.class);
    NoticeRequestMassiveDTO request = podamFactory.manufacturePojo(NoticeRequestMassiveDTO.class);
    request.setIuvList(null);

    NoticeGenerationMassiveResourceDTO resourceDTO = podamFactory.manufacturePojo(NoticeGenerationMassiveResourceDTO.class);

    NoticeGenerationMassiveRequestDTO requestMassive = new NoticeGenerationMassiveRequestDTO();

    InstallmentDTO installment = podamFactory.manufacturePojo(InstallmentDTO.class);
    installment.setStatus(InstallmentStatus.UNPAID);
    installment.setNav("3" + TEST_IUV);
    InstallmentDTO secondInstallment = podamFactory.manufacturePojo(InstallmentDTO.class);
    secondInstallment.setStatus(InstallmentStatus.PAID);
    secondInstallment.setNav("150" + TEST_IUV);

    PaymentOptionDTO paymentOption = podamFactory.manufacturePojo(PaymentOptionDTO.class);
    paymentOption.setInstallments(List.of(installment, secondInstallment));

    DebtPositionDTO debtPosition = podamFactory.manufacturePojo(DebtPositionDTO.class);
    debtPosition.setPaymentOptions(List.of(paymentOption));
    request.setDebtPositions(List.of(debtPosition));

    NoticeRequestDataDTO noticeRequestData = NoticeRequestMapper.toNoticeRequestDataDTO(
      organization.getOrgFiscalCode(),
      installment
    );

    NoticeGenerationRequestItemDTO noticeGenerationRequestItem = new NoticeGenerationRequestItemDTO();
    noticeGenerationRequestItem.setData(noticeRequestData);
    noticeGenerationRequestItem.setTemplateId(GenerateNoticeTemplates.TEMPLATE_SINGLE_INSTALMENT.templateId());

    requestMassive.setNotices(List.of(noticeGenerationRequestItem));

    Mockito.when(organizationServiceMock.getOrganizationById(request.getOrganizationId(), ACCESS_TOKEN))
      .thenReturn(organization);
    Mockito.when(printPaymentNoticeServiceMock.generateNoticeMassive(
        organization.getBrokerId(), request.getRequestId(), requestMassive, ACCESS_TOKEN))
      .thenReturn(resourceDTO);

    //when
    GeneratedNoticeMassiveFolderDTO result = generateNoticeService.generateNoticeMassive(request, ACCESS_TOKEN);

    //then
    assertEquals(1, requestMassive.getNotices().size());
    assertEquals("3" + TEST_IUV, requestMassive.getNotices().getFirst().getData().getNotice().getCode());
    assertNotNull(result);
    Mockito.verify(organizationServiceMock).getOrganizationById(request.getOrganizationId(), ACCESS_TOKEN);
    Mockito.verify(printPaymentNoticeServiceMock).generateNoticeMassive(
      organization.getBrokerId(), request.getRequestId(), requestMassive, ACCESS_TOKEN);
  }

  @Test
  void givenValidRequestWithIuvListWhenGenerateNoticeMassiveThenOk() {
    //given
    Organization organization = podamFactory.manufacturePojo(Organization.class);
    NoticeRequestMassiveDTO request = podamFactory.manufacturePojo(NoticeRequestMassiveDTO.class);
    request.setIuvList(List.of(TEST_IUV, "IUV456"));

    NoticeGenerationMassiveResourceDTO resourceDTO = podamFactory.manufacturePojo(NoticeGenerationMassiveResourceDTO.class);

    NoticeGenerationMassiveRequestDTO requestMassive = new NoticeGenerationMassiveRequestDTO();

    InstallmentDTO installment = podamFactory.manufacturePojo(InstallmentDTO.class);
    installment.setIuv(TEST_IUV);
    installment.setNav("3" + TEST_IUV);
    InstallmentDTO secondInstallment = podamFactory.manufacturePojo(InstallmentDTO.class);
    secondInstallment.setIuv("IUVVV");
    secondInstallment.setNav("150" + TEST_IUV);

    PaymentOptionDTO paymentOption = podamFactory.manufacturePojo(PaymentOptionDTO.class);
    paymentOption.setInstallments(List.of(installment, secondInstallment));

    DebtPositionDTO debtPosition = podamFactory.manufacturePojo(DebtPositionDTO.class);
    debtPosition.setPaymentOptions(List.of(paymentOption));
    request.setDebtPositions(List.of(debtPosition));

    NoticeRequestDataDTO noticeRequestData = NoticeRequestMapper.toNoticeRequestDataDTO(
      organization.getOrgFiscalCode(),
      installment
    );

    NoticeGenerationRequestItemDTO noticeGenerationRequestItem = new NoticeGenerationRequestItemDTO();
    noticeGenerationRequestItem.setData(noticeRequestData);
    noticeGenerationRequestItem.setTemplateId(GenerateNoticeTemplates.TEMPLATE_SINGLE_INSTALMENT.templateId());

    requestMassive.setNotices(List.of(noticeGenerationRequestItem));

    Mockito.when(organizationServiceMock.getOrganizationById(request.getOrganizationId(), ACCESS_TOKEN))
      .thenReturn(organization);
    Mockito.when(printPaymentNoticeServiceMock.generateNoticeMassive(
        organization.getBrokerId(), request.getRequestId(), requestMassive, ACCESS_TOKEN))
      .thenReturn(resourceDTO);

    //when
    GeneratedNoticeMassiveFolderDTO result = generateNoticeService.generateNoticeMassive(request, ACCESS_TOKEN);

    //then
    assertEquals(1, requestMassive.getNotices().size());
    assertEquals("3" + TEST_IUV, requestMassive.getNotices().getFirst().getData().getNotice().getCode());
    assertNotNull(result);
    Mockito.verify(organizationServiceMock).getOrganizationById(request.getOrganizationId(), ACCESS_TOKEN);
    Mockito.verify(printPaymentNoticeServiceMock).generateNoticeMassive(
      organization.getBrokerId(), request.getRequestId(), requestMassive, ACCESS_TOKEN);
  }
}

