package it.gov.pagopa.pu.pagopapayments.service.printpaymentnotice;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.PersonDTO;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.pagopapayments.connector.organization.OrganizationService;
import it.gov.pagopa.pu.pagopapayments.connector.pagopa.printpaymentnotice.PrintPaymentNoticeService;
import it.gov.pagopa.pu.pagopapayments.enums.GenerateNoticeTemplates;
import it.gov.pagopa.pu.pagopapayments.mapper.NoticeRequestMapper;
import it.gov.pagopa.pu.printpaymentnotice.connector.printpaymentnotice.generated.dto.NoticeGenerationRequestItemDTO;
import it.gov.pagopa.pu.printpaymentnotice.connector.printpaymentnotice.generated.dto.NoticeRequestDataDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
@Slf4j
public class GenerateNoticeService {

  private final PrintPaymentNoticeService printPaymentNoticeService;
  private final OrganizationService organizationService;
  private static final String TEMPLATE_SINGLE_INSTALMENT = GenerateNoticeTemplates.TEMPLATE_SINGLE_INSTALMENT.templateId();
  private static final String TEMPLATE_SINGLE_INSTALMENT_POSTE = GenerateNoticeTemplates.TEMPLATE_SINGLE_INSTALMENT_POSTE.templateId();

  public GenerateNoticeService(
    PrintPaymentNoticeService printPaymentNoticeService, OrganizationService organizationService
  ) {
    this.printPaymentNoticeService = printPaymentNoticeService;
    this.organizationService = organizationService;
  }

  public File generateNotice(Long organizationId, String taxCode, String iuv, DebtPositionDTO debtPosition, String accessToken) {
    Organization org = organizationService.getOrganizationById(organizationId, accessToken);
    NoticeGenerationRequestItemDTO noticeGenerationRequestItemDTO = generateNoticeRequest(org, taxCode, iuv, debtPosition);
    return printPaymentNoticeService.generateNotice(org.getBrokerId(), noticeGenerationRequestItemDTO, accessToken);
  }

  private NoticeGenerationRequestItemDTO generateNoticeRequest(Organization org, String taxCode, String iuv, DebtPositionDTO debtPosition) {
    Pair<InstallmentDTO, PersonDTO> installmentPersonPair = findInstallmentAndDebtorByIuv(debtPosition, iuv);
    if (installmentPersonPair == null) {
      throw new IllegalArgumentException("No installment found for the provided IUV: " + iuv);
    }

    NoticeGenerationRequestItemDTO noticeGenerationRequestItemDTO = new NoticeGenerationRequestItemDTO();

    NoticeRequestDataDTO noticeRequestDataDTO = NoticeRequestMapper.toNoticeRequestDataDTO(
      taxCode,
      installmentPersonPair.getLeft(),
      installmentPersonPair.getRight()
    );

    noticeGenerationRequestItemDTO.data(noticeRequestDataDTO);
    noticeGenerationRequestItemDTO.setTemplateId(
      org.getIban() != null
        ? TEMPLATE_SINGLE_INSTALMENT
        : TEMPLATE_SINGLE_INSTALMENT_POSTE
    );

    return noticeGenerationRequestItemDTO;
  }

  public Pair<InstallmentDTO, PersonDTO> findInstallmentAndDebtorByIuv(DebtPositionDTO debtPosition, String iuv) {
    return debtPosition.getPaymentOptions().stream()
      .flatMap(po -> po.getInstallments().stream())
      .filter(installment -> iuv.equals(installment.getIuv()))
      .map(installment -> Pair.of(installment, installment.getDebtor()))
      .findFirst()
      .orElse(null);
  }

}
