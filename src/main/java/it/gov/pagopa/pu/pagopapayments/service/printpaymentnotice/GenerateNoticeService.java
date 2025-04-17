package it.gov.pagopa.pu.pagopapayments.service.printpaymentnotice;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.pagopapayments.connector.organization.OrganizationService;
import it.gov.pagopa.pu.pagopapayments.connector.pagopa.printpaymentnotice.PrintPaymentNoticeService;
import it.gov.pagopa.pu.pagopapayments.enums.GenerateNoticeTemplates;
import it.gov.pagopa.pu.pagopapayments.mapper.NoticeRequestMapper;
import it.gov.pagopa.pu.printpaymentnotice.connector.printpaymentnotice.generated.dto.NoticeGenerationRequestItemDTO;
import it.gov.pagopa.pu.printpaymentnotice.connector.printpaymentnotice.generated.dto.NoticeRequestDataDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
@Slf4j
public class GenerateNoticeService {

  private final PrintPaymentNoticeService printPaymentNoticeService;
  private final OrganizationService organizationService;

  public GenerateNoticeService(
    PrintPaymentNoticeService printPaymentNoticeService, OrganizationService organizationService
  ) {
    this.printPaymentNoticeService = printPaymentNoticeService;
    this.organizationService = organizationService;
  }

  public File generateNotice(Long organizationId, String iuv, DebtPositionDTO debtPosition, String accessToken) {
    Organization org = organizationService.getOrganizationById(organizationId, accessToken);
    NoticeGenerationRequestItemDTO noticeGenerationRequestItemDTO = generateNoticeRequest(org, iuv, debtPosition);
    return printPaymentNoticeService.generateNotice(org.getBrokerId(), noticeGenerationRequestItemDTO, accessToken);
  }

  private NoticeGenerationRequestItemDTO generateNoticeRequest(Organization org, String iuv, DebtPositionDTO debtPosition) {
    InstallmentDTO installment = findInstallmentAndDebtorByIuv(debtPosition, iuv);
    if (installment == null) {
      throw new IllegalArgumentException("No installment found for the provided IUV: " + iuv);
    }

    NoticeGenerationRequestItemDTO noticeGenerationRequestItemDTO = new NoticeGenerationRequestItemDTO();

    NoticeRequestDataDTO noticeRequestDataDTO = NoticeRequestMapper.toNoticeRequestDataDTO(
      org.getOrgFiscalCode(),
      installment,
      installment.getDebtor()
    );

    noticeGenerationRequestItemDTO.data(noticeRequestDataDTO);
    noticeGenerationRequestItemDTO.setTemplateId(
      org.getIban() != null
        ? GenerateNoticeTemplates.TEMPLATE_SINGLE_INSTALMENT.templateId()
        : GenerateNoticeTemplates.TEMPLATE_SINGLE_INSTALMENT_POSTE.templateId()
    );

    return noticeGenerationRequestItemDTO;
  }

  public InstallmentDTO findInstallmentAndDebtorByIuv(DebtPositionDTO debtPosition, String iuv) {
    return debtPosition.getPaymentOptions().stream()
      .flatMap(po -> po.getInstallments().stream())
      .filter(installment -> iuv.equals(installment.getIuv()))
      .findFirst()
      .orElse(null);
  }

}
