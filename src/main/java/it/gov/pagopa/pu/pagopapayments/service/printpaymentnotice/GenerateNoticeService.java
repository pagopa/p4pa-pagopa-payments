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

  public NoticeDataDTO generateNotice(Long organizationId, String iuv, DebtPositionDTO debtPosition, String accessToken) {
    Organization org = organizationService.getOrganizationById(organizationId, accessToken);
    NoticeGenerationRequestItemDTO noticeGenerationRequestItemDTO = generateNoticeRequest(org, iuv, debtPosition);
    byte[] noticeData = printPaymentNoticeService.generateNotice(org.getBrokerId(), noticeGenerationRequestItemDTO, accessToken);
    return NoticeDataDTO.builder()
      .notice(noticeData)
      .fileName(org.getOrgFiscalCode() + "_" + iuv + ".pdf")
      .build();
  }

  public GeneratedNoticeMassiveFolderDTO generateNoticeMassive(NoticeRequestMassiveDTO request, String accessToken) {
    Organization org = organizationService.getOrganizationById(request.getOrganizationId(), accessToken);
    NoticeGenerationMassiveResourceDTO response;
    NoticeGenerationMassiveRequestDTO requestMassive;

    if (CollectionUtils.isEmpty(request.getIuvList())) {
      requestMassive = generateMassiveFromUnpaid(org, request.getDebtPositions());
    } else {
      requestMassive = generateMassiveFromIuvList(org, request.getDebtPositions(), request.getIuvList());
    }

    response = printPaymentNoticeService.generateNoticeMassive(org.getBrokerId(), request.getRequestId(), requestMassive, accessToken);
    return GeneratedNoticeMassiveFolderMapper.toGeneratedNoticeMassiveFolderDTO(response);
  }

  public SignedUrlResultDTO getNoticeMassiveZip(Long organizationId, String folderId, String accessToken) {
    Organization org = organizationService.getOrganizationById(organizationId, accessToken);
    GetGenerationRequestStatusResourceDTO folderStatus = printPaymentNoticeService.getFolderStatus(org.getBrokerId(), folderId, accessToken);
    SignedUrlResultDTO result = new SignedUrlResultDTO();

    GetGenerationRequestStatusResourceDTO.StatusEnum status = folderStatus.getStatus();
    if (status == PROCESSED || status == PROCESSED_WITH_FAILURES || status == FAILED) {
      GetSignedUrlResourceDTO signedUrlRes = printPaymentNoticeService.getFolderSignedUrlResource(org.getBrokerId(), folderId, accessToken);
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

  public NoticeGenerationMassiveRequestDTO generateMassiveFromUnpaid(Organization org, List<DebtPositionDTO> debtPositions) {
    return generateMassiveGeneric(org, debtPositions, inst -> Objects.equals(inst.getStatus(), InstallmentStatus.UNPAID));
  }

  public NoticeGenerationMassiveRequestDTO generateMassiveFromIuvList(Organization org, List<DebtPositionDTO> debtPositions, List<String> iuvList) {
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
    return debtPosition.getPaymentOptions().stream()
      .flatMap(po -> po.getInstallments().stream())
      .filter(installment -> iuv.equals(installment.getIuv()))
      .findFirst()
      .orElse(null);
  }

}
