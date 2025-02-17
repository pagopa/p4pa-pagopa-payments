package it.gov.pagopa.pu.pagopapayments.connector.fileshare.client;

import it.gov.pagopa.pu.fileshare.dto.generated.FileOrigin;
import it.gov.pagopa.pu.fileshare.dto.generated.IngestionFlowFileType;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.pagopapayments.connector.fileshare.config.FileShareApisHolder;
import it.gov.pagopa.pu.pagopapayments.dto.PaPaymentReportingDTO;
import it.gov.pagopa.pu.pagopapayments.dto.PaSendRtDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class FileShareClient {

  private final FileShareApisHolder apisHolder;

  public FileShareClient(FileShareApisHolder apisHolder){
    this.apisHolder = apisHolder;
  }

  public Long uploadRt(PaSendRtDTO paSendRtDTO, Organization organization, String accessToken) {
    return apisHolder.getIngestionFlowFileApi(accessToken)
      .uploadIngestionFlowFile(
        organization.getOrganizationId(),
        IngestionFlowFileType.RECEIPT_PAGOPA,
        FileOrigin.PAGOPA,
        "RT_" + paSendRtDTO.getNoticeNumber() + ".xml",
        new ByteArrayResource(paSendRtDTO.getReceiptBytes()))
      .getIngestionFlowFileId();
  }

  public Long uploadPaymentReporting(PaPaymentReportingDTO paPaymentReporingDTO, Long organizationId, String fileName, String accessToken) {
    return apisHolder.getIngestionFlowFileApi(accessToken)
      .uploadIngestionFlowFile(
        organizationId,
        IngestionFlowFileType.PAYMENTS_REPORTING_PAGOPA,
        FileOrigin.PAGOPA,
        fileName,
        new ByteArrayResource(paPaymentReporingDTO.getPaymentReportingBytes()) )
      .getIngestionFlowFileId();
  }

}
