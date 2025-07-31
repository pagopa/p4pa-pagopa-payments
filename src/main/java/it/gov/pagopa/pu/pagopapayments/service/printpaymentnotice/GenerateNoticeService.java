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
import it.gov.pagopa.pu.printpaymentnotice.connector.printpaymentnotice.generated.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import static it.gov.pagopa.pu.printpaymentnotice.connector.printpaymentnotice.generated.dto.GetGenerationRequestStatusResourceDTO.StatusEnum.*;

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

  public NoticeDataDTO generateNotice(String iuv, DebtPositionDTO debtPosition, String accessToken) {
    Organization org = organizationService.getOrganizationById(debtPosition.getOrganizationId(), accessToken);
    NoticeGenerationRequestItemDTO noticeData = generateNoticeRequest(org, iuv, debtPosition);
    log.info("generateNotice for organization with id[{}], notice code[{}] and templateId[{}]", org.getOrganizationId(), noticeData.getData().getNotice().getCode(), noticeData.getTemplateId());

    byte[] noticeGenerated = printPaymentNoticeService.generateNotice(org.getOrganizationId(), noticeData, accessToken);
    return NoticeDataDTO.builder()
      .notice(noticeGenerated)
      .fileName(org.getOrgFiscalCode() + "_" + iuv + ".pdf")
      .build();
  }

  public GeneratedNoticeMassiveFolderDTO generateNoticeMassive(NoticeRequestMassiveDTO request, String accessToken) {
    Organization org = organizationService.getOrganizationById(request.getDebtPositions().getFirst().getOrganizationId(), accessToken);
    log.info("generateNoticeMassive - retrieved organization with id[{}]", org.getOrganizationId());

    NoticeGenerationMassiveResourceDTO response;
    NoticeGenerationMassiveRequestDTO requestMassive;

    if (CollectionUtils.isEmpty(request.getIuvList())) {
      requestMassive = generateMassiveFromToSync(org, request.getDebtPositions());
    } else {
      requestMassive = generateMassiveFromIuvList(org, request.getDebtPositions(), request.getIuvList());
    }
    log.info("calling generateNoticeMassive with organizationId[{}] and a list with [{}] notices", org.getOrganizationId(), requestMassive.getNotices().size());

    response = printPaymentNoticeService.generateNoticeMassive(org.getOrganizationId(), request.getRequestId(), requestMassive, accessToken);
    log.info("generateNoticeMassive - retrieved folderId[{}]", response.getFolderId());

    return GeneratedNoticeMassiveFolderMapper.toGeneratedNoticeMassiveFolderDTO(response);
  }

  public SignedUrlResultDTO getNoticeMassiveZip(Long organizationId, String folderId, String accessToken) {
    Organization org = organizationService.getOrganizationById(organizationId, accessToken);
    log.info("getNoticeMassiveZip - retrieved organization with id and calling getFolderStatus with organizationId[{}], folderId[{}]", org.getOrganizationId(), folderId);

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

  public NoticeGenerationRequestItemDTO generateNoticeRequest(Organization org, String iuv, DebtPositionDTO debtPosition) {
    InstallmentDTO installment = findInstallmentAndDebtorByIuv(debtPosition, iuv);
    if (installment == null) {
      throw new IllegalArgumentException("No installment found for the provided IUV: " + iuv);
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
    log.info("Generate massive notice from Installments in TO_SYNC status for Organization with id[{}]", org.getOrganizationId());
    return generateMassiveGeneric(org, debtPositions, inst -> Objects.equals(inst.getStatus(), InstallmentStatus.TO_SYNC));
  }

  public NoticeGenerationMassiveRequestDTO generateMassiveFromIuvList(Organization org, List<DebtPositionDTO> debtPositions, List<String> iuvList) {
    log.info("Generate massive notice from Iuv list given in input [{}]", iuvList);
    return generateMassiveGeneric(org, debtPositions, inst -> iuvList.contains(inst.getIuv()));
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

  public InstallmentDTO findInstallmentAndDebtorByIuv(DebtPositionDTO debtPosition, String iuv) {
    log.info("findInstallmentAndDebtorByIuv on debtPosition with id[{}] and iuv iuv[{}]", debtPosition.getDebtPositionId(), iuv);
    return debtPosition.getPaymentOptions().stream()
      .flatMap(po -> po.getInstallments().stream())
      .filter(installment -> iuv.equals(installment.getIuv()))
      .findFirst()
      .orElse(null);
  }

}
