package it.gov.pagopa.pu.pagopapayments.service.printpaymentnotice;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.pagopapayments.connector.organization.OrganizationService;
import it.gov.pagopa.pu.pagopapayments.connector.pagopa.printpaymentnotice.PrintPaymentNoticeService;
import it.gov.pagopa.pu.pagopapayments.dto.NoticeDataDTO;
import it.gov.pagopa.pu.pagopapayments.dto.generated.GeneratedNoticeMassiveFolderDTO;
import it.gov.pagopa.pu.pagopapayments.dto.generated.NoticeRequestMassiveDTO;
import it.gov.pagopa.pu.pagopapayments.dto.generated.SignedUrlResultDTO;
import it.gov.pagopa.pu.pagopapayments.enums.GenerateNoticeTemplates;
import it.gov.pagopa.pu.pagopapayments.mapper.GeneratedNoticeMassiveFolderMapper;
import it.gov.pagopa.pu.pagopapayments.mapper.NoticeRequestMapper;
import it.gov.pagopa.nodo.printpaymentnotice.dto.generated.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import static it.gov.pagopa.nodo.printpaymentnotice.dto.generated.GetGenerationRequestStatusResourceDTO.StatusEnum.*;

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

  public NoticeDataDTO generateNotice(String nav, DebtPositionDTO debtPosition, String accessToken) {
    Organization org = organizationService.getOrganizationById(debtPosition.getOrganizationId(), accessToken);
    NoticeGenerationRequestItemDTO noticeData = generateNoticeRequest(org, nav, debtPosition);
    log.info("generateNotice for organization with id[{}], notice code[{}] and templateId[{}]", org.getOrganizationId(), noticeData.getData().getNotice().getCode(), noticeData.getTemplateId());

    byte[] noticeGenerated = printPaymentNoticeService.generateNotice(org.getOrganizationId(), noticeData, accessToken);
    return NoticeDataDTO.builder()
      .notice(noticeGenerated)
      .fileName(org.getOrgFiscalCode() + "_" + nav + ".pdf")
      .build();
  }

  public GeneratedNoticeMassiveFolderDTO generateNoticeMassive(NoticeRequestMassiveDTO request, String accessToken) {
    Organization org = organizationService.getOrganizationById(request.getDebtPositions().getFirst().getOrganizationId(), accessToken);
    log.debug("generateNoticeMassive - retrieved organization with id[{}]", org.getOrganizationId());

    NoticeGenerationMassiveResourceDTO response;
    NoticeGenerationMassiveRequestDTO requestMassive;

    if (CollectionUtils.isEmpty(request.getNavList())) {
      requestMassive = generateMassiveFromToSync(org, request.getDebtPositions());
    } else {
      requestMassive = generateMassiveFromNavList(org, request.getDebtPositions(), request.getNavList());
    }
    log.debug("calling generateNoticeMassive with organizationId[{}] and a list with [{}] notices", org.getOrganizationId(), requestMassive.getNotices().size());

    response = printPaymentNoticeService.generateNoticeMassive(org.getOrganizationId(), request.getRequestId(), requestMassive, accessToken);
    log.info("generateNoticeMassive - retrieved folderId[{}]", response.getFolderId());

    return GeneratedNoticeMassiveFolderMapper.toGeneratedNoticeMassiveFolderDTO(response);
  }

  public SignedUrlResultDTO getNoticeMassiveZip(Long organizationId, String folderId, String accessToken) {
    Organization org = organizationService.getOrganizationById(organizationId, accessToken);
    log.debug("getNoticeMassiveZip - retrieved organization with id and calling getFolderStatus with organizationId[{}], folderId[{}]", org.getOrganizationId(), folderId);

    GetGenerationRequestStatusResourceDTO folderStatus = printPaymentNoticeService.getFolderStatus(org.getOrganizationId(), folderId, accessToken);
    log.info("getFolderStatus - noticesInError[{}], processedNotices [{}]", folderStatus.getNoticesInError(), folderStatus.getProcessedNotices());

    SignedUrlResultDTO result = new SignedUrlResultDTO();
    GetGenerationRequestStatusResourceDTO.StatusEnum status = folderStatus.getStatus();
    if (PROCESSED.equals(status) || PROCESSED_WITH_FAILURES.equals(status) || FAILED.equals(status)) {
      GetSignedUrlResourceDTO signedUrlRes = printPaymentNoticeService.getFolderSignedUrlResource(org.getOrganizationId(), folderId, accessToken);
      result.setNoticesInError(folderStatus.getNoticesInError());
      result.setProcessedNotices(folderStatus.getProcessedNotices());
      result.setSignedUrl(signedUrlRes.getSignedUrl());
      return result;
    }

    return null;
  }

  public NoticeGenerationRequestItemDTO generateNoticeRequest(Organization org, String nav, DebtPositionDTO debtPosition) {
    InstallmentDTO installment = findInstallmentAndDebtorByNav(debtPosition, nav);
    if (installment == null) {
      throw new IllegalArgumentException("No installment found for the provided NAV: " + nav);
    }

    NoticeGenerationRequestItemDTO noticeGenerationRequestItemDTO = new NoticeGenerationRequestItemDTO();

    NoticeRequestDataDTO noticeRequestDataDTO = NoticeRequestMapper.toNoticeRequestDataDTO(
      org.getOrgFiscalCode(),
      installment
    );

    noticeGenerationRequestItemDTO.data(noticeRequestDataDTO);
    noticeGenerationRequestItemDTO.setTemplateId(getTemplateId(org));

    return noticeGenerationRequestItemDTO;
  }

  public NoticeGenerationMassiveRequestDTO generateMassiveFromToSync(Organization org, List<DebtPositionDTO> debtPositions) {
    log.debug("Generate massive notice from Installments in TO_SYNC status for Organization with id[{}]", org.getOrganizationId());
    return generateMassiveGeneric(org, debtPositions, inst -> Objects.equals(inst.getStatus(), InstallmentStatus.TO_SYNC));
  }

  public NoticeGenerationMassiveRequestDTO generateMassiveFromNavList(Organization org, List<DebtPositionDTO> debtPositions, List<String> navList) {
    log.debug("Generate massive notice from Nav list given in input [{}]", navList);
    return generateMassiveGeneric(org, debtPositions, inst -> navList.contains(inst.getNav()));
  }

  private NoticeGenerationMassiveRequestDTO generateMassiveGeneric(Organization org, List<DebtPositionDTO> debtPositions, Predicate<InstallmentDTO> filter) {
    NoticeGenerationMassiveRequestDTO requestMassive = new NoticeGenerationMassiveRequestDTO();
    String templateId = getTemplateId(org);

    List<NoticeGenerationRequestItemDTO> notices = debtPositions.stream()
      .flatMap(dp -> dp.getPaymentOptions().stream())
      .flatMap(po -> po.getInstallments().stream())
      .filter(filter)
      .map(inst -> {
        NoticeRequestDataDTO dataDTO = NoticeRequestMapper.toNoticeRequestDataDTO(
          org.getOrgFiscalCode(),
          inst
        );
        NoticeGenerationRequestItemDTO item = new NoticeGenerationRequestItemDTO();
        item.setData(dataDTO);
        item.setTemplateId(templateId);
        return item;
      })
      .toList();

    requestMassive.setNotices(notices);
    return requestMassive;
  }

  private static String getTemplateId(Organization org) {
    return org.getIban() != null
      ? GenerateNoticeTemplates.TEMPLATE_SINGLE_INSTALMENT.templateId()
      : GenerateNoticeTemplates.TEMPLATE_SINGLE_INSTALMENT_POSTE.templateId();
  }

  public InstallmentDTO findInstallmentAndDebtorByNav(DebtPositionDTO debtPosition, String nav) {
    log.debug("findInstallmentAndDebtorByNav on debtPosition with id[{}] and nav [{}]", debtPosition.getDebtPositionId(), nav);
    return debtPosition.getPaymentOptions().stream()
      .flatMap(po -> po.getInstallments().stream())
      .filter(installment -> nav.equals(installment.getNav()))
      .findFirst()
      .orElse(null);
  }

}
